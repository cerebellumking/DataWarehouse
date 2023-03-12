from flask import Flask, request,jsonify
from flask_cors import CORS
from pyspark import SparkConf
from pyspark.sql import SparkSession
from pyspark.sql import Row
from pyspark.sql.functions import *
from pyspark.sql.types import *
# from pyspark.sql.types import StructType
# from pyspark.sql.types import StructField
# from pyspark.sql.types import StringType
# from pyspark.sql.types import IntegerType
# from pyspark.sql.types import LongType
import time
import pandas as pd
import json
from util import transformData
app = Flask(__name__)
CORS(app)

def getSpark():
    spark = SparkSession.builder.appName("SparkSQL")\
        .config("hive.metastore.uris","thrift://localhost:9083")\
        .config("spark.driver.memory", '2G')\
        .config("spark.executor.memory", '2G')\
        .enableHiveSupport()\
        .master("local[*]")\
        .getOrCreate()
    return spark

# SparkSql 访问hive数仓
@app.route('/hive/movie', methods=['GET'])
def getMovieNumByTime():
    # 计算程序运行时间
    startTime = time.time()
    spark = getSpark()
    print("成功连接hive")
    sql = "select * from test_amazon.time where year=" + request.args.get('year')
    df = spark.sql(sql)
    df.show()
    result = df.count() 
    endTime = time.time()
    runTime = endTime - startTime
    print("程序运行时间：", runTime)
    return str(result)

#注意返回limit，不要查崩就行

#根据电影名查询详细信息
@app.route('/hive/movie_name', methods=['GET'])
def getMovieInfoByName():
    # 计算程序运行时间
    startTime = time.time()
    returnValue = {
            'status':500,
            'data':None
        }
    try:
        spark = getSpark()
        print("成功连接hive")
        #查询部分
        sql = "select * \
            from amazon.movie\
            where movie_name=" + "\"" +request.args.get('movieName')+"\""
        df = spark.sql(sql)
        df.show()
        num = df.count() 
        print("查询个数为",num)
        print(type(df))
        df = df.toPandas()
        resultJson = df.to_json(orient="index",force_ascii=False)
        print(type(resultJson))
        endTime = time.time()
        runTime = endTime - startTime
        returnValue = {
            "status":200,
            "data":resultJson,  #法二： df.toJSON().first()
            "time":int(runTime)
        }
        
        print("程序运行时间：", runTime * 1000)
    except Exception as e:
        print(str(e))
        returnValue ={
            "status":200,
            "data":-1
        }
    return jsonify(returnValue)

#根据导演名查询电影数目和名称
@app.route('/hive/directorWorks', methods=['GET'])
def getMovieNumAndNameByDirector():
    # 计算程序运行时间
    startTime = time.time()
    returnValue = {
            'status':500,
            'data':None
        }
    try:
        spark = getSpark()
        print("成功连接hive")
        #查询部分
        sql = "select movie_name \
            from amazon.movie\
            where array_contains(directors,\"" + request.args.get('directorName') + "\")"
        df = spark.sql(sql)
        df.show()
        movies_num = df.count()
        director_name = request.args.get('directorName') 
        print("查询个数为",movies_num)
        df = df.toPandas().to_dict(orient='records')
        resultJson = {
            "movie_num":movies_num,
            "movies":df,
            "director_name":director_name
        }
        endTime = time.time()
        runTime = endTime - startTime
        returnValue = {
            "status":200,
            "data":resultJson,  #法二： df.toJSON().first()
            "time":int(runTime * 1000)
        }
        
        print("程序运行时间：", runTime * 1000)
    except Exception as e:
        print(str(e))
        returnValue ={
            "status":200,
            "data":-1
        }
    return jsonify(returnValue)

