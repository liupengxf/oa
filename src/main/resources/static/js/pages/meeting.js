const MeetingPage = {
    template: `
        <div class="meeting-page">
            <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
                <el-tab-pane label="会议室管理" name="rooms">
                    <div class="page-header">
                        <h2>会议室管理</h2>
                        <div class="header-right">
                            <el-button type="primary" @click="showRoomDialog = true">添加会议室</el-button>
                        </div>
                    </div>
                    <div style="background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                        <el-table :data="rooms" border stripe>
                            <el-table-column prop="id" label="ID"></el-table-column>
                            <el-table-column prop="roomName" label="会议室名称"></el-table-column>
                            <el-table-column prop="roomCode" label="编码"></el-table-column>
                            <el-table-column prop="capacity" label="容纳人数"></el-table-column>
                            <el-table-column prop="location" label="位置"></el-table-column>
                            <el-table-column prop="status" label="状态">
                                <template #default="{ row }">
                                    <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                                        {{ row.status === 1 ? '可用' : '已停用' }}
                                    </el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column label="操作">
                                <template #default="{ row }">
                                    <el-button size="small" @click="editRoom(row)">编辑</el-button>
                                    <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" 
                                               @click="toggleRoomStatus(row)">
                                        {{ row.status === 1 ? '停用' : '启用' }}
                                    </el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </el-tab-pane>

                <el-tab-pane label="预约会议" name="reserve">
                    <div class="page-header">
                        <h2>预约会议</h2>
                    </div>
                    <div style="background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1); padding: 20px;">
                        <el-form :model="reservationForm" label-width="100px">
                            <el-form-item label="选择会议室">
                                <el-select v-model="reservationForm.roomId" placeholder="请选择会议室">
                                    <el-option v-for="room in availableRooms" :key="room.id" 
                                                :label="room.roomName + ' (' + room.capacity + '人)'" 
                                                :value="room.id"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="会议主题">
                                <el-input v-model="reservationForm.meetingTitle" placeholder="请输入会议主题"></el-input>
                            </el-form-item>
                            <el-form-item label="开始时间">
                                <el-date-picker v-model="reservationForm.startTime" type="datetime" placeholder="选择开始时间"></el-date-picker>
                            </el-form-item>
                            <el-form-item label="结束时间">
                                <el-date-picker v-model="reservationForm.endTime" type="datetime" placeholder="选择结束时间"></el-date-picker>
                            </el-form-item>
                            <el-form-item label="会议说明">
                                <el-input v-model="reservationForm.description" type="textarea" placeholder="请输入会议说明"></el-input>
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" @click="checkAvailability">检查可用性</el-button>
                                <el-button type="success" @click="submitReservation">提交预约</el-button>
                                <el-button @click="resetForm">重置</el-button>
                            </el-form-item>
                        </el-form>
                        <div v-if="availabilityChecked" style="margin-top: 15px; padding: 15px; border-radius: 8px;"
                             :class="isAvailable ? 'available-message' : 'unavailable-message'">
                            <span>{{ isAvailable ? '✓ 该时间段可用' : '✗ 该时间段已被占用，请选择其他时间' }}</span>
                        </div>
                    </div>
                </el-tab-pane>

                <el-tab-pane label="我的预约" name="myreservations">
                    <div class="page-header">
                        <h2>我的预约记录</h2>
                    </div>
                    <div style="background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                        <el-table :data="myReservations" border stripe>
                            <el-table-column prop="reservationNo" label="预约单号"></el-table-column>
                            <el-table-column prop="roomId" label="会议室">
                                <template #default="{ row }">
                                    {{ getRoomName(row.roomId) }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="meetingTitle" label="会议主题"></el-table-column>
                            <el-table-column prop="startTime" label="开始时间"></el-table-column>
                            <el-table-column prop="endTime" label="结束时间"></el-table-column>
                            <el-table-column prop="status" label="状态">
                                <template #default="{ row }">
                                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                                        {{ row.status === 1 ? '有效' : '已取消' }}
                                    </el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column label="操作">
                                <template #default="{ row }">
                                    <el-button v-if="row.status === 1" size="small" type="danger" 
                                               @click="cancelReservation(row)">取消</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </el-tab-pane>

                <el-tab-pane label="日历视图" name="calendar">
                    <div class="calendar-event-layout">
                        <div class="calendar-left">
                            <div class="calendar-month-title">
                                <el-button-group>
                                    <el-button size="small" @click="prevMonth">&lt;</el-button>
                                    <el-button size="small" style="width: 120px;">{{ currentMonthStr }}</el-button>
                                    <el-button size="small" @click="nextMonth">&gt;</el-button>
                                </el-button-group>
                            </div>
                            <div class="calendar-mini-header">
                                <span>周一</span>
                                <span>周二</span>
                                <span>周三</span>
                                <span>周四</span>
                                <span>周五</span>
                                <span>周六</span>
                                <span>周日</span>
                            </div>
                            <div class="calendar-mini">
                                <div v-for="(day, idx) in calendarDays" :key="idx"
                                     class="calendar-mini-day"
                                     :class="{ 
                                         'today': isToday(day),
                                         'selected': isSelectedDay(day),
                                         'has-event': hasEventOnDay(day),
                                         'other-month': isOtherMonth(day)
                                     }"
                                     @click="selectDay(day)">
                                    {{ getDayNum(day) }}
                                </div>
                            </div>
                            <div style="margin-top: 20px;">
                                <el-select v-model="calendarRoomId" placeholder="筛选会议室" size="small" style="width: 100%;">
                                    <el-option :value="null" label="全部会议室"></el-option>
                                    <el-option v-for="room in rooms" :key="room.id" 
                                                :label="room.roomName" :value="room.id"></el-option>
                                </el-select>
                            </div>
                            <div style="margin-top: 15px; padding: 15px; background: #f5f5f5; border-radius: 8px;">
                                <div style="font-size: 12px; color: #999; margin-bottom: 8px;">统计信息</div>
                                <div style="display: flex; justify-content: space-between;">
                                    <div>
                                        <div style="font-size: 24px; font-weight: 600; color: #1890ff;">{{ filteredReservations.length }}</div>
                                        <div style="font-size: 12px; color: #666;">本月预约</div>
                                    </div>
                                    <div>
                                        <div style="font-size: 24px; font-weight: 600; color: #52c41a;">{{ rooms.length }}</div>
                                        <div style="font-size: 12px; color: #666;">会议室</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="calendar-right">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                                <h2 style="font-size: 18px; color: #333;">会议室预约情况</h2>
                                <div style="font-size: 14px; color: #666;">{{ selectedDateStr || '请选择日期' }}</div>
                            </div>
                            
                            <div class="event-filter-tabs">
                                <div class="event-filter-tab" :class="{ active: eventFilter === 'all' }" @click="eventFilter = 'all'">
                                    全部预约
                                </div>
                                <div class="event-filter-tab" :class="{ active: eventFilter === 'ongoing' }" @click="eventFilter = 'ongoing'">
                                    进行中
                                </div>
                                <div class="event-filter-tab" :class="{ active: eventFilter === 'upcoming' }" @click="eventFilter = 'upcoming'">
                                    未开始
                                </div>
                                <div class="event-filter-tab" :class="{ active: eventFilter === 'finished' }" @click="eventFilter = 'finished'">
                                    已结束
                                </div>
                            </div>
                            
                            <div class="event-list" v-if="filteredEventList.length > 0">
                                <div v-for="event in filteredEventList" :key="event.id" class="event-card">
                                    <div class="event-card-header">
                                        <div class="event-card-icon" :class="getRoomIconClass(event.roomId)">
                                            🏢
                                        </div>
                                        <div style="flex: 1;">
                                            <div class="event-card-title">{{ event.meetingTitle }}</div>
                                            <div style="font-size: 12px; color: #999; margin-top: 4px;">
                                                {{ getRoomName(event.roomId) }} · {{ event.reservationNo }}
                                            </div>
                                        </div>
                                        <div class="event-card-status" :class="getEventStatusClass(event)">
                                            {{ getEventStatusText(event) }}
                                        </div>
                                    </div>
                                    <div class="event-card-body">
                                        <div class="event-card-info">
                                            <div class="event-card-info-label">开始时间</div>
                                            <div class="event-card-info-value">{{ formatDateTimeDisplay(event.startTime) }}</div>
                                        </div>
                                        <div class="event-card-info">
                                            <div class="event-card-info-label">结束时间</div>
                                            <div class="event-card-info-value">{{ formatDateTimeDisplay(event.endTime) }}</div>
                                        </div>
                                        <div class="event-card-info">
                                            <div class="event-card-info-label">会议室位置</div>
                                            <div class="event-card-info-value">{{ getRoomLocation(event.roomId) }}</div>
                                        </div>
                                    </div>
                                    <div class="event-card-footer">
                                        <span class="event-card-footer-text">预约人：{{ getEmpName(event.empId) }}</span>
                                        <span class="event-card-footer-text">|</span>
                                        <span class="event-card-footer-text" v-if="event.description">{{ event.description }}</span>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="event-empty" v-else>
                                <div style="font-size: 48px; margin-bottom: 10px;">📭</div>
                                <div style="font-size: 16px; color: #999;">暂无预约记录</div>
                                <div style="font-size: 14px; color: #ccc; margin-top: 5px;">请选择其他日期或筛选条件</div>
                            </div>
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>

            <el-dialog v-model="showRoomDialog" title="添加会议室" width="400px">
                <el-form :model="roomForm" label-width="80px">
                    <el-form-item label="会议室名称">
                        <el-input v-model="roomForm.roomName"></el-input>
                    </el-form-item>
                    <el-form-item label="会议室编码">
                        <el-input v-model="roomForm.roomCode"></el-input>
                    </el-form-item>
                    <el-form-item label="容纳人数">
                        <el-input-number v-model="roomForm.capacity" :min="1"></el-input-number>
                    </el-form-item>
                    <el-form-item label="位置">
                        <el-input v-model="roomForm.location"></el-input>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="showRoomDialog = false">取消</el-button>
                    <el-button type="primary" @click="saveRoom">确定</el-button>
                </template>
            </el-dialog>
        </div>
    `,
    setup() {
        const { ref, computed, onMounted } = Vue;
        const { ElMessage } = ElementPlus;

        const activeTab = ref('rooms');
        const rooms = ref([]);
        const availableRooms = ref([]);
        const myReservations = ref([]);
        const calendarReservations = ref([]);
        const calendarRoomId = ref(null);
        const eventFilter = ref('all');
        const currentMonth = ref(new Date());
        const selectedDay = ref(new Date());

        const showRoomDialog = ref(false);
        const roomForm = ref({ roomName: '', roomCode: '', capacity: 10, location: '' });
        const editingRoom = ref(null);

        const reservationForm = ref({
            roomId: null,
            meetingTitle: '',
            startTime: null,
            endTime: null,
            description: ''
        });
        const availabilityChecked = ref(false);
        const isAvailable = ref(false);

        const calendarDays = computed(() => {
            const year = currentMonth.value.getFullYear();
            const month = currentMonth.value.getMonth();
            const firstDay = new Date(year, month, 1);
            const lastDay = new Date(year, month + 1, 0);
            let firstDayOfWeek = firstDay.getDay();
            firstDayOfWeek = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1;
            const days = [];
            for (let i = firstDayOfWeek - 1; i >= 0; i--) {
                const prevDay = new Date(year, month, -i);
                days.push(prevDay);
            }
            for (let i = 1; i <= lastDay.getDate(); i++) {
                days.push(new Date(year, month, i));
            }
            const remainingDays = 42 - days.length;
            for (let i = 1; i <= remainingDays; i++) {
                days.push(new Date(year, month + 1, i));
            }
            return days;
        });

        const currentMonthStr = computed(() => {
            const year = currentMonth.value.getFullYear();
            const month = currentMonth.value.getMonth() + 1;
            return `${year}年${month}月`;
        });

        const selectedDateStr = computed(() => {
            const d = new Date(selectedDay.value);
            return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        });

        const filteredEventList = computed(() => {
            let list = calendarReservations.value.filter(r => {
                if (calendarRoomId.value && r.roomId !== calendarRoomId.value) return false;
                const eventDate = r.startTime.split('T')[0];
                if (eventDate !== selectedDateStr.value) return false;
                return true;
            });
            const now = new Date();
            if (eventFilter.value !== 'all') {
                list = list.filter(event => {
                    const start = new Date(event.startTime);
                    const end = new Date(event.endTime);
                    if (eventFilter.value === 'ongoing') {
                        return start <= now && end >= now;
                    } else if (eventFilter.value === 'upcoming') {
                        return start > now;
                    } else if (eventFilter.value === 'finished') {
                        return end < now;
                    }
                    return true;
                });
            }
            return list;
        });

        const filteredReservations = computed(() => {
            return calendarReservations.value.filter(r => {
                if (calendarRoomId.value && r.roomId !== calendarRoomId.value) return false;
                return r.status === 1;
            });
        });

        const loadRooms = async () => {
            try {
                const res = await axios.get('/api/meeting/rooms/all');
                if (res.data.code === 200) {
                    rooms.value = res.data.data;
                }
                const availableRes = await axios.get('/api/meeting/rooms');
                if (availableRes.data.code === 200) {
                    availableRooms.value = availableRes.data.data;
                }
            } catch (e) {
                console.error('加载会议室失败', e);
            }
        };

        const loadMyReservations = async () => {
            try {
                const res = await axios.get('/api/meeting/reservations/my');
                if (res.data.code === 200) {
                    myReservations.value = res.data.data;
                }
            } catch (e) {
                console.error('加载预约失败', e);
            }
        };

        const loadCalendarData = async () => {
            const firstDay = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth(), 1);
            const lastDay = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 0);
            const startDate = formatDate(firstDay);
            const endDate = formatDate(lastDay);
            try {
                const url = `/api/meeting/calendar?startDate=${startDate}&endDate=${endDate}`;
                const res = await axios.get(url);
                if (res.data.code === 200) {
                    calendarReservations.value = res.data.data.reservations;
                }
            } catch (e) {
                console.error('加载日历数据失败', e);
            }
        };

        const saveRoom = async () => {
            if (!roomForm.value.roomName || !roomForm.value.roomCode) {
                ElMessage.warning('请填写会议室名称和编码');
                return;
            }
            try {
                let res;
                if (editingRoom.value) {
                    res = await axios.put('/api/meeting/rooms/' + editingRoom.value.id, roomForm.value);
                } else {
                    res = await axios.post('/api/meeting/rooms', roomForm.value);
                }
                if (res.data.code === 200) {
                    ElMessage.success(editingRoom.value ? '修改成功' : '添加成功');
                    showRoomDialog.value = false;
                    roomForm.value = { roomName: '', roomCode: '', capacity: 10, location: '' };
                    editingRoom.value = null;
                    loadRooms();
                }
            } catch (e) {
                ElMessage.error('操作失败');
            }
        };

        const editRoom = (row) => {
            editingRoom.value = row;
            roomForm.value = { ...row };
            showRoomDialog.value = true;
        };

        const toggleRoomStatus = async (row) => {
            try {
                const res = await axios.put(`/api/meeting/rooms/${row.id}`, {
                    ...row,
                    status: row.status === 1 ? 0 : 1
                });
                if (res.data.code === 200) {
                    ElMessage.success(row.status === 1 ? '已停用' : '已启用');
                    loadRooms();
                }
            } catch (e) {
                ElMessage.error('操作失败');
            }
        };

        const checkAvailability = async () => {
            if (!reservationForm.value.roomId || !reservationForm.value.startTime || !reservationForm.value.endTime) {
                ElMessage.warning('请先选择会议室和时间');
                return;
            }
            const startTime = formatDateTime(reservationForm.value.startTime);
            const endTime = formatDateTime(reservationForm.value.endTime);
            try {
                const res = await axios.get('/api/meeting/calendar', {
                    params: {
                        roomId: reservationForm.value.roomId,
                        startDate: startTime.split('T')[0],
                        endDate: endTime.split('T')[0]
                    }
                });
                if (res.data.code === 200) {
                    const reservations = res.data.data.reservations;
                    isAvailable.value = !reservations.some(r => 
                        checkTimeOverlap(r.startTime, r.endTime, startTime, endTime)
                    );
                    availabilityChecked.value = true;
                }
            } catch (e) {
                ElMessage.error('检查失败');
            }
        };

        const checkTimeOverlap = (existingStart, existingEnd, newStart, newEnd) => {
            return !(new Date(existingEnd) <= new Date(newStart) || 
                     new Date(existingStart) >= new Date(newEnd));
        };

        const submitReservation = async () => {
            if (!reservationForm.value.roomId || !reservationForm.value.meetingTitle || 
                !reservationForm.value.startTime || !reservationForm.value.endTime) {
                ElMessage.warning('请填写完整信息');
                return;
            }
            const startTime = formatDateTime(reservationForm.value.startTime);
            const endTime = formatDateTime(reservationForm.value.endTime);
            try {
                const res = await axios.post('/api/meeting/reservations', {
                    roomId: reservationForm.value.roomId,
                    meetingTitle: reservationForm.value.meetingTitle,
                    startTime: startTime,
                    endTime: endTime,
                    description: reservationForm.value.description
                });
                if (res.data.code === 200) {
                    ElMessage.success('预约成功');
                    resetForm();
                    loadMyReservations();
                    loadCalendarData();
                } else {
                    ElMessage.error(res.data.msg);
                }
            } catch (e) {
                ElMessage.error(e.response?.data?.msg || '预约失败');
            }
        };

        const resetForm = () => {
            reservationForm.value = {
                roomId: null,
                meetingTitle: '',
                startTime: null,
                endTime: null,
                description: ''
            };
            availabilityChecked.value = false;
            isAvailable.value = false;
        };

        const cancelReservation = async (row) => {
            try {
                const res = await axios.delete(`/api/meeting/reservations/${row.id}`);
                if (res.data.code === 200) {
                    ElMessage.success('已取消');
                    loadMyReservations();
                    loadCalendarData();
                } else {
                    ElMessage.error(res.data.msg);
                }
            } catch (e) {
                ElMessage.error(e.response?.data?.msg || '取消失败');
            }
        };

        const getRoomName = (roomId) => {
            const room = rooms.value.find(r => r.id === roomId);
            return room ? room.roomName : '未知';
        };

        const getRoomLocation = (roomId) => {
            const room = rooms.value.find(r => r.id === roomId);
            return room ? room.location : '未知';
        };

        const getEmpName = (empId) => {
            const empNames = { 1: '管理员', 2: '张经理', 3: '李主管', 4: '王财务' };
            return empNames[empId] || `员工${empId}`;
        };

        const prevMonth = () => {
            currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1, 1);
            loadCalendarData();
        };

        const nextMonth = () => {
            currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1);
            loadCalendarData();
        };

        const selectDay = (day) => {
            selectedDay.value = day;
        };

        const getDayNum = (day) => {
            return day.getDate();
        };

        const isToday = (day) => {
            const today = new Date();
            return day.getFullYear() === today.getFullYear() &&
                   day.getMonth() === today.getMonth() &&
                   day.getDate() === today.getDate();
        };

        const isSelectedDay = (day) => {
            return day.getFullYear() === selectedDay.value.getFullYear() &&
                   day.getMonth() === selectedDay.value.getMonth() &&
                   day.getDate() === selectedDay.value.getDate();
        };

        const isOtherMonth = (day) => {
            return day.getMonth() !== currentMonth.value.getMonth();
        };

        const hasEventOnDay = (day) => {
            const dateStr = formatDate(day);
            return calendarReservations.value.some(r => {
                if (calendarRoomId.value && r.roomId !== calendarRoomId.value) return false;
                const eventDate = r.startTime.split('T')[0];
                return eventDate === dateStr && r.status === 1;
            });
        };

        const getRoomIconClass = (roomId) => {
            const idx = (roomId - 1) % 5;
            return `room-${idx + 1}`;
        };

        const getEventStatusClass = (event) => {
            const now = new Date();
            const start = new Date(event.startTime);
            const end = new Date(event.endTime);
            if (start <= now && end >= now) return 'ongoing';
            if (start > now) return 'upcoming';
            return 'finished';
        };

        const getEventStatusText = (event) => {
            const now = new Date();
            const start = new Date(event.startTime);
            const end = new Date(event.endTime);
            if (start <= now && end >= now) return '进行中';
            if (start > now) return '未开始';
            return '已结束';
        };

        const formatDateTimeDisplay = (datetimeStr) => {
            const d = new Date(datetimeStr);
            return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
        };

        const formatDate = (date) => {
            const d = new Date(date);
            return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        };

        const formatDateTime = (date) => {
            const d = new Date(date);
            return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}T${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:00`;
        };

        const handleTabChange = (tab) => {
            if (tab === 'myreservations') {
                loadMyReservations();
            } else if (tab === 'calendar') {
                loadCalendarData();
            }
        };

        onMounted(() => {
            loadRooms();
        });

        return {
            activeTab,
            rooms,
            availableRooms,
            myReservations,
            calendarReservations,
            calendarRoomId,
            eventFilter,
            currentMonth,
            selectedDay,
            calendarDays,
            currentMonthStr,
            selectedDateStr,
            filteredEventList,
            filteredReservations,
            showRoomDialog,
            roomForm,
            editingRoom,
            reservationForm,
            availabilityChecked,
            isAvailable,
            loadRooms,
            loadMyReservations,
            loadCalendarData,
            saveRoom,
            editRoom,
            toggleRoomStatus,
            checkAvailability,
            submitReservation,
            resetForm,
            cancelReservation,
            getRoomName,
            getRoomLocation,
            getEmpName,
            prevMonth,
            nextMonth,
            selectDay,
            getDayNum,
            isToday,
            isSelectedDay,
            isOtherMonth,
            hasEventOnDay,
            getRoomIconClass,
            getEventStatusClass,
            getEventStatusText,
            formatDateTimeDisplay,
            handleTabChange
        };
    }
};

if (window.appInstance) {
    window.appInstance.component('page-meeting', MeetingPage);
}
