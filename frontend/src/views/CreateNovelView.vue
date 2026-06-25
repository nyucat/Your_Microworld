<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createNovel } from '../api'
import TopNav from '../components/TopNav.vue'
import WritingPen from '../components/WritingPen.vue'

const router = useRouter()
const saving = ref(false)
const error = ref('')
const form = reactive({
  title: '',
  type: 'SERIAL',
  description: '',
  microContent: '',
  worldSetting: '',
  outlineContent: '',
  allowIfBranch: true,
  allowBid: true,
  firstChapterTitle: '第一章',
  firstChapterContent: ''
})

const isMicro = computed(() => form.type === 'MICRO')

async function submit() {
  saving.value = true
  error.value = ''
  try {
    const novel = await createNovel(form)
    router.push(`/novels/${novel.id}`)
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <main class="page-shell">
    <TopNav mode="simple" back-to="/published" back-label="返回书库" />

    <form class="editor container cute-panel editor-animated" @submit.prevent="submit">
      <header class="editor-header">
        <div>
          <p class="eyebrow">NEW NOVEL</p>
          <h1>发布你的小说</h1>
          <p>现在可以选择两种创作方式：一口气写完的微小说，或持续展开的连载小说。</p>
        </div>

        <WritingPen />
      </header>

      <label>
        小说标题
        <input v-model.trim="form.title" required maxlength="200" placeholder="给故事一个让人心动的名字" />
      </label>

      <section class="type-selector">
        <p class="eyebrow">NOVEL TYPE</p>
        <div class="type-grid">
          <label class="type-card" :class="{ active: form.type === 'MICRO' }">
            <input v-model="form.type" type="radio" value="MICRO" />
            <strong>微小说</strong>
            <span>只发布一次，直接展示完整正文，不包含章节与后续续写。</span>
          </label>

          <label class="type-card" :class="{ active: form.type === 'SERIAL' }">
            <input v-model="form.type" type="radio" value="SERIAL" />
            <strong>连载小说</strong>
            <span>像现在这样从第一章开始发布，后续可以继续增加章节。</span>
          </label>
        </div>
      </section>

      <label>
        简介
        <textarea v-model.trim="form.description" maxlength="5000" placeholder="这部小说讲述了什么？"></textarea>
      </label>

      <template v-if="isMicro">
        <p class="eyebrow">MICRO STORY</p>
        <label>
          微小说正文
          <textarea
            v-model="form.microContent"
            class="story-input"
            required
            maxlength="100000"
            placeholder="在这里直接写下完整的微小说正文……"
          ></textarea>
        </label>
      </template>

      <template v-else>
        <details>
          <summary>世界观与大纲（可选）</summary>

          <label>
            世界观
            <textarea v-model.trim="form.worldSetting" maxlength="20000" placeholder="人物、时代、规则、情绪基调……"></textarea>
          </label>

          <label>
            故事大纲
            <textarea v-model.trim="form.outlineContent" maxlength="20000" placeholder="可公开展示的大纲内容"></textarea>
          </label>
        </details>

        <div class="checks">
          <label><input v-model="form.allowIfBranch" type="checkbox" /> 允许未来创建 IF 分支</label>
          <label><input v-model="form.allowBid" type="checkbox" /> 允许未来发起主线竞标</label>
        </div>

        <p class="eyebrow">FIRST CHAPTER</p>

        <label>
          第一章标题
          <input v-model.trim="form.firstChapterTitle" required maxlength="200" />
        </label>

        <label>
          第一章正文
          <textarea
            v-model="form.firstChapterContent"
            class="story-input"
            required
            maxlength="100000"
            placeholder="在这里写下故事的开端……"
          ></textarea>
        </label>
      </template>

      <button class="primary publish cute-primary" :disabled="saving">
        {{ saving ? '正在发布…' : isMicro ? '发布微小说' : '发布小说与第一章' }}
      </button>

      <p v-if="error" class="form-message">{{ error }}</p>
    </form>
  </main>
</template>
