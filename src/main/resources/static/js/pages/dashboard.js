const DashboardPage = {
    template: `
        <div class="dashboard-page">
            <el-tabs v-model="activeTab" type="border-card">
                <!-- 工作台首页 -->
                <el-tab-pane label="工作台" name="home">
                    <div style="font-size: 24px; font-weight: 600; margin-bottom: 20px;">欢迎来到工作台</div>
                    <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px;">
                        <div style="background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                            <div style="font-size: 14px; color: #666;">今日待办</div>
                            <div style="font-size: 32px; font-weight: 600; color: #333; margin-top: 10px;">{{ stats.todoCount }}</div>
                        </div>
                        <div style="background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                            <div style="font-size: 14px; color: #666;">待审批</div>
                            <div style="font-size: 32px; font-weight: 600; color: #333; margin-top: 10px;">{{ stats.approvalCount }}</div>
                        </div>
                        <div style="background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                            <div style="font-size: 14px; color: #666;">新通知</div>
                            <div style="font-size: 32px; font-weight: 600; color: #333; margin-top: 10px;">{{ stats.noticeCount }}</div>
                        </div>
                        <div style="background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                            <div style="font-size: 14px; color: #666;">本月考勤</div>
                            <div style="font-size: 32px; font-weight: 600; color: #333; margin-top: 10px;">{{ stats.attendanceDays }}天</div>
                        </div>
                    </div>
                    <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-top: 20px;">
                        <div style="background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                            <div style="font-size: 18px; font-weight: 600; margin-bottom: 15px;">考勤统计图表</div>
                            <div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #999;">图表区域</div>
                        </div>
                        <div style="background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                            <div style="font-size: 18px; font-weight: 600; margin-bottom: 15px;">最近通知</div>
                            <el-timeline>
                                <el-timeline-item>新的审批任务</el-timeline-item>
                                <el-timeline-item>考勤异常提醒</el-timeline-item>
                                <el-timeline-item>系统维护通知</el-timeline-item>
                            </el-timeline>
                        </div>
                    </div>
                </el-tab-pane>

                <!-- RBAC权限演示 -->
                <el-tab-pane label="🔐 RBAC权限演示" name="rbac">
                    <div class="rbac-page">
                        <div class="rbac-header">
                            <h2>🔐 RBAC权限体系演示</h2>
                            <div class="role-select-wrap">
                                <span>当前角色：</span>
                                <el-select v-model="currentRole" style="width: 160px" @change="onRoleChange">
                                    <el-option label="普通员工" :value="2"></el-option>
                                    <el-option label="部门经理" :value="3"></el-option>
                                    <el-option label="HR管理员" :value="4"></el-option>
                                    <el-option label="财务管理员" :value="5"></el-option>
                                    <el-option label="超级管理员" :value="1"></el-option>
                                </el-select>
                            </div>
                        </div>

                        <el-card class="tip-card" shadow="never">
                            <strong>演示说明：</strong>本页面为纯前端模拟RBAC权限效果。切换右上角角色，左侧侧边栏菜单会动态变化（无权限的菜单不显示）；下方员工列表的操作按钮也会根据角色权限显隐，对应数据库中「用户-角色-菜单」五表关联的权限模型。
                        </el-card>

                        <el-card class="section-card" shadow="never">
                            <template #header>
                                <div class="card-title">📋 按钮级权限演示（员工列表）</div>
                            </template>

                            <div class="permission-desc">
                                权限规则：超级管理员 → 全部操作权限；部门经理 → 编辑、查看；HR → 新增、查看；财务 → 仅查看；普通员工 → 仅查看
                            </div>

                            <div class="btn-group">
                                <el-button v-if="hasPerm('user:add')" type="primary" @click="openAddDialog">
                                    <el-icon><Plus /></el-icon> 新增员工
                                </el-button>
                                <el-button v-if="hasPerm('user:edit') && selectedEmployee" type="warning" @click="openEditDialog">
                                    <el-icon><Edit /></el-icon> 编辑
                                </el-button>
                                <el-button v-if="hasPerm('user:delete') && selectedEmployee" type="danger" @click="confirmDelete">
                                    <el-icon><Delete /></el-icon> 删除
                                </el-button>
                                <el-button type="info" @click="openViewDialog" :disabled="!selectedEmployee">
                                    <el-icon><View /></el-icon> 查看详情
                                </el-button>
                            </div>

                            <el-table :data="employeeList" border stripe @selection-change="handleSelectionChange">
                                <el-table-column type="selection" width="50"></el-table-column>
                                <el-table-column prop="empNo" label="工号" width="120"></el-table-column>
                                <el-table-column prop="name" label="姓名" width="100"></el-table-column>
                                <el-table-column prop="dept" label="部门" width="120"></el-table-column>
                                <el-table-column prop="position" label="职位"></el-table-column>
                                <el-table-column prop="entryDate" label="入职日期" width="140"></el-table-column>
                                <el-table-column label="状态" width="100">
                                    <template #default="{ row }">
                                        <el-tag type="success">{{ row.status }}</el-tag>
                                    </template>
                                </el-table-column>
                                <el-table-column label="操作" width="180" fixed="right">
                                    <template #default="{ row }">
                                        <el-button v-if="hasPerm('user:edit')" type="primary" size="small" text @click="openEditDialog(row)">编辑</el-button>
                                        <el-button v-if="hasPerm('user:delete')" type="danger" size="small" text @click="confirmDelete(row)">删除</el-button>
                                        <el-button type="primary" size="small" text @click="openViewDialog(row)">查看</el-button>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </el-card>

                        <el-card class="section-card role-perm-table" shadow="never">
                            <template #header>
                                <div class="card-title">📊 角色-菜单权限对照表（与数据库一致）</div>
                            </template>
                            <el-table :data="rolePermTable" border>
                                <el-table-column prop="roleName" label="角色" width="140"></el-table-column>
                                <el-table-column prop="menus" label="拥有菜单权限"></el-table-column>
                                <el-table-column prop="buttonPerm" label="按钮权限范围"></el-table-column>
                            </el-table>
                        </el-card>

                        <el-card class="section-card" shadow="never">
                            <template #header>
                                <div class="card-title">🏗️ RBAC权限模型架构</div>
                            </template>
                            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px;">
                                <div class="model-card">
                                    <div class="model-title">用户表 (sys_user)</div>
                                    <div class="model-desc">存储员工账号信息</div>
                                    <div class="model-fields">
                                        <span>id</span>
                                        <span>emp_no</span>
                                        <span>username</span>
                                        <span>role_id</span>
                                    </div>
                                </div>
                                <div class="model-card">
                                    <div class="model-title">角色表 (sys_role)</div>
                                    <div class="model-desc">定义角色类型</div>
                                    <div class="model-fields">
                                        <span>id</span>
                                        <span>role_name</span>
                                        <span>description</span>
                                    </div>
                                </div>
                                <div class="model-card">
                                    <div class="model-title">菜单表 (sys_menu)</div>
                                    <div class="model-desc">定义系统菜单和权限点</div>
                                    <div class="model-fields">
                                        <span>id</span>
                                        <span>menu_name</span>
                                        <span>path</span>
                                        <span>perms</span>
                                    </div>
                                </div>
                            </div>
                            <div style="text-align: center; margin-top: 20px; padding: 20px; background: #f5f7fa; border-radius: 8px;">
                                <div style="font-weight: 600; margin-bottom: 10px;">关联关系：用户 → 角色 → 菜单/权限</div>
                                <div style="color: #666;">sys_user.role_id → sys_role.id → sys_role_menu.role_id → sys_menu.id</div>
                            </div>
                        </el-card>
                    </div>
                </el-tab-pane>
            </el-tabs>

            <!-- 新增/编辑对话框 -->
            <el-dialog v-model="formDialogVisible" :title="dialogTitle" width="500px">
                <el-form :model="employeeForm" label-width="80px">
                    <el-form-item label="工号" prop="empNo">
                        <el-input v-model="employeeForm.empNo" :disabled="isEdit"></el-input>
                    </el-form-item>
                    <el-form-item label="姓名" prop="name">
                        <el-input v-model="employeeForm.name"></el-input>
                    </el-form-item>
                    <el-form-item label="部门" prop="dept">
                        <el-select v-model="employeeForm.dept" style="width: 100%">
                            <el-option label="研发部" value="研发部"></el-option>
                            <el-option label="市场部" value="市场部"></el-option>
                            <el-option label="产品部" value="产品部"></el-option>
                            <el-option label="财务部" value="财务部"></el-option>
                            <el-option label="人事部" value="人事部"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="职位" prop="position">
                        <el-input v-model="employeeForm.position"></el-input>
                    </el-form-item>
                    <el-form-item label="入职日期" prop="entryDate">
                        <el-date-picker v-model="employeeForm.entryDate" type="date" style="width: 100%"></el-date-picker>
                    </el-form-item>
                    <el-form-item label="状态" prop="status">
                        <el-select v-model="employeeForm.status" style="width: 100%">
                            <el-option label="在职" value="在职"></el-option>
                            <el-option label="试用期" value="试用期"></el-option>
                            <el-option label="离职" value="离职"></el-option>
                        </el-select>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="formDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="saveEmployee">{{ isEdit ? '保存修改' : '确认添加' }}</el-button>
                </template>
            </el-dialog>

            <!-- 查看详情对话框 -->
            <el-dialog v-model="viewDialogVisible" title="员工详情" width="500px">
                <el-descriptions :column="2" border v-if="viewEmployee">
                    <el-descriptions-item label="工号">{{ viewEmployee.empNo }}</el-descriptions-item>
                    <el-descriptions-item label="姓名">{{ viewEmployee.name }}</el-descriptions-item>
                    <el-descriptions-item label="部门">{{ viewEmployee.dept }}</el-descriptions-item>
                    <el-descriptions-item label="职位">{{ viewEmployee.position }}</el-descriptions-item>
                    <el-descriptions-item label="入职日期">{{ viewEmployee.entryDate }}</el-descriptions-item>
                    <el-descriptions-item label="状态">
                        <el-tag type="success">{{ viewEmployee.status }}</el-tag>
                    </el-descriptions-item>
                </el-descriptions>
            </el-dialog>

            <!-- 删除确认对话框 -->
            <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
                <p>确定要删除员工 <strong>{{ deleteEmployee?.name }}</strong> 吗？此操作不可恢复。</p>
                <template #footer>
                    <el-button @click="deleteDialogVisible = false">取消</el-button>
                    <el-button type="danger" @click="doDelete">确认删除</el-button>
                </template>
            </el-dialog>
        </div>
    `,
    setup() {
        const { ref, computed, reactive } = Vue;
        const { ElMessage, ElMessageBox } = ElementPlus;

        const activeTab = ref('home');

        const stats = ref({
            todoCount: 5,
            approvalCount: 3,
            noticeCount: 2,
            attendanceDays: 22
        });

        const currentRole = ref(2);
        const activeMenu = ref('/dashboard');

        const allMenus = [
            { id: 1, parentId: 0, menuName: '工作台', path: '/dashboard', icon: 'DataBoard', perms: '' },
            { id: 2, parentId: 0, menuName: '考勤日历', path: '/attendance', icon: 'Calendar', perms: '' },
            { id: 3, parentId: 0, menuName: '审批流', path: '/approval', icon: 'Document', perms: '' },
            { id: 4, parentId: 0, menuName: '通知中心', path: '/notice', icon: 'Bell', perms: '' },
            { id: 5, parentId: 0, menuName: '数据看板', path: '/report', icon: 'DataAnalysis', perms: '' },
            { id: 6, parentId: 0, menuName: '会议预约', path: '/meeting', icon: 'Monitor', perms: '' }
        ];

        const roleMenuMap = {
            1: [1,2,3,4,5,6],
            2: [1,2,4,6],
            3: [1,2,3,4,6],
            4: [1,2,4,5],
            5: [1,3,4,5]
        };

        const roleButtonPermMap = {
            1: ['user:add', 'user:edit', 'user:delete'],
            2: [],
            3: ['user:edit'],
            4: ['user:add'],
            5: []
        };

        const currentMenus = computed(() => {
            const menuIds = roleMenuMap[currentRole.value] || [];
            return allMenus.filter(menu => menuIds.includes(menu.id));
        });

        const hasPerm = (perm) => {
            const perms = roleButtonPermMap[currentRole.value] || [];
            return perms.includes(perm);
        };

        const onRoleChange = (val) => {
            const roleNameMap = {
                1: '超级管理员',
                2: '普通员工',
                3: '部门经理',
                4: 'HR管理员',
                5: '财务管理员'
            };
            ElMessage.success(`已切换为【${roleNameMap[val]}】，菜单与按钮权限已同步更新`);
            if (currentMenus.value.length > 0) {
                activeMenu.value = currentMenus.value[0].path;
            }
        };

        const employeeList = ref([
            { empNo: 'E2024001', name: '张三', dept: '研发部', position: 'Java工程师', entryDate: '2024-03-01', status: '在职' },
            { empNo: 'E2024002', name: '李四', dept: '市场部', position: '销售经理', entryDate: '2023-06-15', status: '在职' },
            { empNo: 'E2024003', name: '王五', dept: '产品部', position: '产品经理', entryDate: '2024-01-10', status: '在职' },
            { empNo: 'E2024004', name: '赵六', dept: '财务部', position: '财务主管', entryDate: '2022-09-01', status: '在职' },
            { empNo: 'E2024005', name: '孙七', dept: '人事部', position: 'HRBP', entryDate: '2023-12-01', status: '试用期' }
        ]);

        const rolePermTable = [
            { roleName: '超级管理员', menus: '工作台、考勤日历、审批流、通知中心、数据看板、会议预约（全部）', buttonPerm: '新增、编辑、删除、查看（全部操作）' },
            { roleName: '普通员工', menus: '工作台、考勤日历、通知中心、会议预约', buttonPerm: '仅查看，无数据操作权限' },
            { roleName: '部门经理', menus: '工作台、考勤日历、审批流、通知中心、会议预约', buttonPerm: '编辑、查看' },
            { roleName: 'HR管理员', menus: '工作台、考勤日历、通知中心、数据看板', buttonPerm: '新增、查看' },
            { roleName: '财务管理员', menus: '工作台、审批流、通知中心、数据看板', buttonPerm: '仅查看' }
        ];

        // 对话框状态
        const formDialogVisible = ref(false);
        const viewDialogVisible = ref(false);
        const deleteDialogVisible = ref(false);
        const dialogTitle = ref('');
        const isEdit = ref(false);
        const selectedEmployee = ref(null);
        const viewEmployee = ref(null);
        const deleteEmployee = ref(null);

        const employeeForm = reactive({
            empNo: '',
            name: '',
            dept: '',
            position: '',
            entryDate: '',
            status: '在职'
        });

        const handleSelectionChange = (val) => {
            selectedEmployee.value = val.length > 0 ? val[0] : null;
        };

        const openAddDialog = () => {
            dialogTitle.value = '新增员工';
            isEdit.value = false;
            employeeForm.empNo = '';
            employeeForm.name = '';
            employeeForm.dept = '';
            employeeForm.position = '';
            employeeForm.entryDate = '';
            employeeForm.status = '在职';
            formDialogVisible.value = true;
        };

        const openEditDialog = (row = null) => {
            const employee = row || selectedEmployee.value;
            if (!employee) {
                ElMessage.warning('请选择要编辑的员工');
                return;
            }
            dialogTitle.value = '编辑员工';
            isEdit.value = true;
            employeeForm.empNo = employee.empNo;
            employeeForm.name = employee.name;
            employeeForm.dept = employee.dept;
            employeeForm.position = employee.position;
            employeeForm.entryDate = employee.entryDate;
            employeeForm.status = employee.status;
            formDialogVisible.value = true;
        };

        const openViewDialog = (row = null) => {
            const employee = row || selectedEmployee.value;
            if (!employee) {
                ElMessage.warning('请选择要查看的员工');
                return;
            }
            viewEmployee.value = employee;
            viewDialogVisible.value = true;
        };

        const confirmDelete = (row = null) => {
            const employee = row || selectedEmployee.value;
            if (!employee) {
                ElMessage.warning('请选择要删除的员工');
                return;
            }
            deleteEmployee.value = employee;
            deleteDialogVisible.value = true;
        };

        const doDelete = () => {
            if (!deleteEmployee.value) return;
            const index = employeeList.value.findIndex(e => e.empNo === deleteEmployee.value.empNo);
            if (index !== -1) {
                employeeList.value.splice(index, 1);
                ElMessage.success('删除成功');
            }
            deleteDialogVisible.value = false;
            deleteEmployee.value = null;
            selectedEmployee.value = null;
        };

        const saveEmployee = () => {
            if (!employeeForm.empNo || !employeeForm.name || !employeeForm.dept) {
                ElMessage.warning('请填写完整信息');
                return;
            }
            if (isEdit.value) {
                const index = employeeList.value.findIndex(e => e.empNo === employeeForm.empNo);
                if (index !== -1) {
                    employeeList.value[index] = { ...employeeForm };
                    ElMessage.success('修改成功');
                }
            } else {
                employeeList.value.push({ ...employeeForm });
                ElMessage.success('新增成功');
            }
            formDialogVisible.value = false;
        };

        return {
            activeTab,
            stats,
            currentRole,
            activeMenu,
            currentMenus,
            hasPerm,
            onRoleChange,
            employeeList,
            rolePermTable,
            formDialogVisible,
            viewDialogVisible,
            deleteDialogVisible,
            dialogTitle,
            isEdit,
            selectedEmployee,
            viewEmployee,
            employeeForm,
            handleSelectionChange,
            openAddDialog,
            openEditDialog,
            openViewDialog,
            confirmDelete,
            doDelete,
            saveEmployee
        };
    }
};

app.component('page-dashboard', DashboardPage);