#根据演员名查询主演和参演电影
@app.route('/hive/actorWorks', methods=['GET'])
def getMovieByActor():
    # 计算程序运行时间
    startTime = time.time()
    returnValue = {
            'status':500,
            'data':None
        }
    try:
        spark = getSpark()
        print("成功连接hive")
        #查询部分
        actor_name = request.args.get('actorName')
        not_star_sql = "select movie_name \
            from amazon.movie\
            where array_contains(actors,\"" + actor_name + "\")"  #加上引号，补足前端发的不足
        not_star_df = spark.sql(not_star_sql)
        not_star_movieNum = not_star_df.count()

        print("not_star查询个数为",not_star_movieNum)
        not_star_df = not_star_df.toPandas().values.tolist()
        
        star_sql = "select movie_name \
            from amazon.movie\
            where array_contains(stars,\"" + actor_name + "\")"
        star_df = spark.sql(star_sql)
        star_movieNum = star_df.count()

        print("star查询个数为",star_movieNum)
        star_df = star_df.toPandas().to_dict(orient='records')
        
        resultJson = {
            "actor_name":actor_name,
            "is_star_movieNum":star_movieNum,
            "is_star_movies":star_df,
            "not_star_movieNum":not_star_movieNum,
            "not_star_movies":not_star_df,
        }
        endTime = time.time()
        runTime = endTime - startTime
        returnValue = {
            "status":200,
            "data":resultJson,  #法二： df.toJSON().first()
            "time":int(runTime * 1000)
        }
    except Exception as e:
        print(str(e))
        returnValue ={
            "status":200,
            "data":-1
        }
    return jsonify(returnValue)

@app.route('/hive/movie/association/result', methods=['POST'])
def getMovieAssociation():
    #接收post参数
    data = request.get_data()
    data = json.loads(data)
    sql = "select * from amazon.movie where"
    whereAppear = False
    if(data.get('movieName') != None):
        sql += " movie_name = \"" + data.get('movieName') + "\""
        whereAppear = True
    if(data.get("style")!=None):
        if(whereAppear):
            sql += " and"
        sql += " array_contains(styles,\"" + data.get('style') + "\")"
        whereAppear = True
    if(data.get('directorNames') != None):
        directors = data.get('directorNames')
        for director in directors:
            if(whereAppear):
                sql += " and array_contains(directors,\"" + director + "\")"
            else:
                sql += "  array_contains(directors,\"" + director + "\")"
                whereAppear = True
    if(data.get('actors') != None):
        actors = data.get('actors')
        for actor in actors:
            if(whereAppear):
                sql += " and array_contains(actors,\"" + actor + "\")"
            else:
                sql += "  array_contains(actors,\"" + actor + "\")"
                whereAppear = True
    if(data.get('mainActors') != None):
        stars = data.get('mainActors')
        for star in stars:
            if(whereAppear):
                sql += " and array_contains(stars,\"" + star + "\")"
            else:
                sql += "  array_contains(stars,\"" + star + "\")"
                whereAppear = True
    if(data.get("minYear") != None):
        minYear = data.get("minYear")
        maxYear = data.get("maxYear")
        minMonth = int(data.get("minMonth"))
        maxMonth = int(data.get("maxMonth"))
        minDay = int(data.get("minDay"))
        maxDay = int(data.get("maxDay"))
        if minMonth < 10:
            minMonth = "0" + str(minMonth)
        if maxMonth < 10:
            maxMonth = "0" + str(maxMonth)
        if minDay < 10:
            minDay = "0" + str(minDay)
        if maxDay < 10:
            maxDay = "0" + str(maxDay)
        minDateStr = str(minYear) + "-" + str(minMonth) + "-" + str(minDay)
        maxDateStr = str(maxYear) + "-" + str(maxMonth) + "-" + str(maxDay)
        if(whereAppear):
            sql += " and movie_time >= \"" + minDateStr + "\" and movie_time <= \"" + maxDateStr + "\""
        else:
            sql += " movie_time >= \"" + minDateStr + "\" and movie_time <= \"" + maxDateStr + "\""
            whereAppear = True
    if(data.get("minScore") != None):
        minScore = data.get("minScore")
        maxScore = data.get("maxScore")
        if(whereAppear):
            sql += " and movie_score >= " + str(minScore) + " and movie_score <= " + str(maxScore)
        else:
            sql += " movie_score >= " + str(minScore) + " and movie_score <= " + str(maxScore)
            whereAppear = True
    sql += " limit 10"
    spark = getSpark()
    startTime = time.time()
    df = spark.sql(sql)
    count = df.count()
    dfPandas = df.toPandas()
    endTime = time.time()
    movie_df =dfPandas.to_dict(orient='records')
    return jsonify({
        "totalMovieNum":count,
        "movies":movie_df,
        "time": int((endTime - startTime) *1000)
    })

@app.route('/hive/relation/actor_actor', methods=['GET'])
def getMostCoopActorActor():
    limit = request.args.get('limit')
    sql = "select * from amazon.actor_actor_coop_time limit " + limit
    startTime = time.time()
    spark = getSpark()
    df = spark.sql(sql)
    dfPandas = df.toPandas()
    endTime = time.time()
    df = dfPandas.to_dict(orient='records')
    return jsonify({
        "data":df,
        "time": int((endTime - startTime) *1000)
    })

