<template>
  <div class="dashboard-container">
    <!-- <div class="dashboard-text">name: {{ name }}</div>
    <div>通过爬虫获取数据</div> -->
    <mavon-editor style="z-index:-100;height: 30%;" 
    v-model="value" 
    :subfield="false" 
    :defaultOpen="'preview'" 
    :editable='false'
    :toolbarsFlag='false' 
    :navigation='false' />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import {mavonEditor} from 'mavon-editor'
import 'mavon-editor/dist/css/index.css'
export default {
  name: 'Dashboard',
  computed: {
    ...mapGetters([
      'name'
    ])
  },
  data(){
    return{
      value:"## 你好\n你好"
    }
  },
  components: { mavonEditor},
  created(){
    var axios = require('axios');
    this.value = '**使用说明**\n'+
    '- 1.电影查询: 单条件查询(按时间进行查询及统计、按导演进行查询及统计、按演员进行查询统计等）或 多条件联合查询 \n' +
    '- 2.关系查询: 按照演员、导演之间的关系进行查询及统计\n'+
    '- 3.溯源查询: 在过程中我们找出多少非电影的数据；系列电影溯源结果\n';
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;
  }
  &-text {
    font-size: 30px;
    line-height: 46px;
  }
}
</style>
