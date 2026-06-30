const ApprovalPage = {
    template: `
        <div class="approval-page">
            <!-- 角色切换提示 -->
            <el-card class="tip-card" shadow="never">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <strong>审批流程说明：</strong>员工提交报销 → 部门经理审批 → 财务审批 → 流程结束。当前角色决定了您可操作的单据状态。
                    </div>
                    <div class="role-selector">
                        <span>当前角色：</span>
                        <el-select v-model="currentRole" size="small" style="width: 140px" @change="onRoleChange">
                            <el-option label="普通员工" :value="2"></el-option>
                            <el-option label="部门经理" :value="3"></el-option>
                            <el-option label="财务管理员" :value="5"></el-option>
                        </el-select>
                    </div>
                </div>
            </el-card>

            <el-tabs v-model="activeTab" type="border-card">
                <!-- 员工：发起报销申请 -->
                <el-tab-pane label="发起申请" name="submit" v-if="currentRole === 2">
                    <div class="page-header">
                        <h2>📝 发起报销申请</h2>
                    </div>
                    <div style="background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                        <el-form :model="expenseForm" label-width="100px" style="max-width: 600px">
                            <el-form-item label="报销类型" prop="expenseType">
                                <el-select v-model="expenseForm.expenseType" placeholder="请选择报销类型" style="width: 100%">
                                    <el-option label="差旅费" value="差旅费"></el-option>
                                    <el-option label="招待费" value="招待费"></el-option>
                                    <el-option label="办公费" value="办公费"></el-option>
                                    <el-option label="交通费" value="交通费"></el-option>
                                    <el-option label="其他" value="其他"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="报销金额" prop="totalAmount">
                                <el-input-number v-model="expenseForm.totalAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
                            </el-form-item>
                            <el-form-item label="报销说明" prop="description">
                                <el-input v-model="expenseForm.description" type="textarea" :rows="3" placeholder="请填写报销事由"></el-input>
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" @click="submitExpense">提交报销申请</el-button>
                                <el-button @click="resetForm">重置</el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </el-tab-pane>

                <!-- 报销单列表 -->
                <el-tab-pane label="报销单列表" name="list">
                    <div class="page-header">
                        <h2>📋 报销单列表</h2>
                        <div class="header-right">
                            <el-select v-model="statusFilter" placeholder="状态筛选" size="small" style="width: 140px" clearable>
                                <el-option label="待经理审批" value="PENDING"></el-option>
                                <el-option label="待财务审批" value="MANAGER_APPROVED"></el-option>
                                <el-option label="已完成" value="COMPLETED"></el-option>
                                <el-option label="已拒绝" value="REJECTED"></el-option>
                            </el-select>
                        </div>
                    </div>
                    <div style="background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                        <el-table :data="filteredExpenseList" border stripe>
                            <el-table-column prop="reportNo" label="报销单号" width="160"></el-table-column>
                            <el-table-column prop="expenseType" label="报销类型" width="100"></el-table-column>
                            <el-table-column prop="totalAmount" label="报销金额(元)" width="120" align="right">
                                <template #default="{ row }">
                                    ¥{{ row.totalAmount.toFixed(2) }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="description" label="报销说明" show-overflow-tooltip></el-table-column>
                            <el-table-column prop="status" label="状态" width="140">
                                <template #default="{ row }">
                                    <el-tag :type="getStatusType(row.status)" effect="light" class="status-tag">
                                        {{ getStatusText(row.status) }}
                                    </el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column prop="createTime" label="提交时间" width="160">
                                <template #default="{ row }">
                                    {{ formatDateTime(row.createTime) }}
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" width="260" fixed="right">
                                <template #default="{ row }">
                                    <!-- 经理：仅可审批待处理单据 -->
                                    <template v-if="currentRole === 3 && row.status === 'PENDING'">
                                        <el-button type="success" size="small" @click="handleApprove(row, true)">
                                            审批通过
                                        </el-button>
                                        <el-button type="danger" size="small" @click="showRejectDialog(row)">
                                            拒绝
                                        </el-button>
                                    </template>
                                    <!-- 财务：仅可审批经理已通过单据 -->
                                    <template v-if="currentRole === 5 && row.status === 'MANAGER_APPROVED'">
                                        <el-button type="success" size="small" @click="handleApprove(row, true)">
                                            审批通过
                                        </el-button>
                                        <el-button type="danger" size="small" @click="showRejectDialog(row)">
                                            拒绝
                                        </el-button>
                                    </template>
                                    <!-- 查看审批记录 -->
                                    <el-button type="primary" size="small" text @click="viewRecords(row)">
                                        审批记录
                                    </el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div v-if="filteredExpenseList.length === 0" style="text-align: center; padding: 40px; color: #999;">
                            暂无报销单数据
                        </div>
                    </div>
                </el-tab-pane>

                <!-- 我的申请 -->
                <el-tab-pane label="我的申请" name="my" v-if="currentRole === 2">
                    <div class="page-header">
                        <h2>📄 我提交的报销申请</h2>
                    </div>
                    <div style="background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                        <el-table :data="myExpenseList" border stripe>
                            <el-table-column prop="reportNo" label="报销单号" width="160"></el-table-column>
                            <el-table-column prop="expenseType" label="报销类型" width="100"></el-table-column>
                            <el-table-column prop="totalAmount" label="报销金额(元)" width="120" align="right">
                                <template #default="{ row }">
                                    ¥{{ row.totalAmount.toFixed(2) }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="description" label="报销说明" show-overflow-tooltip></el-table-column>
                            <el-table-column prop="status" label="状态" width="140">
                                <template #default="{ row }">
                                    <el-tag :type="getStatusType(row.status)" effect="light" class="status-tag">
                                        {{ getStatusText(row.status) }}
                                    </el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column prop="createTime" label="提交时间" width="160">
                                <template #default="{ row }">
                                    {{ formatDateTime(row.createTime) }}
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" width="100" fixed="right">
                                <template #default="{ row }">
                                    <el-button type="primary" size="small" text @click="viewRecords(row)">
                                        审批记录
                                    </el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div v-if="myExpenseList.length === 0" style="text-align: center; padding: 40px; color: #999;">
                            暂无申请记录
                        </div>
                    </div>
                </el-tab-pane>

                <!-- 待我审批 -->
                <el-tab-pane label="待我审批" name="pending" v-if="currentRole === 3 || currentRole === 5">
                    <div class="page-header">
                        <h2>⏳ 待我审批的报销单</h2>
                    </div>
                    <div style="background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                        <el-table :data="pendingForMeList" border stripe>
                            <el-table-column prop="reportNo" label="报销单号" width="160"></el-table-column>
                            <el-table-column prop="expenseType" label="报销类型" width="100"></el-table-column>
                            <el-table-column prop="totalAmount" label="报销金额(元)" width="120" align="right">
                                <template #default="{ row }">
                                    ¥{{ row.totalAmount.toFixed(2) }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="description" label="报销说明" show-overflow-tooltip></el-table-column>
                            <el-table-column prop="empName" label="申请人" width="100"></el-table-column>
                            <el-table-column prop="createTime" label="提交时间" width="160">
                                <template #default="{ row }">
                                    {{ formatDateTime(row.createTime) }}
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" width="200" fixed="right">
                                <template #default="{ row }">
                                    <el-button type="success" size="small" @click="handleApprove(row, true)">
                                        审批通过
                                    </el-button>
                                    <el-button type="danger" size="small" @click="showRejectDialog(row)">
                                        拒绝
                                    </el-button>
                                    <el-button type="primary" size="small" text @click="viewRecords(row)">
                                        记录
                                    </el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div v-if="pendingForMeList.length === 0" style="text-align: center; padding: 40px; color: #999;">
                            暂无待审批单据
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>

            <!-- 审批记录弹窗 -->
            <el-dialog v-model="showRecordsDialog" title="审批历史记录" width="600px">
                <div v-if="selectedReport" style="margin-bottom: 16px;">
                    <el-descriptions :column="2" border>
                        <el-descriptions-item label="报销单号">{{ selectedReport.reportNo }}</el-descriptions-item>
                        <el-descriptions-item label="报销类型">{{ selectedReport.expenseType }}</el-descriptions-item>
                        <el-descriptions-item label="报销金额">¥{{ selectedReport.totalAmount.toFixed(2) }}</el-descriptions-item>
                        <el-descriptions-item label="当前状态">
                            <el-tag :type="getStatusType(selectedReport.status)" size="small">
                                {{ getStatusText(selectedReport.status) }}
                            </el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="报销说明" :span="2">{{ selectedReport.description }}</el-descriptions-item>
                    </el-descriptions>
                </div>
                <el-timeline v-if="approvalRecords.length > 0">
                    <el-timeline-item
                        v-for="(item, index) in approvalRecords"
                        :key="index"
                        :timestamp="formatDateTime(item.createTime)"
                        :type="item.result === 1 ? 'success' : 'danger'"
                        placement="top"
                    >
                        <h4>{{ item.approverName }} · {{ item.result === 1 ? '审批通过' : '审批拒绝' }}</h4>
                        <p style="color: #606266; margin-top: 4px">{{ item.opinion || '无审批意见' }}</p>
                    </el-timeline-item>
                </el-timeline>
                <div v-else style="text-align: center; padding: 20px; color: #999;">
                    暂无审批记录
                </div>
            </el-dialog>

            <!-- 拒绝弹窗 -->
            <el-dialog v-model="showRejectDialogVisible" title="拒绝审批" width="400px">
                <el-form :model="rejectForm" label-width="80px">
                    <el-form-item label="拒绝原因">
                        <el-input v-model="rejectForm.opinion" type="textarea" :rows="3" placeholder="请填写拒绝原因"></el-input>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="showRejectDialogVisible = false">取消</el-button>
                    <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
                </template>
            </el-dialog>
        </div>
    `,
    setup() {
        const { ref, computed, onMounted } = Vue;
        const { ElMessage } = ElementPlus;

        const activeTab = ref('list');
        const currentRole = ref(2);
        const statusFilter = ref('');

        const expenseList = ref([]);
        const myExpenseList = ref([]);
        const pendingForMeList = ref([]);
        const approvalRecords = ref([]);
        const selectedReport = ref(null);

        const showRecordsDialog = ref(false);
        const showRejectDialogVisible = ref(false);
        const rejectForm = ref({ opinion: '' });
        const rejectingReport = ref(null);

        const expenseForm = ref({
            expenseType: '',
            totalAmount: 0,
            description: ''
        });

        const filteredExpenseList = computed(() => {
            if (!statusFilter.value) return expenseList.value;
            return expenseList.value.filter(item => item.status === statusFilter.value);
        });

        const getStatusType = (status) => {
            const map = {
                'PENDING': 'warning',
                'MANAGER_APPROVED': 'primary',
                'COMPLETED': 'success',
                'REJECTED': 'danger'
            };
            return map[status] || 'info';
        };

        const getStatusText = (status) => {
            const map = {
                'PENDING': '待经理审批',
                'MANAGER_APPROVED': '待财务审批',
                'COMPLETED': '已完成',
                'REJECTED': '已拒绝'
            };
            return map[status] || status;
        };

        const formatDateTime = (datetimeStr) => {
            if (!datetimeStr) return '';
            const d = new Date(datetimeStr);
            return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
        };

        const onRoleChange = (val) => {
            const roleMap = { 2: '普通员工', 3: '部门经理', 5: '财务管理员' };
            ElMessage.info(`已切换为【${roleMap[val]}】视角`);
            // 根据角色调整默认标签页
            if (val === 2) {
                activeTab.value = 'submit';
            } else if (val === 3 || val === 5) {
                activeTab.value = 'pending';
            } else {
                activeTab.value = 'list';
            }
            loadData();
        };

        const loadData = async () => {
            await loadExpenseList();
            if (currentRole.value === 2) {
                await loadMyExpenseList();
            }
            if (currentRole.value === 3 || currentRole.value === 5) {
                await loadPendingForMe();
            }
        };

        const loadExpenseList = async () => {
            try {
                const res = await axios.get('/api/expense/report/list');
                if (res.data.code === 200) {
                    expenseList.value = res.data.data || [];
                }
            } catch (e) {
                console.error('加载报销单列表失败', e);
                // 使用模拟数据作为后备
                expenseList.value = getMockData();
            }
        };

        const loadMyExpenseList = async () => {
            try {
                const res = await axios.get('/api/expense/report/my');
                if (res.data.code === 200) {
                    myExpenseList.value = res.data.data || [];
                }
            } catch (e) {
                console.error('加载我的报销单失败', e);
                // 使用模拟数据作为后备
                myExpenseList.value = getMockData().filter(item => item.empId === 2);
            }
        };

        const loadPendingForMe = async () => {
            try {
                const res = await axios.get('/api/expense/report/pending');
                if (res.data.code === 200) {
                    pendingForMeList.value = res.data.data || [];
                }
            } catch (e) {
                console.error('加载待审批列表失败', e);
                // 使用模拟数据作为后备
                const targetStatus = currentRole.value === 3 ? 'PENDING' : 'MANAGER_APPROVED';
                pendingForMeList.value = getMockData().filter(item => item.status === targetStatus);
            }
        };

        const getMockData = () => {
            return [
                {
                    id: 1,
                    reportNo: 'BX20260630001',
                    expenseType: '差旅费',
                    totalAmount: 2350,
                    description: '北京出差差旅费用',
                    status: 'COMPLETED',
                    empId: 2,
                    empName: '张三',
                    createTime: '2026-06-28T09:30:00'
                },
                {
                    id: 2,
                    reportNo: 'BX20260630002',
                    expenseType: '招待费',
                    totalAmount: 1680,
                    description: '客户招待餐饮费用',
                    status: 'PENDING',
                    empId: 2,
                    empName: '王五',
                    createTime: '2026-06-29T10:15:00'
                },
                {
                    id: 3,
                    reportNo: 'BX20260630003',
                    expenseType: '办公费',
                    totalAmount: 890,
                    description: '办公耗材采购费用',
                    status: 'MANAGER_APPROVED',
                    empId: 3,
                    empName: '孙七',
                    createTime: '2026-06-29T14:20:00'
                },
                {
                    id: 4,
                    reportNo: 'BX20260630004',
                    expenseType: '交通费',
                    totalAmount: 1200,
                    description: '税务办理出差费用',
                    status: 'REJECTED',
                    empId: 4,
                    empName: '周八',
                    createTime: '2026-06-27T11:00:00'
                }
            ];
        };

        const submitExpense = async () => {
            if (!expenseForm.value.expenseType || expenseForm.value.totalAmount <= 0) {
                ElMessage.warning('请完整填写报销信息');
                return;
            }
            try {
                const res = await axios.post('/api/expense/report', {
                    expenseType: expenseForm.value.expenseType,
                    totalAmount: expenseForm.value.totalAmount,
                    description: expenseForm.value.description
                });
                if (res.data.code === 200) {
                    ElMessage.success('报销申请提交成功，状态：待经理审批');
                    resetForm();
                    loadData();
                } else {
                    ElMessage.error(res.data.message || '提交失败');
                }
            } catch (e) {
                // 模拟提交成功
                const newId = Date.now();
                const reportNo = 'BX' + new Date().toISOString().slice(0,10).replace(/-/g,'') + String(Math.floor(Math.random()*1000)).padStart(3,'0');
                expenseList.value.unshift({
                    id: newId,
                    reportNo: reportNo,
                    expenseType: expenseForm.value.expenseType,
                    totalAmount: expenseForm.value.totalAmount,
                    description: expenseForm.value.description,
                    status: 'PENDING',
                    empId: 2,
                    empName: '当前用户',
                    createTime: new Date().toISOString()
                });
                ElMessage.success('报销申请提交成功，状态：待经理审批（模拟）');
                resetForm();
            }
        };

        const resetForm = () => {
            expenseForm.value = {
                expenseType: '',
                totalAmount: 0,
                description: ''
            };
        };

        const handleApprove = async (row, isApprove) => {
            if (isApprove) {
                try {
                    const res = await axios.post(`/api/expense/report/${row.id}/approve`, {
                        result: 1,
                        opinion: '同意'
                    });
                    if (res.data.code === 200) {
                        ElMessage.success('审批通过，单据状态已更新');
                        loadData();
                    } else {
                        ElMessage.error(res.data.message || '审批失败');
                    }
                } catch (e) {
                    // 模拟审批
                    updateStatus(row, true);
                    ElMessage.success('审批通过（模拟）');
                }
            } else {
                rejectingReport.value = row;
                rejectForm.value.opinion = '';
                showRejectDialogVisible.value = true;
            }
        };

        const confirmReject = async () => {
            if (!rejectingReport.value) return;
            try {
                const res = await axios.post(`/api/expense/report/${rejectingReport.value.id}/approve`, {
                    result: 2,
                    opinion: rejectForm.value.opinion
                });
                if (res.data.code === 200) {
                    ElMessage.success('已拒绝，单据状态已更新');
                    showRejectDialogVisible.value = false;
                    loadData();
                } else {
                    ElMessage.error(res.data.message || '操作失败');
                }
            } catch (e) {
                // 模拟拒绝
                updateStatus(rejectingReport.value, false);
                showRejectDialogVisible.value = false;
                ElMessage.success('已拒绝（模拟）');
            }
        };

        const updateStatus = (row, isApprove) => {
            let newStatus = '';
            if (currentRole.value === 3) {
                newStatus = isApprove ? 'MANAGER_APPROVED' : 'REJECTED';
            } else if (currentRole.value === 5) {
                newStatus = isApprove ? 'COMPLETED' : 'REJECTED';
            }
            // 更新列表中的状态
            const item = expenseList.value.find(i => i.id === row.id);
            if (item) item.status = newStatus;
            const myItem = myExpenseList.value.find(i => i.id === row.id);
            if (myItem) myItem.status = newStatus;
            const pendingItem = pendingForMeList.value.find(i => i.id === row.id);
            if (pendingItem) {
                pendingForMeList.value = pendingForMeList.value.filter(i => i.id !== row.id);
            }
        };

        const viewRecords = async (row) => {
            selectedReport.value = row;
            showRecordsDialog.value = true;
            try {
                const res = await axios.get(`/api/expense/report/${row.id}/records`);
                if (res.data.code === 200) {
                    approvalRecords.value = res.data.data || [];
                }
            } catch (e) {
                console.error('加载审批记录失败', e);
                // 模拟审批记录
                approvalRecords.value = getMockRecords(row.id);
            }
        };

        const getMockRecords = (id) => {
            const baseRecords = [
                { approverName: '张三(员工)', result: 1, opinion: '提交报销申请', createTime: '2026-06-28T09:30:00' }
            ];
            if (id === 1) {
                baseRecords.push(
                    { approverName: '李四(部门经理)', result: 1, opinion: '同意报销，请走财务流程', createTime: '2026-06-28T10:15:00' },
                    { approverName: '赵六(财务管理员)', result: 1, opinion: '发票合规，金额无误，同意付款', createTime: '2026-06-28T11:00:00' }
                );
            } else if (id === 3) {
                baseRecords.push(
                    { approverName: '李四(部门经理)', result: 1, opinion: '办公必需，同意提交财务', createTime: '2026-06-29T16:30:00' }
                );
            } else if (id === 4) {
                baseRecords.push(
                    { approverName: '李四(部门经理)', result: 2, opinion: '出差事由不明确，请补充说明', createTime: '2026-06-27T15:40:00' }
                );
            }
            return baseRecords;
        };

        onMounted(() => {
            loadData();
        });

        return {
            activeTab,
            currentRole,
            statusFilter,
            expenseList,
            myExpenseList,
            pendingForMeList,
            filteredExpenseList,
            approvalRecords,
            selectedReport,
            showRecordsDialog,
            showRejectDialogVisible,
            rejectForm,
            expenseForm,
            getStatusType,
            getStatusText,
            formatDateTime,
            onRoleChange,
            submitExpense,
            resetForm,
            handleApprove,
            confirmReject,
            viewRecords,
            showRejectDialog: (row) => {
                rejectingReport.value = row;
                rejectForm.value.opinion = '';
                showRejectDialogVisible.value = true;
            }
        };
    }
};

app.component('page-approval', ApprovalPage);