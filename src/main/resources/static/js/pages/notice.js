const NoticePage = {
    template: `
        <div class="notice-page">
            <div class="page-header">
                <div class="page-title">
                    <span class="bell-icon">🔔</span>
                    消息通知中心
                </div>
                <div class="page-operate">
                    <span class="unread-count-text">{{ unreadCount }}条未读</span>
                    <el-button type="danger" plain @click="readAll">一键已读</el-button>
                    <el-button type="primary" @click="showPublishDialog = true">发布公告</el-button>
                </div>
            </div>

            <div style="background: #fff; border-radius: 8px; padding: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
                <h5 class="msg-list-title">消息列表</h5>
                <div v-if="noticeList.length === 0" style="text-align: center; padding: 60px; color: #999;">
                    暂无通知消息
                </div>
                <div v-else>
                    <div v-for="notice in noticeList" :key="notice.id" 
                         class="msg-item" :class="{ unread: !notice.read }"
                         @click="viewDetail(notice)">
                        <div class="msg-top-row">
                            <div class="msg-title-row">
                                <span v-if="!notice.read" class="dot-unread"></span>
                                <div class="msg-title">{{ notice.title }}</div>
                            </div>
                            <div class="msg-time">{{ formatTime(notice.createTime) }}</div>
                        </div>
                        <div class="msg-desc">{{ truncateContent(notice.content) }}</div>
                    </div>
                </div>
            </div>

            <el-dialog v-model="showPublishDialog" title="发布新公告" width="900px" 
                       @open="initEditor" @close="destroyEditor">
                <el-form :model="publishForm" label-width="120px">
                    <el-form-item label="通知标题" required>
                        <el-input v-model="publishForm.title" placeholder="请输入公告标题"></el-input>
                    </el-form-item>
                    <el-form-item label="通用接收人群">
                        <el-select v-model="publishForm.group" placeholder="不选择通用人群">
                            <el-option label="普通员工" value="staff"></el-option>
                            <el-option label="部门经理" value="manager"></el-option>
                            <el-option label="财务管理员" value="finance"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="指定单独接收人员">
                        <div>
                            <div v-for="(person, index) in publishForm.persons" :key="index" class="custom-person-row">
                                <el-input v-model="publishForm.persons[index]" placeholder="输入员工姓名/工号" size="small"></el-input>
                                <el-button v-if="publishForm.persons.length > 1" type="danger" size="small" icon="Delete" @click="removePerson(index)"></el-button>
                            </div>
                            <el-button type="primary" size="small" plain style="margin-top: 8px;" @click="addPerson">+ 添加人员</el-button>
                        </div>
                    </el-form-item>
                    <el-form-item label="公告内容" required>
                        <div id="editor-container" style="height: 400px;"></div>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="showPublishDialog = false; resetForm()">取消</el-button>
                    <el-button type="primary" @click="submitPublish">确认发布</el-button>
                </template>
            </el-dialog>

            <el-dialog v-model="showDetailDialog" title="消息详情" width="700px">
                <div v-if="currentNotice">
                    <h3 style="font-size: 20px; font-weight: 600; margin-bottom: 15px;">{{ currentNotice.title }}</h3>
                    <div style="color: #909399; margin-bottom: 20px;">{{ formatTime(currentNotice.createTime) }}</div>
                    <div style="line-height: 1.8; font-size: 16px;" v-html="currentNotice.content"></div>
                </div>
                <template #footer>
                    <el-button @click="showDetailDialog = false">关闭</el-button>
                </template>
            </el-dialog>
        </div>
    `,
    setup() {
        const { ref, onMounted } = Vue;
        const { ElMessage } = ElementPlus;

        const noticeList = ref([]);
        const unreadCount = ref(0);
        const showPublishDialog = ref(false);
        const showDetailDialog = ref(false);
        const currentNotice = ref(null);

        const publishForm = ref({
            title: '',
            group: '',
            persons: ['']
        });

        let editor = null;

        const loadNotices = async () => {
            try {
                const res = await axios.get('/api/notice');
                if (res.data.code === 200) {
                    noticeList.value = res.data.data;
                    updateUnreadCount();
                }
            } catch (e) {
                console.error('加载通知列表失败', e);
            }
        };

        const updateUnreadCount = async () => {
            try {
                const res = await axios.get('/api/notice/unread/count');
                if (res.data.code === 200) {
                    unreadCount.value = res.data.data.count;
                }
            } catch (e) {}
        };

        const readAll = async () => {
            try {
                await axios.post('/api/notice/read-all');
                noticeList.value.forEach(n => n.read = true);
                unreadCount.value = 0;
                ElMessage.success('已全部标记为已读');
            } catch (e) {
                ElMessage.error('操作失败');
            }
        };

        const viewDetail = async (notice) => {
            currentNotice.value = notice;
            showDetailDialog.value = true;
            if (!notice.read) {
                try {
                    await axios.post(`/api/notice/${notice.id}/read`);
                    notice.read = true;
                    updateUnreadCount();
                } catch (e) {}
            }
        };

        const initEditor = () => {
            if (!window.wangEditor) {
                console.error('WangEditor not loaded');
                return;
            }
            const { createEditor, createToolbar } = window.wangEditor;
            const container = document.getElementById('editor-container');
            if (!container) {
                console.error('Editor container not found');
                return;
            }
            
            container.innerHTML = '<div id="toolbar-container"></div><div id="editor-content"></div>';
            
            const editorContent = document.getElementById('editor-content');
            if (editorContent) {
                editorContent.style.height = '350px';
            }
            
            editor = createEditor({
                selector: '#editor-content',
                html: '',
            });
            
            createToolbar({
                editor,
                selector: '#toolbar-container',
                toolbarConfig: {
                    toolbarKeys: [
                        'bold', 'underline', 'italic', 'strikethrough',
                        'color', 'bgColor',
                        'head', 'fontSize',
                        'list', 'todo',
                        'alignLeft', 'alignCenter', 'alignRight', 'alignJustify',
                        'lineHeight',
                        'indent', 'delIndent',
                        'table', 'divider',
                        'link', 'image',
                        'undo', 'redo'
                    ]
                }
            });
        };

        const destroyEditor = () => {
            if (editor) {
                editor.destroy();
                editor = null;
            }
            const container = document.getElementById('editor-container');
            if (container) {
                container.innerHTML = '';
            }
        };

        const addPerson = () => {
            publishForm.value.persons.push('');
        };

        const removePerson = (index) => {
            if (publishForm.value.persons.length > 1) {
                publishForm.value.persons.splice(index, 1);
            }
        };

        const resetForm = () => {
            publishForm.value.title = '';
            publishForm.value.group = '';
            publishForm.value.persons = [''];
            if (editor) {
                editor.setHtml('');
            }
        };

        const submitPublish = async () => {
            if (!publishForm.value.title.trim()) {
                ElMessage.warning('请输入公告标题');
                return;
            }
            const content = editor ? editor.getHtml() : '';
            if (!content.trim()) {
                ElMessage.warning('请填写公告内容');
                return;
            }
            try {
                const res = await axios.post('/api/notice', {
                    title: publishForm.value.title,
                    content: content,
                    type: 1,
                    status: 1
                });
                if (res.data.code === 200) {
                    ElMessage.success('公告发布成功');
                    showPublishDialog.value = false;
                    resetForm();
                    loadNotices();
                } else {
                    ElMessage.error(res.data.msg || res.data.message || '发布失败');
                }
            } catch (e) {
                console.error('发布公告失败', e);
                let msg = '发布失败';
                if (e.response) {
                    msg = e.response.data?.msg || e.response.data?.message || msg;
                }
                ElMessage.error(msg);
            }
        };

        const formatTime = (dateTime) => {
            if (!dateTime) return '-';
            const date = new Date(dateTime);
            const now = new Date();
            const diff = now.getTime() - date.getTime();
            const minutes = Math.floor(diff / 60000);
            const hours = Math.floor(diff / 3600000);
            const days = Math.floor(diff / 86400000);
            
            if (minutes < 1) return '刚刚';
            if (minutes < 60) return minutes + '分钟前';
            if (hours < 24) return hours + '小时前';
            if (days < 7) return days + '天前';
            
            return date.toLocaleDateString('zh-CN');
        };

        const truncateContent = (content) => {
            if (!content) return '';
            const text = content.replace(/<[^>]*>/g, '');
            return text.length > 100 ? text.substring(0, 100) + '...' : text;
        };

        onMounted(() => {
            loadNotices();
        });

        return {
            noticeList,
            unreadCount,
            showPublishDialog,
            showDetailDialog,
            currentNotice,
            publishForm,
            loadNotices,
            readAll,
            viewDetail,
            initEditor,
            destroyEditor,
            addPerson,
            removePerson,
            resetForm,
            submitPublish,
            formatTime,
            truncateContent
        };
    }
};

if (window.appInstance) {
    window.appInstance.component('page-notice', NoticePage);
}
