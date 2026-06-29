app.component('page-approval', {
    template: `
        <div class="approval-page">
            <div class="page-header">
                <h2>审批管理</h2>
                <div class="header-right">
                    <el-button type="primary" @click="showOvertimeDialog = true">
                        <el-icon><Plus /></el-icon> 加班申请
                    </el-button>
                    <el-button type="primary" @click="showExpenseDialog = true">
                        <el-icon><Plus /></el-icon> 报销申请
                    </el-button>
                </div>
            </div>

            <el-tabs v-model="activeTab" type="border-card">
                <el-tab-pane label="加班申请" name="overtime">
                    <div style="background: #fff; border-radius: 8px; padding: 20px;">
                        <el-table :data="overtimeList" border stripe>
                            <el-table-column prop="overtimeNo" label="加班单号" width="120"></el-table-column>
                            <el-table-column prop="empName" label="员工姓名" width="100"></el-table-column>
                            <el-table-column prop="deptName" label="部门" width="100"></el-table-column>
                            <el-table-column prop="overtimeTypeName" label="加班类型" width="120"></el-table-column>
                            <el-table-column prop="startTime" label="开始时间" width="180">
                                <template #default="scope">{{ formatDateTime(scope.row.startTime) }}</template>
                            </el-table-column>
                            <el-table-column prop="endTime" label="结束时间" width="180">
                                <template #default="scope">{{ formatDateTime(scope.row.endTime) }}</template>
                            </el-table-column>
                            <el-table-column prop="hours" label="时长(小时)" width="100"></el-table-column>
                            <el-table-column prop="reason" label="原因" min-width="150"></el-table-column>
                            <el-table-column prop="statusText" label="状态" width="100">
                                <template #default="scope">
                                    <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.statusText }}</el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column prop="createTime" label="创建时间" width="180">
                                <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
                            </el-table-column>
                            <el-table-column label="操作" width="180">
                                <template #default="scope">
                                    <el-button v-if="scope.row.status === 'PENDING'" size="small" type="success" @click="approveOvertime(scope.row, 1)">通过</el-button>
                                    <el-button v-if="scope.row.status === 'PENDING'" size="small" type="danger" @click="approveOvertime(scope.row, 2)">驳回</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </el-tab-pane>

                <el-tab-pane label="报销申请" name="expense">
                    <div style="background: #fff; border-radius: 8px; padding: 20px;">
                        <el-table :data="expenseList" border stripe>
                            <el-table-column prop="reportNo" label="报销单号" width="120"></el-table-column>
                            <el-table-column prop="empName" label="员工姓名" width="100"></el-table-column>
                            <el-table-column prop="deptName" label="部门" width="100"></el-table-column>
                            <el-table-column prop="expenseType" label="报销类型" width="100"></el-table-column>
                            <el-table-column prop="totalAmount" label="金额" width="100">
                                <template #default="scope">¥{{ scope.row.totalAmount }}</template>
                            </el-table-column>
                            <el-table-column prop="description" label="说明" min-width="200"></el-table-column>
                            <el-table-column prop="statusText" label="状态" width="100">
                                <template #default="scope">
                                    <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.statusText }}</el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column prop="createTime" label="创建时间" width="180">
                                <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
                            </el-table-column>
                            <el-table-column label="操作" width="180">
                                <template #default="scope">
                                    <el-button v-if="scope.row.status === 'PENDING'" size="small" type="success" @click="approveExpense(scope.row, 1)">通过</el-button>
                                    <el-button v-if="scope.row.status === 'PENDING'" size="small" type="danger" @click="approveExpense(scope.row, 2)">驳回</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </el-tab-pane>
            </el-tabs>

            <el-dialog v-model="showOvertimeDialog" title="加班申请" width="500px">
                <el-form :model="overtimeForm" label-width="100px">
                    <el-form-item label="加班类型">
                        <el-select v-model="overtimeForm.overtimeType" placeholder="请选择加班类型">
                            <el-option :label="'工作日加班'" :value="1"></el-option>
                            <el-option :label="'周末加班'" :value="2"></el-option>
                            <el-option :label="'节假日加班'" :value="3"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="开始时间">
                        <el-date-picker v-model="overtimeForm.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%;"></el-date-picker>
                    </el-form-item>
                    <el-form-item label="结束时间">
                        <el-date-picker v-model="overtimeForm.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%;"></el-date-picker>
                    </el-form-item>
                    <el-form-item label="加班原因">
                        <el-input v-model="overtimeForm.reason" type="textarea" :rows="3" placeholder="请输入加班原因"></el-input>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="showOvertimeDialog = false">取消</el-button>
                    <el-button type="primary" @click="submitOvertime">提交申请</el-button>
                </template>
            </el-dialog>

            <el-dialog v-model="showExpenseDialog" title="报销申请" width="500px">
                <el-form :model="expenseForm" label-width="100px">
                    <el-form-item label="报销类型">
                        <el-select v-model="expenseForm.expenseType" placeholder="请选择报销类型">
                            <el-option label="差旅费" value="差旅费"></el-option>
                            <el-option label="交通费" value="交通费"></el-option>
                            <el-option label="餐饮费" value="餐饮费"></el-option>
                            <el-option label="办公用品" value="办公用品"></el-option>
                            <el-option label="其他" value="其他"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="报销金额">
                        <el-input v-model="expenseForm.totalAmount" type="number" placeholder="请输入报销金额" prefix="¥"></el-input>
                    </el-form-item>
                    <el-form-item label="报销说明">
                        <el-input v-model="expenseForm.description" type="textarea" :rows="3" placeholder="请输入报销说明"></el-input>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="showExpenseDialog = false">取消</el-button>
                    <el-button type="primary" @click="submitExpense">提交申请</el-button>
                </template>
            </el-dialog>

            <el-dialog v-model="showApproveDialog" title="审批" width="400px">
                <el-form :model="approveForm" label-width="80px">
                    <el-form-item label="审批意见">
                        <el-input v-model="approveForm.opinion" type="textarea" :rows="3" placeholder="请输入审批意见"></el-input>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="showApproveDialog = false">取消</el-button>
                    <el-button type="primary" @click="confirmApprove">确认审批</el-button>
                </template>
            </el-dialog>
        </div>
    `,
    setup() {
        const { ref, onMounted, nextTick } = Vue;
        const { ElMessage } = ElementPlus;

        const activeTab = ref('overtime');
        const overtimeList = ref([]);
        const expenseList = ref([]);
        const showOvertimeDialog = ref(false);
        const showExpenseDialog = ref(false);
        const showApproveDialog = ref(false);

        const overtimeForm = ref({
            overtimeType: null,
            startTime: null,
            endTime: null,
            reason: ''
        });

        const expenseForm = ref({
            expenseType: '',
            totalAmount: null,
            description: ''
        });

        const approveForm = ref({
            id: null,
            type: '',
            result: null,
            opinion: ''
        });

        const loadOvertimeList = async () => {
            try {
                const res = await axios.get('/api/overtime/list');
                if (res.data.code === 200) {
                    overtimeList.value = res.data.data;
                }
            } catch (e) {
                console.error('加载加班列表失败', e);
            }
        };

        const loadExpenseList = async () => {
            try {
                const res = await axios.get('/api/expense/list');
                if (res.data.code === 200) {
                    expenseList.value = res.data.data;
                }
            } catch (e) {
                console.error('加载报销列表失败', e);
            }
        };

        const submitOvertime = async () => {
            if (!overtimeForm.value.overtimeType || !overtimeForm.value.startTime || !overtimeForm.value.endTime) {
                ElMessage.warning('请填写完整信息');
                return;
            }
            try {
                await axios.post('/api/overtime', {
                    overtimeType: overtimeForm.value.overtimeType,
                    startTime: overtimeForm.value.startTime,
                    endTime: overtimeForm.value.endTime,
                    reason: overtimeForm.value.reason
                });
                ElMessage.success('加班申请提交成功');
                showOvertimeDialog.value = false;
                overtimeForm.value = { overtimeType: null, startTime: null, endTime: null, reason: '' };
                loadOvertimeList();
            } catch (e) {
                ElMessage.error('提交失败');
            }
        };

        const submitExpense = async () => {
            if (!expenseForm.value.expenseType || !expenseForm.value.totalAmount) {
                ElMessage.warning('请填写完整信息');
                return;
            }
            try {
                await axios.post('/api/expense', {
                    expenseType: expenseForm.value.expenseType,
                    totalAmount: expenseForm.value.totalAmount,
                    description: expenseForm.value.description
                });
                ElMessage.success('报销申请提交成功');
                showExpenseDialog.value = false;
                expenseForm.value = { expenseType: '', totalAmount: null, description: '' };
                loadExpenseList();
            } catch (e) {
                ElMessage.error('提交失败');
            }
        };

        const approveOvertime = (row, result) => {
            approveForm.value = {
                id: row.id,
                type: 'overtime',
                result: result,
                opinion: ''
            };
            showApproveDialog.value = true;
        };

        const approveExpense = (row, result) => {
            approveForm.value = {
                id: row.id,
                type: 'expense',
                result: result,
                opinion: ''
            };
            showApproveDialog.value = true;
        };

        const confirmApprove = async () => {
            try {
                const url = approveForm.value.type === 'overtime' ? '/api/overtime/approve' : '/api/expense/approve';
                await axios.post(url, {
                    id: approveForm.value.id,
                    result: approveForm.value.result,
                    opinion: approveForm.value.opinion
                });
                ElMessage.success(approveForm.value.result === 1 ? '审批通过' : '审批驳回');
                showApproveDialog.value = false;
                if (approveForm.value.type === 'overtime') {
                    loadOvertimeList();
                } else {
                    loadExpenseList();
                }
            } catch (e) {
                ElMessage.error('审批失败');
            }
        };

        const formatDateTime = (dateTime) => {
            if (!dateTime) return '';
            const d = new Date(dateTime);
            return d.toLocaleString('zh-CN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });
        };

        const getStatusType = (status) => {
            if (status === 'PENDING') return 'warning';
            if (status === 'COMPLETED') return 'success';
            if (status === 'REJECTED') return 'danger';
            return 'info';
        };

        onMounted(() => {
            loadOvertimeList();
            loadExpenseList();
        });

        return {
            activeTab,
            overtimeList,
            expenseList,
            showOvertimeDialog,
            showExpenseDialog,
            showApproveDialog,
            overtimeForm,
            expenseForm,
            approveForm,
            loadOvertimeList,
            loadExpenseList,
            submitOvertime,
            submitExpense,
            approveOvertime,
            approveExpense,
            confirmApprove,
            formatDateTime,
            getStatusType
        };
    }
});