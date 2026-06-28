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
                    <el-button 
                        type="warning" 
                        @click="showLeaveDialog = true">
                        请假申请
                    </el-button>
                </div>
            </div>

            <div class="calendar-section">
                <el-calendar v-model="currentDate" :cell-class-name="getCellClassName">
                    <template #dateCell="{ date, data }">
                        <div class="calendar-cell" :style="getCellStyle(date)">
                            <span class="date-num">{{ data.day.split('-').slice(-1)[0] }}</span>
                            <span class="status-text">{{ getStatusText(date) }}</span>
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
                    <div class="stat-card sick">
                        <div class="stat-icon">🤒</div>
                        <div class="stat-info">
                            <div class="stat-value">{{ stats.sickLeaveDays }}</div>
                            <div class="stat-label">病假</div>
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

            <el-dialog v-model="showLeaveDialog" title="请假申请" width="500px">
                <el-form :model="leaveForm" label-width="80px">
                    <el-form-item label="请假类型">
                        <el-select v-model="leaveForm.leaveType" placeholder="请选择请假类型">
                            <el-option label="事假" :value="1"></el-option>
                            <el-option label="病假" :value="2"></el-option>
                            <el-option label="年假" :value="3"></el-option>
                            <el-option label="其他" :value="4"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="开始日期">
                        <el-date-picker 
                            v-model="leaveForm.startDate" 
                            type="date" 
                            placeholder="请选择开始日期"
                            :disabled-date="disabledPastDate">
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item label="结束日期">
                        <el-date-picker 
                            v-model="leaveForm.endDate" 
                            type="date" 
                            placeholder="请选择结束日期"
                            :disabled-date="disabledEndDate">
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item label="请假原因">
                        <el-input 
                            v-model="leaveForm.reason" 
                            type="textarea" 
                            rows="3" 
                            placeholder="请输入请假原因">
                        </el-input>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="showLeaveDialog = false">取消</el-button>
                    <el-button type="primary" @click="handleLeaveSubmit">提交申请</el-button>
                </template>
            </el-dialog>
        </div>
    `,
    setup() {
        const { ref, computed, onMounted, watch } = Vue;
        const { ElMessage } = ElementPlus;

        const currentDate = ref(new Date());
        const attendanceRecords = ref([]);
        const checking = ref(false);
        const showLeaveDialog = ref(false);

        const leaveForm = ref({
            leaveType: null,
            startDate: null,
            endDate: null,
            reason: ''
        });

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
            let sickLeaveDays = 0;

            monthRecords.forEach(record => {
                if (record.status === 1) normalDays++;
                else if (record.status === 2) lateDays++;
                else if (record.status === 3) absentDays++;
                else if (record.status === 4) sickLeaveDays++;
            });

            const checkedDates = new Set(monthRecords.map(r => r.checkDate));
            const uncheckedDays = totalDays - checkedDates.size;
            absentDays += uncheckedDays;

            return { normalDays, lateDays, absentDays, sickLeaveDays, totalDays };
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

        watch(currentDate, () => {
            fetchAttendanceRecords();
        });

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

        const handleLeaveSubmit = async () => {
            if (!leaveForm.value.leaveType) {
                ElMessage.warning('请选择请假类型');
                return;
            }
            if (!leaveForm.value.startDate) {
                ElMessage.warning('请选择开始日期');
                return;
            }
            if (!leaveForm.value.endDate) {
                ElMessage.warning('请选择结束日期');
                return;
            }
            if (!leaveForm.value.reason) {
                ElMessage.warning('请输入请假原因');
                return;
            }

            try {
                const res = await axios.post('/api/leave', {
                    leaveType: leaveForm.value.leaveType,
                    startDate: formatDateStr(leaveForm.value.startDate),
                    endDate: formatDateStr(leaveForm.value.endDate),
                    reason: leaveForm.value.reason
                });
                if (res.data.code === 200) {
                    ElMessage.success(res.data.message);
                    showLeaveDialog.value = false;
                    leaveForm.value = {
                        leaveType: null,
                        startDate: null,
                        endDate: null,
                        reason: ''
                    };
                    await fetchAttendanceRecords();
                } else {
                    ElMessage.error(res.data.message || '提交失败');
                }
            } catch (e) {
                console.error('提交请假申请失败', e);
                ElMessage.error('提交请假申请失败，请稍后重试');
            }
        };

        const disabledPastDate = (time) => {
            return time.getTime() < new Date().getTime() - 86400000;
        };

        const disabledEndDate = (time) => {
            if (!leaveForm.value.startDate) {
                return time.getTime() < new Date().getTime() - 86400000;
            }
            return time.getTime() < leaveForm.value.startDate.getTime();
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
            if (record.status === 4) return 'status-sick';
            return 'status-absent';
        };

        const getCellStyle = (date) => {
            const dateStr = formatDateStr(date);
            const record = attendanceRecords.value.find(r => r.checkDate === dateStr);
            console.log('转换后的日期:', dateStr, '找到的记录:', record); // 打印看看
            if (!record) {
                return { backgroundColor: '#fff1f0' };
            }
            if (record.status === 1) return { backgroundColor: '#f6ffed' };
            if (record.status === 2) return { backgroundColor: '#fffbe6' };
            if (record.status === 3) return { backgroundColor: '#fff1f0' };
            if (record.status === 4) return { backgroundColor: '#e6f7ff' };
            return { backgroundColor: '#fff1f0' };
        };

        const getStatusText = (date) => {
            const dateStr = formatDateStr(date);
            const record = attendanceRecords.value.find(r => r.checkDate === dateStr);
            console.log('转换后的日期:', dateStr, '找到的记录:', record); // 打印看看
            if (!record) return '缺卡';
            if (record.status === 1) return '正常';
            if (record.status === 2) return '迟到';
            if (record.status === 3) return '缺卡';
            if (record.status === 4) return '病假';
            return '缺卡';
        };

        const getCellClassName = ({ date }) => {
            const dateStr = formatDateStr(date);
            const record = attendanceRecords.value.find(r => r.checkDate === dateStr);
            
            if (!record) {
                return 'status-absent';
            }
            if (record.status === 1) return 'status-normal';
            if (record.status === 2) return 'status-late';
            if (record.status === 3) return 'status-absent';
            if (record.status === 4) return 'status-sick';
            return 'status-absent';
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
            if (status === 4) return '病假';
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
            showLeaveDialog,
            leaveForm,
            handleCheckIn,
            handleCheckOut,
            handleLeaveSubmit,
            disabledPastDate,
            disabledEndDate,
            getStatusClass,
            getStatusText,
            getCellClassName,
            getCellStyle,
            getStatusTagType,
            getStatusTextByCode,
            formatDate,
            formatDateTime,
            formatDateStr
        };
    }
};

app.component('page-attendance', AttendancePage);