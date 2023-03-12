package cn.edu.tongji.neo4jbackend.controller;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;
import static org.neo4j.driver.Values.parameters;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("neo4j/trace")
public class TraceController {
    private final Driver driver;

    public TraceController(Driver driver) {
        this.driver = driver;
    }

    //获取无Merge关系的MetaData节点信息
    @GetMapping("non_movie")
    public ResponseEntity<HashMap<String, Object>> getNonMovieInfo(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            int limit = pageSize * pageNo;
            String sql = "match (n:MetaData) where not (n)-[:Merge]->() return n " + "limit " + limit;
            Long startTime = System.currentTimeMillis();
            Result res = session.run(sql);
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            //根据pageNo和pageSize进行分页
            int start = (pageNo - 1) * pageSize;
            int end = min(pageNo * pageSize, records.size());
            ArrayList<Map> movieList = new ArrayList<>();
            for (int i = start; i < end; i++) {
                movieList.add(records.get(i).get("n").asMap());
            }
            sql = "match (n:MetaData) where not (n)-[:Merge]->() return count(n)";
            Result resCount = session.run(sql);
            Record recordCount = resCount.single();
            map.put("totalMovie", recordCount.get(0).asInt());
            map.put("time", endTime - startTime);
            map.put("movieList", movieList);
            return ResponseEntity.ok(map);
        }
    }

    @GetMapping("movie")
    public ResponseEntity<HashMap<String, Object>> traceMovieInfo(String name) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            String sql = "match (n:MetaData)-[o:Merge]->(m:Movie{name:$name}) return n";
            Long startTime = System.currentTimeMillis();
            Result res = session.run(sql, parameters("name", name));
            List<Record> record = res.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<Map> movieList = new ArrayList<>();
            for (Record r : record) {
                movieList.add(r.get("n").asMap());
            }
            map.put("time", endTime - startTime);
            map.put("movieList", movieList);
            return ResponseEntity.ok(map);
        }
    }

    @GetMapping("fuzzy")
    public ResponseEntity<HashMap<String, Object>> fuzzyMatchMovie(String name) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            String sql = "match (n:Movie) where n.name =~'.*" + name + ".*' return n.name";
            Result res = session.run(sql);
            List<Record> record = res.list();
            ArrayList<String> movieList = new ArrayList<>();
            for (Record r : record) {
                movieList.add(r.get(0).asString());
            }
            map.put("movieList", movieList);
            return ResponseEntity.ok(map);
        }
    }
}
