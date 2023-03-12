
<template>
  <div class="app-container">
    <el-row>
      <el-col :span="12">
        <el-form>
          <el-divider content-position="left">非电影数据获取</el-divider>
          <el-form-item>
            <el-button round @click="getNoFilmData"
              >获取所有非电影数据</el-button
            >
          </el-form-item>
          <el-divider content-position="left">电影溯源</el-divider>
          <el-form-item label="需溯源的电影名">
            <el-autocomplete
              v-model="traceMovieFuzzyName"
              :fetch-suggestions="movieSearchSuggest"
              placeholder="请输入内容"
              style="width: 20vw;"
              clearable
              @select="handleSelect"
            />
          </el-form-item>



          <p v-if="hasDetailFilmResult">
              共有{{ traceMovieNumber }}个结果
          </p>


          <el-form-item v-if="hasDetailFilmResult" label="需溯源的具体电影名">
            <el-select
              v-model="detailFilmName"
              filterable
              clearable
              placeholder="请选择电影名"
            >
              <el-option
                v-for="item in detailMovieList"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="traceBackMovie">查询</el-button>
            <el-button @click="onCancel">取消</el-button>
          </el-form-item>
          

        </el-form>

      </el-col>
      <el-col :span="1">
        <el-divider direction="vertical" />
      </el-col>
      <el-col :span="10">
        <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane label="查询结果" name="first">
            <p v-if="hasDetailFilmResult ">
              共有{{ movieNumber }}个结果
            </p>
            <el-table v-loading="movieLoading" height="460" border stripe :data="movieData" style="width: 100%">
              <el-table-column type="expand">
                <template slot-scope="props">
                  <el-form label-position="left" inline class="demo-table-expand">
                    <!-- <el-form-item>
                        <el-image src="https://m.media-amazon.com/images/I/81nbTAZ-p3S._SL1500_.jpg"
                        style="width: 30%;">
                        </el-image>
                        </el-form-item> -->
                    <el-form-item label="名称">
                      <span>{{ props.row.name }}</span>
                    </el-form-item>
                    <el-form-item label="上映时间">
                      <span>{{ props.row.release_time }}</span>
                    </el-form-item>
                    <el-form-item label="导演">
                      <span>{{ props.row.directors }}</span>
                    </el-form-item>
                    <el-form-item label="主演">
                      <span>{{ props.row.stars }}</span>
                    </el-form-item>
                    <el-form-item label="演员">
                      <span>{{ props.row.actors }}</span>
                    </el-form-item>
                    <el-form-item label="风格">
                      <span>{{ props.row.style }}</span>
                    </el-form-item>
                    <el-form-item label="格式">
                      <span>{{ props.row.format }}</span>
                    </el-form-item>
                  </el-form>
                </template>
              </el-table-column>
              <el-table-column prop="asin" label="编号" />
              <el-table-column prop="name" label="名称" width="250" />


            </el-table>
            <div>
              <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="pageNo"
              :page-sizes="[5,10,15,20]"
              :page-size="pageSize" layout = "total,sizes,prev,pager,next,jumper"
              :total="movieNumber" >
              </el-pagination>
            </div>

          </el-tab-pane>


        </el-tabs>

      </el-col>
    </el-row>
    
  </div>
</template>

