const AttendancePage = {
    template: `
        <div class="attendance-page">
            <div class="page-header">
                <h2>考勤日历</h2>
                <div class="header-right">
                    <el-button 
                        v-if="canCheckIn" 
                        type="primary" 
                        @click="handleCheckIn"
                        :loading="checking">
                        上班打卡
                    </el-button>
                    <el-button 
                        v-if="canCheckOut" 
                        type="success" 
                        @click="handleCheckOut"
                        :loading="checking">
                        下班打卡
                    </el-button>
                    <el-button 
                        v-if="hasCheckedIn && hasCheckedOut" 
                        disabled>
                        今日已完成打卡
                    </el-button>
                </div>
            </div>

            <div class="calendar-section">
                <el-calendar v-model="currentDate">
                    <template #dateCell="{ date, data }">
                        <div class="calendar-cell">
                            <span class="date-num">{{ data.day }}</span>
                            <div class="status-indicator" :class="getStatusClass(date)">
                                {{ getStatusText(date) }}
                            </div>
                        </div>
                    </template>
                </el-calendar>
            </div>

            <div class="stats-section">
                <h3>本月考勤统计</h3>
                <div class="stats-cards">
                    <div class="stat-card normal">
                        <div class="stat-icon">✓</div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.normalDays }}</div>
                            <div class="stat-label">正常</div>
                        </div>
                    </div>
                    <div class="stat-card late">
                        <div class="stat-icon">!</div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.lateDays }}</div>
                            <div class="stat-label">迟到</div>
                        </div>
                    </div>
                    <div class="stat-card absent">
                        <div class="stat-icon">✕</div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.absentDays }}</div>
                            <div class="stat-label">缺卡</div>
                        </div>
                    </div>
                    <div class="stat-card total">
                        <div class="stat-icon">📅</div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.totalDays }}</div>
                            <div class="stat-label">本月天数</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="detail-section">
                <h3>本月打卡记录</h3>
                <el-table :data="filteredRecords" border stripe>
                    <el-table-column prop="checkDate" label="日期" width="120">
                        <template #default="scope">
                            {{ formatDate(scope.row.checkDate) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="checkInTime" label="上班打卡" width="150">
                        <template #default="scope">
                            {{ scope.row.checkInTime ? formatDateTime(scope.row.checkInTime) : '--' }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="checkOutTime" label="下班打卡" width="150">
                        <template #default="scope">
                            {{ scope.row.checkOutTime ? formatDateTime(scope.row.checkOutTime) : '--' }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="status" label="状态" width="100">
                        <template #default="scope">
                            <el-tag :type="getStatusTagType(scope.row.status)">
                                {{ getStatusTextByCode(scope.row.status) }}
                            </el-tag>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </div>
    `,
    setup() {
        const { ref, computed, onMounted } = Vue;
        const { ElMessage } = ElementPlus;

        const currentDate = ref(new Date());
        const attendanceRecords = ref([]);
        const checking = ref(false);

        const filteredRecords = computed(() => {
            const currentMonth = currentDate.value.getMonth();
            const currentYear = currentDate.value.getFullYear();
            return attendanceRecords.value.filter(r => {
                const date = new Date(r.checkDate);
                return date.getMonth() === currentMonth && date.getFullYear() === currentYear;
            }).sort((a, b) => new Date(b.checkDate) - new Date(a.checkDate));
        });

        const stats = computed(() => {
            const currentMonth = currentDate.value.getMonth();
            const currentYear = currentDate.value.getFullYear();
            const monthRecords = attendanceRecords.value.filter(r => {
                const date = new Date(r.checkDate);
                return date.getMonth() === currentMonth && date.getFullYear() === currentYear;
            });

            const totalDays = new Date(currentYear, currentMonth + 1, 0).getDate();
            let normalDays = 0;
            let lateDays = 0;
            let absentDays = 0;
            let restDays = 0;

            monthRecords.forEach(record => {
                if (record.status === 1) normalDays++;
                else if (record.status === 2) lateDays++;
                else if (record.status === 3) absentDays++;
                else if (record.status === 4) restDays++;
            });

            const checkedDates = new Set(monthRecords.map(r => r.checkDate));
            const uncheckedDays = totalDays - checkedDates.size;
            absentDays += uncheckedDays;

            return { normalDays, lateDays, absentDays, totalDays, restDays };
        });

        const canCheckIn = computed(() => {
            const today = new Date().toISOString().split('T')[0];
            const todayRecord = attendanceRecords.value.find(r => r.checkDate === today);
            return !todayRecord || !todayRecord.checkInTime;
        });

        const canCheckOut = computed(() => {
            const today = new Date().toISOString().split('T')[0];
            const todayRecord = attendanceRecords.value.find(r => r.checkDate === today);
            return todayRecord && todayRecord.checkInTime && !todayRecord.checkOutTime;
        });

        const hasCheckedIn = computed(() => !canCheckIn.value);
        const hasCheckedOut = computed(() => !canCheckOut.value);

        const fetchAttendanceRecords = async () => {
            const year = currentDate.value.getFullYear();
            const month = currentDate.value.getMonth() + 1;
            try {
                const res = await axios.get('/api/attendance/monthly', {
                    params: { year, month }
                });
                if (res.data.code === 200) {
                    attendanceRecords.value = res.data.data;
                }
            } catch (e) {
                console.error('获取考勤记录失败', e);
                ElMessage.error('获取考勤记录失败');
            }
        };

        const handleCheckIn = async () => {
            checking.value = true;
            try {
                const res = await axios.post('/api/attendance/checkin');
                if (res.data.code === 200) {
                    ElMessage.success(res.data.message);
                    await fetchAttendanceRecords();
                } else {
                    ElMessage.error(res.data.message || '打卡失败');
                }
            } catch (e) {
                ElMessage.error('打卡失败，请稍后重试');
            } finally {
                checking.value = false;
            }
        };

        const handleCheckOut = async () => {
            checking.value = true;
            try {
                const res = await axios.post('/api/attendance/checkout');
                if (res.data.code === 200) {
                    ElMessage.success(res.data.message);
                    await fetchAttendanceRecords();
                } else {
                    ElMessage.error(res.data.message || '打卡失败');
                }
            } catch (e) {
                ElMessage.error('打卡失败，请稍后重试');
            } finally {
                checking.value = false;
            }
        };

        const formatDateStr = (date) => {
            const y = date.getFullYear();
            const m = String(date.getMonth() + 1).padStart(2, '0');
            const d = String(date.getDate()).padStart(2, '0');
            return `${y}-${m}-${d}`;
        };

        const getStatusClass = (date) => {
            const dateStr = formatDateStr(date);
            const record = attendanceRecords.value.find(r => r.checkDate === dateStr);
            
            if (!record) {
                return 'status-absent';
            }
            if (record.status === 1) return 'status-normal';
            if (record.status === 2) return 'status-late';
            if (record.status === 3) return 'status-absent';
            if (record.status === 4) return 'status-rest';
            return 'status-absent';
        };

        const getStatusText = (date) => {
            const dateStr = formatDateStr(date);
            const record = attendanceRecords.value.find(r => r.checkDate === dateStr);
            
            if (!record) return '缺卡';
            if (record.status === 1) return '正常';
            if (record.status === 2) return '迟到';
            if (record.status === 3) return '缺卡';
            if (record.status === 4) return '休息';
            return '缺卡';
        };

        const getStatusTagType = (status) => {
            if (status === 1) return 'success';
            if (status === 2) return 'warning';
            if (status === 3) return 'danger';
            if (status === 4) return 'info';
            return 'danger';
        };

        const getStatusTextByCode = (status) => {
            if (status === 1) return '正常';
            if (status === 2) return '迟到';
            if (status === 3) return '缺卡';
            if (status === 4) return '休息';
            return '缺卡';
        };

        const formatDate = (dateStr) => {
            if (!dateStr) return '';
            const date = new Date(dateStr);
            return `${date.getMonth() + 1}月${date.getDate()}日`;
        };

        const formatDateTime = (dateTimeStr) => {
            if (!dateTimeStr) return '';
            const date = new Date(dateTimeStr);
            return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
        };

        onMounted(() => {
            fetchAttendanceRecords();
        });

        return {
            currentDate,
            attendanceRecords,
            filteredRecords,
            checking,
            stats,
            canCheckIn,
            canCheckOut,
            hasCheckedIn,
            hasCheckedOut,
            handleCheckIn,
            handleCheckOut,
            getStatusClass,
            getStatusText,
            getStatusTagType,
            getStatusTextByCode,
            formatDate,
            formatDateTime
        };
    }
};

app.component('page-attendance', AttendancePage);