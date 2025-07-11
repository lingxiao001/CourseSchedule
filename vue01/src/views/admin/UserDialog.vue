<template>
  <u-popup
    v-model="visible"
    :title="isEdit ? '编辑用户' : '添加用户'"
    width="90%"
    @close="handleClose"
  >
    <u-form
      ref="userForm"
      :model="formData"
      :rules="rules"
      label-width="100px"
      label-position="right"
    >
    
      <u-form-item label="用户名" prop="username">
        <u-input
          v-model="formData.username"
          :disabled="isEdit"
          placeholder="请输入用户名"
        />
      </u-form-item>

      <u-form-item label="姓名" prop="realName">
        <u-input v-model="formData.realName" placeholder="请输入真实姓名" />
      </u-form-item>

      <u-form-item label="角色" prop="role">
        <u-select
          v-model="formData.role"
          placeholder="请选择角色"
          @change="handleRoleChange"
        >
          <u-option label="管理员" value="admin" />
          <u-option label="教师" value="teacher" />
          <u-option label="学生" value="student" />
        </u-select>
      </u-form-item>

      <!-- 学生特定字段 -->
      <template v-if="formData.role === 'student'">
          <u-form-item label="学号" prop="studentId">
    <u-input v-model="formData.studentId" placeholder="请输入学号" />
  </u-form-item>
        <u-form-item label="年级" prop="grade">
          <u-input v-model="formData.grade" placeholder="请输入年级" />
        </u-form-item>

        <u-form-item label="班级" prop="className">
          <u-input v-model="formData.className" placeholder="请输入班级" />
        </u-form-item>
      </template>

      <!-- 教师特定字段 -->
      <template v-if="formData.role === 'teacher'">
        <u-form-item label="职称" prop="title">
          <u-input v-model="formData.title" placeholder="请输入职称" />
        </u-form-item>
         <u-form-item label="工号" prop="teacherId">
    <u-input 
      v-model="formData.teacherId" 
      :disabled="isEdit" 
      placeholder="请输入工号" 
    />
  </u-form-item>
        <u-form-item label="院系" prop="department">
          <u-input v-model="formData.department" placeholder="请输入院系" />
        </u-form-item>
      </template>

      <u-form-item label="密码" prop="password" v-if="!isEdit">
        <u-input
          v-model="formData.password"
          type="password"
          placeholder="请输入密码"
          show-password
        />
      </u-form-item>

      <u-form-item
        label="确认密码"
        prop="confirmPassword"
        v-if="!isEdit"
      >
        <u-input
          v-model="formData.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          show-password
        />
      </u-form-item>
    </u-form>

    <template #footer>
      <u-button @click="visible = false">取消</u-button>
      <u-button type="primary" @click="submitForm">确定</u-button>
    </template>
  </u-popup>
</template>

<script setup>

// 全局 uni 对象定义 - 已移除，使用原生方法替代






import { ref, watch, computed } from 'vue'
// eslint-disable-next-line no-undef
const props = defineProps({
  modelValue: Boolean,
  user: Object,
  isEdit: Boolean
})

// eslint-disable-next-line no-undef
const emit = defineEmits(['update:modelValue', 'submit'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formData = ref({
  id: null,
  username: '',
  realName: '',
  role: '',
  studentId: '',
  teacherId: '',
  roleId: '',
  grade: '',
  className: '',
  title: '',
  department: '',
  password: '',
  confirmPassword: ''
})

const userForm = ref(null)
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  roleId: [
    { required: true, message: '请输入id', trigger: 'blur' }
  ],
  studentId: [
    { required: formData.value.role === 'student', message: '请输入学号', trigger: 'blur' }
  ],
  grade: [
    { required: formData.value.role === 'student', message: '请输入年级', trigger: 'blur' }
  ],
  className: [
    { required: formData.value.role === 'student', message: '请输入班级', trigger: 'blur' }
  ],
  teacherId: [
    { required: formData.value.role === 'teacher', message: '请输入工号', trigger: 'blur' }
  ],
  title: [
    { required: formData.value.role === 'teacher', message: '请输入职称', trigger: 'blur' }
  ],
  department: [
    { required: formData.value.role === 'teacher', message: '请输入院系', trigger: 'blur' }
  ]
}

const resetForm = () => {
  formData.value = {
    id: null,
    username: '',
    realName: '',
    role: '',
    studentId: '',
    teacherId: '',
    grade: '',
    className: '',
    title: '',
    department: '',
    password: '',
    confirmPassword: ''
  }
  if (userForm.value) {
    userForm.value.resetFields()
  }
}

const handleRoleChange = (role) => {
  if (role !== 'student') {
    formData.value.studentId = ''
    formData.value.grade = ''
    formData.value.className = ''
  }
  if (role !== 'teacher') {
    formData.value.teacherId = ''
    formData.value.title = ''
    formData.value.department = ''
  }
}

watch(
  () => props.user,
  (user) => {
    if (user) {
      formData.value = {
        id: user.id,
        username: user.username,
        realName: user.realName,
        role: user.role,
        studentId: user.studentId || '',  // 确保 studentId 正确填充
        teacherId: user.teacherId || '',  // 确保 teacherId 正确填充
        grade: user.grade || '',
        className: user.className || '',
        title: user.title || '',
        department: user.department || '',
        password: '',
        confirmPassword: ''
      }
    } else {
      resetForm()
    }
  },
  { immediate: true }
)

const handleClose = () => {
  resetForm()
}

const submitForm = async () => {
  try {
    await userForm.value.validate()
    
    // 准备提交数据，直接使用 formData 中的字段名
    const submitData = { ...formData.value }
    
    // 如果是编辑模式且密码为空，则移除密码字段
    if (props.isEdit && !submitData.password) {
      delete submitData.password
      delete submitData.confirmPassword
    }
    
    emit('submit', submitData)
    visible.value = false
  } catch (error) {
    window.uni.showToast({ title: '$1', icon: 'error' })('表单验证失败，请检查输入')
  }
}
</script>