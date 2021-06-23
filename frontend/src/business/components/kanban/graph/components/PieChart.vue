<template>
    <div class="echart" :id="id" :style="{float:'left',width: '100%', height: '100%'}"></div> 
</template>

<script>
import echarts from  "echarts";

export default {
  name: "PieChart",
  props: ["id", "title", "subTitle", "data"],
  data() {
    return {
      myChart: null,
      option: {
          color: ['#749f83', '#c23531', '#6e7074', '#546570', '#c4ccd3', '#d48265', '#61a0a8', '#bda29a', '#ca8622', '#91c7ae', '#2f4554'],
          title: {
              text: this.title,
              subtext: this.subTitle,
              left: 'center'
          },
          tooltip: {
              trigger: 'item'
          },
          legend: {
              orient: 'vertical',
              left: 'left',
          },
          series: [
              {
                  name: '完成状态',
                  type: 'pie',
                  radius: '50%',
                  data: this.data,
                  emphasis: {
                      itemStyle: {
                          shadowBlur: 10,
                          shadowOffsetX: 0,
                          shadowColor: 'rgba(0, 0, 0, 0.5)'
                      }
                  }
              }
          ]
      }
    }
  },
  mounted() {
    this.myChart = echarts.init(document.getElementById(this.id));
    this.initChart();
  },
  methods:{
    initChart() {
      this.myChart.setOption(this.option);

      //随着屏幕大小调节图表
      window.addEventListener("resize", () => {
        this.myChart.resize();
      });
    },
    updateChart(data) {
      if (data === null) { return; }
      
      this.option.series[0].data = data;
      this.myChart.setOption(this.option);
      this.$forceUpdate();

      //随着屏幕大小调节图表
      window.addEventListener("resize", () => {
        this.myChart.resize();
      });
    }
  }

}
</script>
