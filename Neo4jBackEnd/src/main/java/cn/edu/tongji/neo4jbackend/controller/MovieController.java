package cn.edu.tongji.neo4jbackend.controller;

import cn.edu.tongji.neo4jbackend.dto.MovieInfoDTO;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.value.NullValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Math.min;
import static org.neo4j.driver.Values.parameters;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("neo4j/movie")
public class MovieController {
    private static final Pattern alphaPattern = Pattern.compile(".*[a-zA-Z]+.*");
    private final Driver driver;

    public MovieController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping()
    public ResponseEntity<HashMap<String, Object>> getMovieByName(
            @RequestParam(value = "movieName") String movieName,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            String sql = "match(n:Movie{movieName:$name}) return n limit " + (pageSize * pageNo);
            System.out.println(sql);
            Result res = session.run(sql, parameters("name", movieName));
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            //根据pageNo和pageSize进行分页
            int start = (pageNo - 1) * pageSize;
            int end = min(pageNo * pageSize, records.size());
            ArrayList<Map> movieList = new ArrayList<>();
            for (int i = start; i < end; i++) {
                movieList.add(records.get(i).get("n").asMap());
            }
            map.put("time", endTime - startTime);
            map.put("movies", movieList);
            return ResponseEntity.ok(map);
        }
    }

    //查找两个演员共同出演的电影
    @GetMapping("actors")
    public ResponseEntity<HashMap<String, Object>> getMovieByActors(String actor1, String actor2,
                                                                    @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                                    @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            String sql = "match(n:Actor{name:$actor1})-[r:Act|Star]->(m:Movie)<-[s:Act|Star]-(o:Actor{name:$actor2}) return m";
            System.out.println(sql);
            Result res = session.run(sql, parameters("actor1", actor1, "actor2", actor2));
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<Map> movieList = new ArrayList<>();
            int start = (pageNo - 1) * pageSize > records.size() ? 0 : (pageNo - 1) * pageSize;
            int end = min(pageNo * pageSize, records.size());
            map.put("totalMovie", records.size());
            records = records.subList(start, end);
            for (Record record : records) {
                movieList.add(record.get("m").asMap());
            }
            map.put("time", endTime - startTime);
            map.put("movies", movieList);
            return ResponseEntity.ok(map);
        }
    }

    @GetMapping("actor_director")
    public ResponseEntity<HashMap<String, Object>> getMovieByActorAndDirector(String actor, String director
            , @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo
            , @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            String sql = "match(n:Actor{name:$actor})-[r:Act]->(m:Movie)<-[s:Direct]-(o:Director{name:$director}) return m";
            System.out.println(sql);
            Result res = session.run(sql, parameters("actor", actor, "director", director));
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<Map> movieList = new ArrayList<>();
            int start = (pageNo - 1) * pageSize > records.size() ? 0 : (pageNo - 1) * pageSize;
            int end = min(pageNo * pageSize, records.size());
            map.put("totalMovie", records.size());
            records = records.subList(start, end);
            for (Record record : records) {
                movieList.add(record.get("m").asMap());
            }
            map.put("time", endTime - startTime);
            map.put("movies", movieList);
            return ResponseEntity.ok(map);
        }
    }

    @GetMapping("directorWorks")
    public ResponseEntity<HashMap<String, Object>> getMovieByDirector(String directorName) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            String sql = "match(n:Movie)<-[:Direct]-(d:Director{name:$name}) return n.movieName as movieName";
            System.out.println(sql);
            Result res = session.run(sql, parameters("name", directorName));
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<String> movieList = new ArrayList<>();
            //根据pageNo和pageSize进行分页
            for (Record record : records) {
                movieList.add(record.get("movieName").asString());
            }
            map.put("time", endTime - startTime);
            map.put("movies", movieList);
            map.put("director_name", directorName);
            map.put("movieNum", movieList.size());
            return ResponseEntity.ok(map);
        }
    }

    @GetMapping("actorWorks")
    public ResponseEntity<HashMap<String, Object>> getMovieByActor(String actorName) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            String sql = "match(n:Movie)<-[:Act]-(d:Actor{name:$name}) return n.movieName as movieName";
            String sql2 = "match(n:Movie)<-[:Star]-(d:Actor{name:$name}) return n.movieName as movieName";
            System.out.println(sql);
            Result res = session.run(sql, parameters("name", actorName));
            Result res2 = session.run(sql2, parameters("name", actorName));
            List<Record> records = res.list();
            List<Record> records2 = res2.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<String> movieList = new ArrayList<>();
            ArrayList<String> movieList2 = new ArrayList<>();
            //根据pageNo和pageSize进行分页
            for (Record record : records) {
                movieList.add(record.get("movieName").asString());
            }
            for (Record record : records2) {
                movieList2.add(record.get("movieName").asString());
            }
            map.put("time", endTime - startTime);
            map.put("actor_name", actorName);
            map.put("is_star_movies", movieList2);
            map.put("is_star_movieNum", movieList2.size());
            map.put("non_star_movies", movieList);
            map.put("non_star_movieNum", movieList.size());
            return ResponseEntity.ok(map);
        }
    }


    @RequestMapping(value = "score", method = RequestMethod.GET)
    public ResponseEntity<Map> getMovieByScores(
            @RequestParam(value = "score_floor", required = false, defaultValue = "0.0") float score_floor,
            @RequestParam(value = "score_ceiling", required = false, defaultValue = "5.0") float score_ceiling,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            String sql = "match (n:Movie) where n.movieScore>=" + score_floor + " and n.movieScore<=" + score_ceiling + " return n limit " + (pageSize * pageNo);
            String countSql = "match (n:Movie) where n.movieScore>=" + score_floor + " and n.movieScore<=" + score_ceiling + " return count(n)";
            Result countRes = session.run(countSql);
            System.out.println(countSql);
            System.out.println(sql);
            Result res = session.run(sql);
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            Record recordCount = countRes.single();
            //根据pageNo和pageSize进行分页
            int start = (pageNo - 1) * pageSize;
            int end = min(pageNo * pageSize, records.size());
            ArrayList<Map> movieList = new ArrayList<>();
            for (int i = start; i < end; i++) {
                movieList.add(records.get(i).get("n").asMap());
            }
            map.put("time", endTime - startTime);
            map.put("totalMovie", recordCount.get(0).asInt());
            map.put("movieList", movieList);
            return ResponseEntity.ok(map);
        }
    }

    @GetMapping("catagory")
    public ResponseEntity<HashMap<String, Object>> getCategory(String name) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            Long startTime = System.currentTimeMillis();
            Result res = session.run("match (n:Category{name:$name}) return n", parameters("name", name));
            Record record = res.single();
            map.put("catagory", record.get("n").asMap());
            Long endTime = System.currentTimeMillis();
            map.put("time", endTime - startTime);
            return ResponseEntity.ok(map);
        }
    }

    @GetMapping(value = "time/condition")
    public ResponseEntity<Map> getMovieNumByYearAndMonth(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "day", required = false) Integer day,
            @RequestParam(value = "season", required = false) Integer season,
            @RequestParam(value = "weekday", required = false) Integer weekday,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            String sql = "match (m:Movie) where ";
            Boolean flag = false;
            if (year != null) {
                sql += "m.year = " + year + " ";
                flag = true;
            }
            if (month != null) {
                if (flag) {
                    sql += "and ";
                }
                flag = true;
                sql += "m.month = " + month + " ";

            }
            if (day != null) {
                if (flag) {
                    sql += "and ";
                }
                flag = true;
                sql += "m.day = " + day + " ";
            }
            if (season != null) {
                if (flag) {
                    sql += "and ";
                }
                flag = true;
                sql += "m.season = " + season + " ";
            }
            if (weekday != null) {
                if (flag) {
                    sql += "and ";
                }
                flag = true;
                sql += "m.weekday = " + weekday + " ";
            }
            sql += "return m";
            Long startTime = System.currentTimeMillis();
            Result res = session.run(sql);
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            //根据pageNo和pageSize进行分页
            int start = (pageNo - 1) * pageSize;
            int end = min(pageNo * pageSize, records.size());
            ArrayList<Map> movieList = new ArrayList<>();
            for (int i = start; i < end; i++) {
                movieList.add(records.get(i).get("m").asMap());
            }
            map.put("time", endTime - startTime);
            map.put("totalMovie", records.size());
            map.put("movieList", movieList);
            return ResponseEntity.ok(map);
        }
    }

    @RequestMapping(value = "style", method = RequestMethod.GET)
    public ResponseEntity<Map> getMovieByStyles(
            @RequestParam(value = "style_1", required = false) String style_1,
            @RequestParam(value = "style_2", required = false) String style_2,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            if (style_1 == null && style_2 == null) {
                System.out.println("style_1 and style_2 are null");
                map.put("totalMovie", 0);
                map.put("movieList", new ArrayList<>());
                return ResponseEntity.ok(map);
            }
            String sql = "match (m:Movie)";
            if (style_1 != null) {
                sql += "-[r1:Belong]->(c1:Category{name:'" + style_1 + "'}) ";
            }
            if (style_2 != null) {
                if (style_1 != null) {
                    sql += ",";
                }
                sql += "(m:Movie)-[r2:Belong]->(c2:Category{name:'" + style_2 + "'}) ";
            }
            sql += " with m,count(m) as num return m,num skip " + (pageNo - 1) * pageSize + " limit " + pageSize;
            Long startTime = System.currentTimeMillis();
            System.out.println(sql);
            Result res = session.run(sql);
            List<Record> records = res.list();
            Long endTime = System.currentTimeMillis();
            ArrayList<Map> movieList = new ArrayList<>();
            for (Record record : records) {
                movieList.add(record.get("m").asMap());
            }
            map.put("time", endTime - startTime);
            map.put("totalMovie", records.get(0).get("num").asInt());
            map.put("movieList", movieList);
            return ResponseEntity.ok(map);
        }
    }

    @RequestMapping(value = "association/result", method = RequestMethod.POST)
    public ResponseEntity<HashMap<String, Object>> getMovieByMultipleConditions(
            @RequestBody MovieInfoDTO movieInfoDTO
    ) {
        try (Session session = driver.session()) {
            HashMap<String, Object> map = new HashMap<>();
            String match = "match (m:Movie)";
            String where = " where ";
            String returnStr = " return m";
            Boolean whereAppear = false;
            //电影类别查询
            if (movieInfoDTO.getStyle() != null) {
                match += ",(m)-[r1:Belong]->(:Category{name:'" + movieInfoDTO.getStyle() + "'}) ";
            }

            // 电影导演查询
            if (movieInfoDTO.getDirectorNames() != null) {
                for (String directorName : movieInfoDTO.getDirectorNames()) {
                    match += " ,(m)<-[:Direct]-(:Director{name:\"" + directorName + "\"})";
                }
            }

            //主演查询
            if (movieInfoDTO.getMainActors() != null) {
                for (String mainActor : movieInfoDTO.getMainActors()) {
                    match += " ,(m)<-[:Star]-(:Actor{name:\"" + mainActor + "\"})";
                }
            }

            //演员查询
            if (movieInfoDTO.getActors() != null) {
                for (String actor : movieInfoDTO.getActors()) {
                    match += " ,(m)<-[:Act]-(:Actor{name:\"" + actor + "\"})";
                }
            }
            //电影名称查询
            if (movieInfoDTO.getMovieName() != null) {
                where += "m.name =~ '.*" + movieInfoDTO.getMovieName() + ".*' ";
                whereAppear = true;
            }

            //按照分数最大最小值查找 需要给默认值 min默认0 max默认5
            if (movieInfoDTO.getMinScore() != null && movieInfoDTO.getMaxScore() != null) {
                if (movieInfoDTO.getMinScore() != null) {
                    if (whereAppear) {
                        where += " and ";
                    }
                    where += " m.movieScore >=" + movieInfoDTO.getMinScore() + " ";
                }
                whereAppear = true;
                if (movieInfoDTO.getMaxScore() != null) {
                    where += " and m.movieScore <=" + movieInfoDTO.getMaxScore() + " ";
                }
            }
            //按照正面评论比例查找 正面评价在positive之上
            if (movieInfoDTO.getPositive() != null) {
                float positive = movieInfoDTO.getPositive() / 100;
                if (whereAppear) {
                    where += " and ";
                }
                where += " m.positive >=" + movieInfoDTO.getPositive() + " ";
                whereAppear = true;
            }

            //按照日期查询
            if (movieInfoDTO.getMinDay() != null) {//由于前端是选择时间段，当这个参数不为空时，六个参数都不为空
                //获取最小日期的str
                String minDateStr = movieInfoDTO.getMinYear().toString() + "-" +
                        (movieInfoDTO.getMinMonth() < 10 ? "0" + movieInfoDTO.getMinMonth().toString() : movieInfoDTO.getMinMonth().toString()) +
                        "-" + (movieInfoDTO.getMinDay() < 10 ? "0" + movieInfoDTO.getMinDay().toString() : movieInfoDTO.getMinDay().toString());
                //获取最大日期的str
                String maxDateStr = movieInfoDTO.getMaxYear().toString() + "-" +
                        (movieInfoDTO.getMaxMonth() < 10 ? "0" + movieInfoDTO.getMaxMonth().toString() : movieInfoDTO.getMaxMonth().toString()) +
                        "-" + (movieInfoDTO.getMaxDay() < 10 ? "0" + movieInfoDTO.getMaxDay().toString() : movieInfoDTO.getMaxDay().toString());
                if (whereAppear) {
                    where += " and ";
                }
                where += " m.movieTime >='" + minDateStr + "' and m.movieTime <='" + maxDateStr + "' ";
                whereAppear = true;
            }
            //根据电影节点m查询电影的类别、导演、主演、演员
            int pageSize = movieInfoDTO.getPageSize();
            int pageNo = movieInfoDTO.getPageNo();
            String matchAll = "with m Optional MATCH (m)-[:Belong]->(c:Category) Optional MATCH (m)<-[:Direct]-(d:Director) Optional MATCH (m)<-[:Star]-(s:Actor) Optional MATCH (m)<-[:Act]-(a:Actor) ";
            returnStr += ",collect(distinct c.name) as style,collect(distinct d.name) as director,collect(distinct s.name) as mainActor,collect(distinct a.name) as actor skip " + (pageNo - 1) * pageSize + " limit " + pageSize;
            //查询完毕
            String sql = match + (whereAppear ? where : "") + matchAll + returnStr;
            String sqlCount = match + (whereAppear ? where : "") + " return count(distinct(m))";
            System.out.println(sql);
            System.out.println(sqlCount);
            long startTime = System.currentTimeMillis();
            Result res = session.run(sql);
            Result resCount = session.run(sqlCount);
            List<Record> records = res.list();
            Record recordCount = resCount.single();
            long endTime = System.currentTimeMillis();
            map.put("totalMovieNum", recordCount.get(0).asInt());
            ArrayList<HashMap<String, Object>> movieResult = new ArrayList<>();
            for (Record record : records) {
                HashMap<String, Object> movieNode = new HashMap<>();
                if (record.get(0).get("movieName") != NullValue.NULL) {
                    movieNode.put("movieName", record.get(0).get("movieName").asString());
                }
                if (record.get(0).get("movieScore") != NullValue.NULL) {
                    movieNode.put("score", record.get(0).get("movieScore").asDouble());
                }
                if (record.get(0).get("movieTime") != NullValue.NULL) {
                    movieNode.put("movieTime", record.get(0).get("movieTime").asString());
                }
                if (record.get(0).get("formatNum") != NullValue.NULL) {
                    movieNode.put("formatNum", record.get(0).get("formatNum").asInt());
                }
                if (record.get(0).get("reviewNum") != NullValue.NULL) {
                    movieNode.put("reviewNum", record.get(0).get("reviewNum").asInt());
                }
                if (record.get(0).get("positive") != NullValue.NULL) {
                    movieNode.put("positive", record.get(0).get("positive").asDouble());
                }
                movieNode.put("movieStyle", record.get(1).asList());
                movieNode.put("director", record.get(2).asList());
                movieNode.put("mainActor", record.get(3).asList());
                movieNode.put("actor", record.get(4).asList());
                movieResult.add(movieNode);
            }
            map.put("movies", movieResult);
            map.put("time", endTime - startTime);
            return ResponseEntity.ok(map);
        }
    }

    @RequestMapping(value = "association/movieName", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getMovieNameListByString(
            @RequestParam(value = "movieName") String movieName
    ) {
        try (Session session = driver.session()) {
            String sql = "match (m:Movie) where ";
            if (movieName == null || !alphaPattern.matcher(movieName).matches()) {
                sql += "m.name =~ '.*A.*' return m.name limit 15";
            } else {
                sql += "m.name =~ '.*" + movieName + ".*' return m.name limit 15";
            }
            Result res = session.run(sql);
            List<Record> records = res.list();
            ArrayList<String> movieNameList = new ArrayList<>();
            for (Record record : records) {
                movieNameList.add(record.get(0).asString());
            }
            return ResponseEntity.ok(movieNameList);
        }
    }

    @RequestMapping(value = "association/actorName", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getActorNameListByString(
            @RequestParam(value = "actorName") String actorName,
            @RequestParam(value = "isStar") boolean isStar
    ) {
        try (Session session = driver.session()) {
            //根据isStar判断是主演还是演员
            String relation = isStar ? "Star" : "Act";
            String sql = "match (m:Movie)<-[r:" + relation + "]-(a:Actor) where ";
            if (actorName == null || !alphaPattern.matcher(actorName).matches()) {
                sql += "m.name =~ '.*A.*' return m.name limit 15";
            } else {
                sql += "m.name =~ '.*" + actorName + ".*' return m.name limit 15";
            }
            Result res = session.run(sql);
            List<Record> records = res.list();
            ArrayList<String> movieNameList = new ArrayList<>();
            for (Record record : records) {
                movieNameList.add(record.get(0).asString());
            }
            return ResponseEntity.ok(movieNameList);
        }
    }

    @RequestMapping(value = "association/directorName", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getDirectorNameListByString(
            @RequestParam(value = "directorName") String directorName
    ) {
        try (Session session = driver.session()) {
            //根据isStar判断是主演还是演员
            String sql = "match (m:Movie)<-[r:Direct]-(a:Director) where ";
            if (directorName == null || !alphaPattern.matcher(directorName).matches()) {
                sql += "m.name =~ '.*A.*' return m.name limit 15";
            } else {
                sql += "m.name =~ '.*" + directorName + ".*' return m.name limit 15";
            }
            Result res = session.run(sql);
            List<Record> records = res.list();
            ArrayList<String> movieNameList = new ArrayList<>();
            for (Record record : records) {
                movieNameList.add(record.get(0).asString());
            }
            return ResponseEntity.ok(movieNameList);
        }
    }
}