<script>
import 'echarts/lib/component/title'
/* eslint-disable */
export default {
  filters: {

  },
  data() {
    return {
      isNoFilmQuery:false,
      traceMovieFuzzyName:'Harry',
      hasDetailFilmResult:false,
      detailFilmName:'',
      detailMovieList:[],
      pageSize:10,
      pageNo:1,
      traceMovieNumber:0,
      form: {
        movieName: '',
        region: '',
        delivery: false,
        type: [],
        resource: '',
        desc: '',
        style:'',
        movieDirectors:[],
        movieMainActors:[],
        movieActors:[],
        actorName:'',
        movieMinScore:0,
        movieMaxScore:5.0,
        movieDate:[],
        positive:0,
      },
      percentageColors: [
          {color: '#f56c6c', percentage: 20},
          {color: '#e6a23c', percentage: 40},
          {color: '#5cb87a', percentage: 60},
          {color: '#1989fa', percentage: 80},
          {color: '#6f7ad3', percentage: 100}
        ],
      movieLoading:false,
      labelColor:["#77C9D4","#57BC90","#015249"],
      pickerOptions: {
          disabledDate(time) {
            return time.getTime() > Date.now();
          },
          shortcuts: [{
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近一个月',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit('pick', [start, end]);
            }
          }, {
            text: '最近三个月',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              picker.$emit('pick', [start, end]);
            }
          },{
            text: '最近半年',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 182);
              picker.$emit('pick', [start, end]);
            }
          },
          {
            text: '最近一年',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 365);
              picker.$emit('pick', [start, end]);
            }
          },
          {
            text: '最近三年',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 365 * 3);
              picker.$emit('pick', [start, end]);
            }
          },
        ]
      },

      // 速度比较图
      vchartsConfig: {
        setting:{
          // 别称
          labelMap: {
            'type': '数据库',
            'software': '软件',
            'speed': '速度',
          },
        },
        extend: {
          title:{
            show: true,
            text:'检索电影',
            subtext:'通过关系型数据库MySql、分布式数据库Hive和图数据库Neo4j分别检索电影的速度对比',
            // textAlign:'center',
          },
          // 图标顶部的标题及按钮
          legend:{
            show:false,
          },
          // backgroundColor:'red',//整个组件的背景颜色
          //X轴线
          xAxis: {
            // name: "数据库类型",
            // type:'style',
            show:true,
            // 坐标轴轴线
            axisLine:{
              show:false,
            },
            // 坐标轴刻度

            // 坐标轴每项的文字
            axisLabel:{
              showMaxLabel:true,
              showMinLabel:true,
              color:'#708090',
              rotate:0, //刻度文字旋转，防止文字过多不显示
              margin:8,//文字离x轴的距离
              boundaryGap:true,
              // backgroundColor:'#0f0',
              formatter:(v)=>{
                // console.log('x--v',v)
                return v
              },
            },
            // X轴下面的刻度小竖线
            axisTick:{
              show:false,
              alignWithLabel:true,//axisLabel.boundaryGap=true时有效
              interval:0,
              length:4,//长度
            },
            // x轴对应的竖线
            splitLine: {
              show: false,
              interval: 0,
              lineStyle:{
                color:'red',
                backgroundColor:'red',
              }
            }
          },
          yAxis: {
            show:true,
            offset:0,
            // 坐标轴轴线
            axisLine:{
              show:false,
            },
            // x轴对应的竖线
            splitLine: {
              show: true,
            },
            // 坐标轴刻度
            axisTick:{
              show:false,
            },
            boundaryGap:true,
            axisLabel:{
              color:'#708090',
              formatter:(v)=>{
                if (v==0){
                  return v;
                }
                return v+' ms'
            },
            },

          },

          // 滚动组件参数
          dataZoom:[
            {
              type: 'inside',
              show: true,
              xAxisIndex: [0],
              startValue: 0,
              endValue: 4,
              zoomLock:true,//阻止区域缩放
            }
          ],



          // 每个柱子
          series(v) {
            // console.log("v", v);
            // 设置柱子的样式
            v.forEach(i => {
              console.log("series", i);
              i.barWidth = 30;
              i.itemStyle={
                barBorderRadius:[15,15,0,0],
                borderWidth:20,
              };
              i.label={
                color:'#666',
                show:true,
                position:'top',
                // backgroundColor:'yellow',
              };
              i.color = '#00bfff'

            });
            return v;
          },
        }
      },
      // v-chats列表数据
      chartData: {
        columns: ["type","speed"],
        rows: [
          { "type":"关系型数据库","software": "mysql", "speed": 0 },
          { "type":"分布式数据库","software": "hive", "speed": 0 },
          { "type":"图数据库","software": "neo4j", "speed": 0 },
        ],
      },

      directorInputVisible:false,
      directorInputValue:'',
      mainActorInputVisible:false,
      mainActorInputValue:'',
      actorInputVisible:false,
      actorInputValue:'',
      activeName: 'first',
      searchText:'暂无查询',
      BASE_URL:'http://1.117.102.181:8101',
      NEO4J_BASE_URL:'http://49.235.72.134:8087',
      HIVE_BASE_URL:'http://106.15.204.216:8084',
      movieData:[],

      categoryLoading:false,
      movieStyle:['Romance','Comedy','Action','ScienceFiction','Animation','Crime','Mystery& Thrillers'
      ,'Suspence','Adventure','War','Biography','Horror','Kids& Family'],
      movieNumber:0,

      relationReady:false,
      distributeReady:false,
      graphReady:false,
      }
  },
  created() {

  },
  methods: {
    onCancel() {
      this.$message({
        message: 'cancel!',
        type: 'warning'
      })
    },
    handleSelect(item) {
      // this.hasDetailFilmResult = true;
      this.pageNo = 1
      this.pageSize = 10
      var axios = require('axios');
      var config = {
        method: 'get',
        url: this.NEO4J_BASE_URL+'/neo4j/trace/fuzzy',
        params:{"name":this.traceMovieFuzzyName},
        headers: { 
        }
      };
      axios(config)
      .then(response => {
        this.detailMovieList = []
        for(let i=0;i<response.data.movieList.length;++i){
          if (response.data.movieList[i].includes('Game'))
          {
            console.log(response.data.movieList[i])
            continue;
          }
          this.detailMovieList.push(response.data.movieList[i])
        }
        if(this.detailMovieList.length > 0)
        {
          this.hasDetailFilmResult = true
          this.traceMovieNumber = this.detailMovieList.length
        }
      })
      .catch(function(error){
        console.log(error)
      })
      console.log(item);
    },
    handleSizeChange(val){
      this.pageSize = val;
      console.log(this.pageSize);
      if(this.isNoFilmQuery)
      {
        this.getNoFilmData()
      }
      else
      {
        this.traceBackMovie()
      }
    },
    handleCurrentChange(val){
      this.pageNo = val;
      console.log(this.pageNo);
      if(this.isNoFilmQuery)
      {
        this.getNoFilmData()
      }
      else
      {
        this.traceBackMovie()
      }
    },
    handleDirectorSelect(item) {
      this.handleDirectorInputConfirm(false)
    },
    handleMainActorSelect(item){
      this.handleMainActorInputConfirm()
    },
    handleActorSelect(item){
      this.handleActorInputConfirm()
    },
    /**
     * 下面是搜索建议的函数
     **/
    movieSearchSuggest(queryString, cb){
      var axios = require('axios');

      var config = {
        method: 'get',
        url: this.BASE_URL+'/mysql/movie/association/movieName',
        params:{"movieName":queryString},
        headers: { }
      };

      // 向mysql 发送请求
      axios(config)
      .then(response=> {
        console.log(response.data)
        var result=[]
        for(let i=0;i<response.data.length;++i){
          result.push({"value":response.data[i]})
        }
        cb(result);
      })
      .catch(function (error) {
        this.$message.error('当前网络异常，请稍后再试');
      });
    },
    directorSearchSuggest(queryString, cb){
      var axios = require('axios');

      var config = {
        method: 'get',
        url: this.BASE_URL+'/mysql/movie/association/directorName',
        params:{"directorName":queryString},
        headers: { }
      };

      // 向mysql 发送请求
      axios(config)
      .then(response=> {
        var result=[]
        for(let i=0;i<response.data.length;++i){
          result.push({"value":response.data[i]})
        }
        
        cb(result);
      })
      .catch(function (error) {
        this.$message.error('当前网络异常，请稍后再试');
      });
    },
    mainActorSearchSuggest(queryString,cb){
      var axios = require('axios');

      var config = {
        method: 'get',
        url: this.BASE_URL+'/mysql/movie/association/actorName',
        params:{"actorName":queryString,"isStar":1},
        headers: { }
      };

      // 向mysql 发送请求
      axios(config)
      .then(response=> {
        var result=[]
        for(let i=response.data.length-1;i>=0;--i){
          if(result.length>=25){
            break
          }
          result.push({"value":response.data[i]})
        }
        cb(result);
      })
      .catch(function (error) {
        this.$message.error('当前网络异常，请稍后再试');
      });

    },
    actorSearchSuggest(queryString, cb){
      var axios = require('axios');

      var config = {
        method: 'get',
        url: this.BASE_URL+'/mysql/movie/association/actorName',
        params:{"actorName":queryString,"isStar":0},
        headers: { }
      };

      // 向mysql 发送请求
      axios(config)
      .then(response=> {
        var result=[]
        for(let i=response.data.length-1;i>=0;--i){
          if(result.length>=25){
            break
          }
          result.push({"value":response.data[i]})
        }
        cb(result);
      })
      .catch(function (error) {
        this.$message.error('当前网络异常，请稍后再试');
      });
    },
    handleClick(tab, event) {
      console.log(tab, event);
    },

    decrease(){
      this.form.positive=this.form.positive>0?this.form.positive-1:this.form.positive
    },
    increase(){
      this.form.positive=this.form.positive<100?this.form.positive+1:this.form.positive
    },

    movieDataToString(data) {
      if (data.length == 0) {
        return ""
      }
      var temp = "asin,title,edition,format,time,director,mainActor,actor,score,commentNum\n"
      for (let j = 0; j < data.length; j++) {
        let i = data[j]
        temp += i.asin + ','
        temp += i.title + ','
        if (i.hasOwnProperty("edition")){
          temp+=i.edition+','
        }
        else{
          temp+=','
        }

        if (i.hasOwnProperty("format")){
          temp+=i.format+','
        }
        else{
          temp+=','
        }
        if (i.hasOwnProperty("time")){
          temp+=i.time+','
        }
        else{
          temp+=','
        }
        if (i.director.length!=0){
          temp+=String(i.director)+','
        }
        else{
          temp+=','
        }
        if (i.mainActor.length!=0){
          temp+=String(i.mainActor)+','
        }
        else{
          temp+=','
        }
        if (i.actor.length!=0){
          temp+=String(i.actor)+','
        }
        else{
          temp+=','
        }
        if (i.hasOwnProperty("score")){
          temp+=i.score+','
        }
        else{
          temp+=','
        }
        if (i.hasOwnProperty("commentNum")){
          temp+=i.commentNum+','
        }
        else{
          temp+=','
        }
        temp += '\n'
      }
      return temp;
    },

    traceBackMovie(){
      // 清空上一轮查询结果
      this.clearResult();
      console.log(this.detailFilmName)
      this.isNoFilmQuery = false
      var axios = require('axios')
      var config = {
        method: 'get',
        url: this.NEO4J_BASE_URL+'/neo4j/trace/movie',
        params:{"name":this.detailFilmName},
        headers: { 
        }
      };
      
      axios(config).then(response=>{
        console.log("neo4j filmTraceBackdata")
        console.log(response)
        var movieList = []
        for(let i=0;i< response.data.movieList.length;i++)
        {
          if (response.data.movieList[i].name.includes('Game'))
          {
            console.log(response.data.movieList[i].name)
            continue;
          }
          movieList.push(response.data.movieList[i])
        }
        if(response.data.movieList.length>0){
          this.movieData = movieList.slice((this.pageNo-1)*this.pageSize,this.pageNo*this.pageSize)
          console.log(this.movieData)
          this.movieNumber = response.data.movieList.length
          this.hasDetailFilmResult = true
          this.movieLoading = false
        }
      })


      

    },
    getNoFilmData()
    {
      this.clearResult()
      this.isNoFilmQuery = true
      var axios = require('axios');
      axios({
        method: 'get',
        url: this.NEO4J_BASE_URL+'/neo4j/trace/non_movie',
        data:{'pageNo':this.pageNo,'pageSize':this.pageSize},
        headers: { }
      }).then(response=>{
        console.log("neo4j nofilmdata")
        console.log(response.data)
        this.movieData = response.data.movieList
        this.movieNumber = response.data.totalMovie
      })


    },
    clearResult(){
      this.movieData.splice(0,this.movieData.length)
      this.hasDetailFilmResult=false
      this.movieLoading=false
      for(let i=0;i<3;++i){
        this.chartData.rows[i].speed=0
      }
      this.searchText="暂无查询"
    },

    /**
     * 下面是处理标签的函数
     **/
    showDirectorInput() {
      this.directorInputVisible = true;
      this.$nextTick(_ => {
        this.$refs.saveDirectorTagInput.$refs.input.focus();
      });
    },

    handleDirectorInputConfirm(showMessage) {
      let inputValue = this.directorInputValue
      // 有效性判断
      if (!inputValue || inputValue.replace(/\s*/g,"").length==0) {
        if(!this.directorInputVisible){
          return;
        }
        if(showMessage){
            this.$message({
            message: '请输入有效的导演名称！',
            type: 'warning'
          })
          this.directorInputVisible=false;
        }

        return;
      }
      this.form.movieDirectors.push(inputValue.replace(/^\s*|\s*$/g,""));
      this.directorInputVisible = false;
      this.directorInputValue = '';
    },
    handleDirectorTagClose(tag) {
      this.form.movieDirectors.splice(this.form.movieDirectors.indexOf(tag), 1);
    },
    showMainActorInput() {
      this.mainActorInputVisible = true;
      this.$nextTick(_ => {
        this.$refs.saveMainActorTagInput.$refs.input.focus();
      });
    },
    handleMainActorInputConfirm() {
      let inputValue = this.mainActorInputValue
      // 有效性判断
      if (!inputValue || inputValue.replace(/\s*/g,"").length==0) {
        if(!this.mainActorInputVisible){
          return;
        }
        this.$message({
          message: '请输入有效的主演名称！',
          type: 'warning'
        })
        this.mainActorInputVisible=false;
        return;
      }
      this.form.movieMainActors.push(inputValue.replace(/^\s*|\s*$/g,""));
      this.mainActorInputVisible = false;
      this.mainActorInputValue = '';
    },
    handleMainActorTagClose(tag) {
      this.form.movieMainActors.splice(this.form.movieMainActors.indexOf(tag), 1);
    },

    showActorInput() {
      this.actorInputVisible = true;
      this.$nextTick(_ => {
        this.$refs.saveActorTagInput.$refs.input.focus();
      });
    },
    handleActorInputConfirm() {
      let inputValue = this.actorInputValue
      // 有效性判断
      if (!inputValue || inputValue.replace(/\s*/g,"").length==0) {
        if(!this.actorInputVisible){
          return;
        }
        this.$message({
          message: '请输入有效的演员名称！',
          type: 'warning'
        })
        this.actorInputVisible=false;
        return;
      }
      this.form.movieActors.push(inputValue.replace(/^\s*|\s*$/g,""));
      this.actorInputVisible = false;
      this.actorInputValue = '';
    },
    handleActorTagClose(tag) {
      this.form.movieActors.splice(this.form.movieActors.indexOf(tag), 1);
    },
    exampleTest(){
      this.form.style='Romance';
      this.form.movieMaxScore=5.0
      this.form.movieMinScore=3.5;
      // this.form.minYear = 2004;
      // this.form.minMonth = 9;
      // this.form.minDay = 13;
      // this.form.maxYear = 2005;
      // this.form.maxMonth = 9;
      // this.form.maxDay = 15;
      this.searchMovie();
    },
  }
}
</script>

<style scoped>
   .el-tag + .el-tag {
    margin-left: 10px;
  }
  .button-new-tag {
    margin-left: 10px;
    height: 32px;
    line-height: 30px;
    padding-top: 0;
    padding-bottom: 0;
  }
  .input-new-tag {
    width: 90px;
    margin-left: 10px;
    vertical-align: bottom;
  }
  .el-divider--vertical{
    height:75vh;
  }

  .demo-table-expand {
    font-size: 0;
  }
  .demo-table-expand label {
    width: 90px;
    color: #99a9bf;
  }
  .demo-table-expand .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 100%;
  }
</style>
