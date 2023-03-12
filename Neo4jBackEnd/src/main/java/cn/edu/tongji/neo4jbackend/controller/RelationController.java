package cn.edu.tongji.neo4jbackend.controller;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Record;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("neo4j/relation")
public class RelationController {
    private final Driver driver;

    public RelationController(Driver driver) {
        this.driver = driver;
    }

    //获取合作次数最多的演员导演组合
    @GetMapping("actor_director")
    public ResponseEntity<HashMap<String, Object>> getMostCooperationActorDirector(
            @RequestParam(name = "limit", defaultValue = "50") String limit) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            Result res = session.run("match (:Actor)-[r:DirectCoop]->(:Director) " +
                    "where r.actor<>r.director " +
                    "return r.actor,r.director,r.coop_time as num order by num desc limit " + limit);
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<HashMap<String, Object>> list = new ArrayList<>();
            for (Record record : records) {
                HashMap<String, Object> temp = new HashMap<>();
                temp.put("actor", record.get(0).asString());
                temp.put("director", record.get(1).asString());
                temp.put("num", record.get(2).asInt());
                list.add(temp);
            }
            map.put("data", list);
            map.put("time", endTime - startTime);
            return ResponseEntity.ok(map);
        }
    }

    //获取合作次数最多的演员组合
    @GetMapping("actor_actor")
    public ResponseEntity<HashMap<String, Object>> getMostCooperationActorActor(
            @RequestParam(name = "limit", defaultValue = "50") String limit) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            Result res = session.run("match (:Actor)-[r:ActCoop]->(:Actor) " +
                    "where r.actor1 <> r.actor2 " +
                    "return r.actor1,r.actor2,r.coop_time as num order by num desc limit " + limit);
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<HashMap<String, Object>> list = new ArrayList<>();
            for (Record record : records) {
                HashMap<String, Object> temp = new HashMap<>();
                temp.put("actor", record.get(0).asString());
                temp.put("actor2", record.get(1).asString());
                temp.put("num", record.get(2).asInt());
                list.add(temp);
            }
            map.put("data", list);
            map.put("time", endTime - startTime);
            return ResponseEntity.ok(map);
        }
    }

    //获取某类型电影评论最多的演员二人组
    @GetMapping("actor_actor_comment")
    public ResponseEntity<HashMap<String, Object>> getMostCommentActorActor(
            @RequestParam(name = "limit", defaultValue = "50") String limit,
            @RequestParam(name = "category", defaultValue = "Kids & Family") String category) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            Result res = session.run("match (a:Actor)-[r:Act|Star]->(m:Movie" + ")<-[r2:Act|Star]-(b:Actor),(m)-[:Belong]->(c:Category{name:$category})" +
                    "where a.name <> b.name " +
                    "return a.name,b.name,SUM(toInteger(m.reviewNum)) as num order by num desc limit " + limit, parameters("category", category));
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<HashMap<String, Object>> list = new ArrayList<>();
            for (Record record : records) {
                HashMap<String, Object> temp = new HashMap<>();
                temp.put("actor", record.get(0).asString());
                temp.put("actor2", record.get(1).asString());
                temp.put("num", record.get(2).asInt());
                list.add(temp);
            }
            map.put("data", list);
            map.put("time", endTime - startTime);
            return ResponseEntity.ok(map);
        }
    }

    //获取某类型电影评论最多的演员三人组
    @GetMapping("actor_actor_actor_comment")
    public ResponseEntity<HashMap<String, Object>> getMostCommentActors(
            @RequestParam(name = "limit", defaultValue = "50") String limit,
            @RequestParam(name = "category", defaultValue = "Kids & Family") String category) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            Result res = session.run("match (a:Actor)-[r:Act|Star]->(m:Movie" + ")<-[r2:Act|Star]-(b:Actor),(c:Actor)-[r3:Act|Star]->(m:Movie) " +
                    ",(m)-[:Belong]->(d:Category{name:$category}) " +
                    "where a.name <> b.name and a.name <>c.name and b.name<>c.name " +
                    "return a.name,b.name,c.name,SUM(toInteger(m.reviewNum)) as num order by num desc limit " + limit, parameters("category", category));
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<HashMap<String, Object>> list = new ArrayList<>();
            for (Record record : records) {
                HashMap<String, Object> temp = new HashMap<>();
                temp.put("actor", record.get(0).asString());
                temp.put("actor2", record.get(1).asString());
                temp.put("actor3", record.get(2).asString());
                temp.put("num", record.get(3).asInt());
                list.add(temp);
            }
            map.put("data", list);
            map.put("time", endTime - startTime);
            return ResponseEntity.ok(map);
        }
    }


}
