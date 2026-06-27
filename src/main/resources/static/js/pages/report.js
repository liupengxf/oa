const ReportPage = {
    template: `
        <div class="report-page">
            <div class="page-header">
                <h2>数据看板</h2>
                <div class="header-right">
                    <el-button type="success" @click="exportEmployee">导出员工台账</el-button>
                    <el-button type="success" @click="exportAttendance">导出考勤报表</el-button>
                    <el-button type="success" @click="exportExpense">导出报销明细</el-button>
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

                <div class="chart-card">
                    <div class="chart-header">
                        <h3>考勤异常率</h3>
                    </div>
                    <div ref="attendanceChartRef" class="chart-container"></div>
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
        const attendanceChartRef = ref(null);

        let deptChart = null;
        let leaveChart = null;
        let expenseChart = null;
        let attendanceChart = null;

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
                if (attendanceChartRef.value && !attendanceChart) {
                    attendanceChart = echarts.init(attendanceChartRef.value);
                }
                refreshData();
            }
        };

        const refreshData = async () => {
            await Promise.all([
                loadDeptEmpCount(),
                loadLeaveTypeStats(),
                loadExpenseTrend(),
                loadAttendanceAbnormalRate()
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

        const loadAttendanceAbnormalRate = async () => {
            try {
                const res = await axios.get('/api/report/attendance-abnormal-rate');
                if (res.data.code === 200 && attendanceChart) {
                    const data = res.data.data;
                    const rate = data.abnormalRate || 0;
                    const abnormalCount = data.abnormalCount || 0;
                    const totalCount = data.totalCount || 0;
                    
                    attendanceChart.setOption({
                        series: [{
                            type: 'gauge',
                            startAngle: 200,
                            endAngle: -20,
                            min: 0,
                            max: 100,
                            splitNumber: 5,
                            axisLine: {
                                lineStyle: {
                                    width: 20,
                                    color: [
                                        [0.3, '#5470c6'],
                                        [0.7, '#fac858'],
                                        [1, '#ee6666']
                                    ]
                                }
                            },
                            pointer: { itemStyle: { color: '#ee6666' }, length: '60%', width: 6 },
                            axisTick: { show: false },
                            splitLine: { length: 15, lineStyle: { color: '#999', width: 2 } },
                            axisLabel: { formatter: '{value}%', color: '#666', fontSize: 12 },
                            detail: {
                                valueAnimation: true,
                                formatter: '{value}%',
                                color: '#ee6666',
                                fontSize: 24,
                                fontWeight: 'bold',
                                offsetCenter: [0, '70%']
                            },
                            data: [{ value: rate, name: '异常率' }]
                        }],
                        graphic: [{
                            type: 'group',
                            left: 'center',
                            top: '85%',
                            children: [{
                                type: 'text',
                                style: {
                                    text: `异常: ${abnormalCount} / 总计: ${totalCount}`,
                                    fill: '#666',
                                    fontSize: 12
                                },
                                left: 'center'
                            }]
                        }]
                    });
                }
            } catch (e) {
                console.error('加载考勤异常率失败', e);
            }
        };

        const exportEmployee = () => {
            window.open('/api/report/export/employee', '_blank');
            ElMessage.success('正在导出员工台账...');
        };

        const exportAttendance = () => {
            window.open('/api/report/export/attendance', '_blank');
            ElMessage.success('正在导出考勤报表...');
        };

        const exportExpense = () => {
            window.open('/api/report/export/expense', '_blank');
            ElMessage.success('正在导出报销明细...');
        };

        const handleResize = () => {
            deptChart && deptChart.resize();
            leaveChart && leaveChart.resize();
            expenseChart && expenseChart.resize();
            attendanceChart && attendanceChart.resize();
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
            attendanceChart && attendanceChart.dispose();
        });

        return {
            deptChartRef,
            leaveChartRef,
            expenseChartRef,
            attendanceChartRef,
            refreshData,
            exportEmployee,
            exportAttendance,
            exportExpense
        };
    }
};

app.component('page-report', ReportPage);