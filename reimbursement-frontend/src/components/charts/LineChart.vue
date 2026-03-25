<template>
  <div ref="chartRef" :style="{ width: '100%', height: '300px' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  data: any[]
  xField: string
  yField: string
  title?: string
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chart) return
  const option = {
    title: { text: props.title, left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: props.data.map(item => item[props.xField])
    },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [{
      data: props.data.map(item => item[props.yField]),
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      lineStyle: { color: '#409eff', width: 2 },
      symbol: 'circle',
      symbolSize: 8
    }]
  }
  chart.setOption(option)
}

watch(() => props.data, () => updateChart(), { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => chart?.resize())
})
</script>