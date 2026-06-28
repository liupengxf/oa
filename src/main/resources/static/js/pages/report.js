const ReportPage = {
    template: `
        <div class="report-page">
            <div class="page-header">
                <h2>数据看板</h2>
                <div class="header-right">
                    <el-button type="success" @click="exportComprehensive">导出综合报表</el-button>
                    <el-button type="primary" @click="refreshData">刷新数据</el-button>
                </div>
            </div>

            <div class="charts-grid">
                <div class="chart-card">
                    <div class="chart-header">
                        <h3>部门人数统计</h3>
                    </div>
                    <div ref="deptChartRef" class="chart-container"></div>
                </div>

                <div class="chart-card">
                    <div class="chart-header">
                        <h3>请假类型分布</h3>
                    </div>
                    <div ref="leaveChartRef" class="chart-container"></div>
                </div>

                <div class="chart-card wide">
                    <div class="chart-header">
                        <h3>报销趋势</h3>
                    </div>
                    <div ref="expenseChartRef" class="chart-container"></div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const { ref, onMounted, onUnmounted, nextTick } = Vue;
        const { ElMessage } = ElementPlus;

        const deptChartRef = ref(null);
        const leaveChartRef = ref(null);
        const expenseChartRef = ref(null);

        let deptChart = null;
        let leaveChart = null;
        let expenseChart = null;

        const initCharts = () => {
            if (window.echarts) {
                if (deptChartRef.value && !deptChart) {
                    deptChart = echarts.init(deptChartRef.value);
                }
                if (leaveChartRef.value && !leaveChart) {
                    leaveChart = echarts.init(leaveChartRef.value);
                }
                if (expenseChartRef.value && !expenseChart) {
                    expenseChart = echarts.init(expenseChartRef.value);
                }
                refreshData();
            }
        };

        const refreshData = async () => {
            await Promise.all([
                loadDeptEmpCount(),
                loadLeaveTypeStats(),
                loadExpenseTrend()
            ]);
        };

        const loadDeptEmpCount = async () => {
            try {
                const res = await axios.get('/api/report/dept-emp-count');
                if (res.data.code === 200 && deptChart) {
                    const data = res.data.data;
                    const names = data.map(item => item.deptName || '未知部门');
                    const counts = data.map(item => item.empCount || 0);
                    
                    deptChart.setOption({
                        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
                        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
                        xAxis: { type: 'category', data: names, axisLabel: { rotate: 30, fontSize: 10 } },
                        yAxis: { type: 'value', name: '人数' },
                        series: [{
                            type: 'bar',
                            data: counts,
                            itemStyle: {
                                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                    { offset: 0, color: '#5470c6' },
                                    { offset: 1, color: '#91cc75' }
                                ])
                            },
                            label: { show: true, position: 'top' }
                        }]
                    });
                }
            } catch (e) {
                console.error('加载部门人数统计失败', e);
            }
        };

        const loadLeaveTypeStats = async () => {
            try {
                const res = await axios.get('/api/report/leave-type-stats');
                if (res.data.code === 200 && leaveChart) {
                    const data = res.data.data;
                    const pieData = data.map(item => ({
                        name: item.leaveTypeName || '其他',
                        value: item.count || 0
                    }));
                    
                    leaveChart.setOption({
                        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
                        legend: { orient: 'vertical', right: '5%', top: 'center' },
                        series: [{
                            type: 'pie',
                            radius: ['40%', '70%'],
                            center: ['40%', '50%'],
                            avoidLabelOverlap: false,
                            itemStyle: {
                                borderRadius: 10,
                                borderColor: '#fff',
                                borderWidth: 2
                            },
                            label: { show: true, formatter: '{b}\n{d}%' },
                            data: pieData.length > 0 ? pieData : [{ name: '暂无数据', value: 1 }],
                            color: ['#5470c6', '#91cc75', '#fac858', '#ee6666']
                        }]
                    });
                }
            } catch (e) {
                console.error('加载请假类型统计失败', e);
            }
        };

        const loadExpenseTrend = async () => {
            try {
                const res = await axios.get('/api/report/expense-trend');
                if (res.data.code === 200 && expenseChart) {
                    const data = res.data.data;
                    const months = data.map(item => item.month || '');
                    const amounts = data.map(item => {
                        const val = item.totalAmount;
                        return val != null ? (typeof val === 'number' ? val : parseFloat(val)) : 0;
                    });
                    
                    expenseChart.setOption({
                        tooltip: { trigger: 'axis' },
                        legend: { data: ['报销金额'] },
                        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
                        xAxis: { type: 'category', boundaryGap: false, data: months.length > 0 ? months : ['暂无数据'] },
                        yAxis: { type: 'value', name: '金额(元)' },
                        series: [{
                            name: '报销金额',
                            type: 'line',
                            smooth: true,
                            data: amounts.length > 0 ? amounts : [0],
                            areaStyle: {
                                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                    { offset: 0, color: 'rgba(84, 112, 198, 0.3)' },
                                    { offset: 1, color: 'rgba(84, 112, 198, 0.05)' }
                                ])
                            },
                            lineStyle: { color: '#5470c6', width: 3 },
                            itemStyle: { color: '#5470c6' }
                        }]
                    });
                }
            } catch (e) {
                console.error('加载报销趋势失败', e);
            }
        };

        const exportComprehensive = () => {
            window.open('/api/report/export/comprehensive', '_blank');
            ElMessage.success('正在导出综合报表...');
        };

        const handleResize = () => {
            deptChart && deptChart.resize();
            leaveChart && leaveChart.resize();
            expenseChart && expenseChart.resize();
        };

        onMounted(() => {
            nextTick(() => {
                initCharts();
            });
            window.addEventListener('resize', handleResize);
        });

        onUnmounted(() => {
            window.removeEventListener('resize', handleResize);
            deptChart && deptChart.dispose();
            leaveChart && leaveChart.dispose();
            expenseChart && expenseChart.dispose();
        });

        return {
            deptChartRef,
            leaveChartRef,
            expenseChartRef,
            refreshData,
            exportComprehensive
        };
    }
};

app.component('page-report', ReportPage);