@app.route('/hive/relation/actor_director', methods=['GET'])
def getMostCoopActorDirector():
    limit = request.args.get('limit')
    sql = "select * from amazon.actor_director_coop_time limit " + limit
    startTime = time.time()
    spark = getSpark()
    df = spark.sql(sql)
    dfPandas = df.toPandas()
    endTime = time.time()
    df = dfPandas.to_dict(orient='records')
    return jsonify({
        "data":df,
        "time": int((endTime - startTime) *1000)
    })

@app.route('/hive/movie/score', methods=['GET'])
def getMovieByScore():
    score_floor = float(request.args.get('score_floor'))
    score_ceiling = float(request.args.get('score_ceiling'))
    pageNo = int(request.args.get('pageNo'))
    pageSize = int(request.args.get('pageSize'))
    sql = "select * from amazon.movie where movie_score >= " + str(score_floor) + " and movie_score <= " + str(score_ceiling)
    spark = getSpark()
    startTime = time.time()
    df = spark.sql(sql)
    count = df.count()
    dfPandas = df.toPandas()
    endTime = time.time()
    if(pageNo * pageSize > count):
        return jsonify({
            "totalMovieNum":count,
            "movies":[],
            "time": int((endTime - startTime) *1000)
        })
    dfPandas = dfPandas.iloc[(pageNo - 1) * pageSize:pageNo * pageSize]
    movie_df =dfPandas.to_dict(orient='records')
    return jsonify({
        "totalMovieNum":count,
        "movies":movie_df,
        "time": int((endTime - startTime) *1000)
    })

@app.route('/hive/movie/style', methods=['GET'])
def getMovieByStyle():
    pageNo = int(request.args.get('pageNo'))
    pageSize = int(request.args.get('pageSize'))
    sql = "select * from amazon.movie where "
    flag = False
    if request.args.get('style_1') != None:
        flag = True
        style1 = request.args.get('style_1')
        sql += "array_contains(styles,\"" + style1 + "\")"
    if request.args.get('style_2') != None:
        style2 = request.args.get('style_2')
        if flag:
            sql += " and"
        sql += " array_contains(styles,\"" + style2 + "\")"
    spark = getSpark()
    startTime = time.time()
    df = spark.sql(sql)
    count = df.count()
    dfPandas = df.toPandas()
    endTime = time.time()
    if(pageNo * pageSize > count):
        return jsonify({
            "totalMovieNum":count,
            "movies":[],
            "time": int((endTime - startTime) *1000)
        })
    dfPandas = dfPandas.iloc[(pageNo - 1) * pageSize:pageNo * pageSize]
    movie_df =dfPandas.to_dict(orient='records')
    return jsonify({
        "totalMovieNum":count,
        "movies":movie_df,
        "time": int((endTime - startTime) *1000)
    })

@app.route('/hive/movie/time/condition', methods=['GET'])
def getMovieByTime():
    print(request.args)
    pageNo = int(request.args.get('pageNo'))
    pageSize = int(request.args.get('pageSize'))
    sql = "select * from amazon.movie where "
    flag = False
    if request.args.get('year') != None:
        flag = True
        year = int(request.args.get('year'))
        sql += "year=" + str(year)
    if request.args.get('month') != None:
        month = int(request.args.get('month'))
        if flag:
            sql += " and"
        sql += " month=" + str(month)
        flag = True
    if request.args.get('day') != None:
        day = int(request.args.get('day'))
        if flag:
            sql += " and"
        sql += " day=" + str(day)
    if request.args.get('season') != None:
        season = int(request.args.get('season'))
        if flag:
            sql += " and"
        sql += " season=" + str(season)
    if request.args.get('weekday') != None:
        weekday = int(request.args.get('weekday'))
        if flag:
            sql += " and"
        sql += " weekday=" + str(weekday)
    spark = getSpark()
    startTime = time.time()
    df = spark.sql(sql)
    count = df.count()
    dfPandas = df.toPandas()
    endTime = time.time()
    if(pageNo * pageSize > count):
        return jsonify({
            "totalMovieNum":count,
            "movies":[],
            "time": int((endTime - startTime) *1000)
        })
    dfPandas = dfPandas.iloc[(pageNo - 1) * pageSize:pageNo * pageSize]
    movie_df =dfPandas.to_dict(orient='records')
    return jsonify({
        "totalMovieNum":count,
        "movies":movie_df,
        "time": int((endTime - startTime) *1000)
    })



if __name__ == '__main__':
    app.run(host = '0.0.0.0', port = 8084, debug = True)

