<script setup>
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '../api'
import TopNav from '../components/TopNav.vue'

const props = defineProps({ initialMode: { type: String, default: 'login' } })
const router = useRouter()
const mode = ref(props.initialMode)
const loading = ref(false)
const message = ref('')
const successMessage = ref('')
const form = reactive({ username: '', password: '' })

watch(
  () => props.initialMode,
  (value) => {
    mode.value = value
    message.value = ''
    successMessage.value = ''
  }
)

async function submit() {
  loading.value = true
  message.value = ''
  successMessage.value = ''
  try {
    const user = mode.value === 'login' ? await login(form) : await register(form)
    localStorage.setItem('microworld-user', JSON.stringify(user))
    localStorage.setItem('microworld-token', user.accessToken)
    successMessage.value = mode.value === 'login' ? '登录成功，欢迎回到你的微世界 ✨' : '注册成功，开始你的第一段故事吧 ♡'
    await new Promise((resolve) => setTimeout(resolve, 1100))
    await router.push('/')
  } catch (error) {
    message.value = error.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page page-shell">
    <TopNav mode="simple" back-to="/" back-label="返回首页" />

    <section class="auth-shell">
      <section class="auth-card cute-panel auth-stage paper-unfold">
        <span class="bookmark-ribbon" aria-hidden="true"></span>
        <span class="paper-sheet" aria-hidden="true"></span>

        <div class="tabs cute-tabs">
          <RouterLink :class="{ active: mode === 'login' }" to="/login">登录</RouterLink>
          <RouterLink :class="{ active: mode === 'register' }" to="/register">注册</RouterLink>
        </div>

        <h2>{{ mode === 'login' ? '继续你的故事旅程' : '创建你的创作者身份' }}</h2>
        <p>用户名为 3–32 位；密码为 6–72 位。</p>

        <form @submit.prevent="submit">
          <label>
            用户名
            <input v-model.trim="form.username" minlength="3" maxlength="32" required autocomplete="username" placeholder="writer_01" />
          </label>

          <label>
            密码
            <input v-model="form.password" type="password" minlength="6" maxlength="72" required autocomplete="current-password" placeholder="至少 6 位" />
          </label>

          <button class="primary cute-primary sparkle-button" :disabled="loading">
            <span>{{ loading ? '处理中…' : mode === 'login' ? '进入微世界' : '创建账户' }}</span>
            <i class="hover-particle star"></i>
            <i class="hover-particle heart"></i>
          </button>
        </form>

        <p v-if="message" class="form-message">{{ message }}</p>
      </section>
    </section>

    <Transition name="toast-pop">
      <div v-if="successMessage" class="success-toast cute-success">
        <span class="success-icon">✦</span>
        <div>
          <strong>操作完成</strong>
          <p>{{ successMessage }}</p>
        </div>
        <span class="success-heart">♡</span>
      </div>
    </Transition>
  </main>
</template>
