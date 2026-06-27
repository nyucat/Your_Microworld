<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createNovel } from '../api'
import { NOVEL_CATEGORIES } from '../constants/novelCategories'
import TopNav from '../components/TopNav.vue'
import WritingPen from '../components/WritingPen.vue'

const router = useRouter()
const saving = ref(false)
const error = ref('')
const tagInput = ref('')
const form = reactive({
  title: '',
  type: 'SERIAL',
  description: '',
  category: '奇幻',
  microContent: '',
  worldSetting: '',
  outlineContent: '',
  allowIfBranch: true,
  allowBid: true,
  firstChapterTitle: '第一章',
  firstChapterContent: ''
})

const isMicro = computed(() => form.type === 'MICRO')
const parsedTags = computed(() =>
  tagInput.value
    .split(/[，,]/)
    .map((tag) => tag.trim())
    .filter(Boolean)
    .slice(0, 5)
)

const totalWords = computed(() => {
  const content = isMicro.value ? form.microContent : form.firstChapterContent
  return (content || '').replace(/\s+/g, '').length
})

const completionTips = computed(() => {
  const tips = []
  if (!form.title.trim()) tips.push('补充一个更抓人的标题')
  if (!form.description.trim()) tips.push('写一句简介帮助读者理解故事气质')
  if (!parsedTags.value.length) tips.push('添加 1~3 个标签更容易被发现')
  if (isMicro.value && !form.microContent.trim()) tips.push('补上微小说正文')
  if (!isMicro.value && !form.firstChapterContent.trim()) tips.push('补上第一章内容')
  return tips
})

async function submit() {
  saving.value = true
  error.value = ''
  try {
    const novel = await createNovel({
      ...form,
      tags: parsedTags.value
    })
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

    <form class="editor editor-upgraded container cute-panel editor-animated" @submit.prevent="submit">
      <header class="editor-header editor-header-upgraded">
        <div class="editor-hero-copy">
          <p class="eyebrow">NEW NOVEL</p>
          <h1>发布你的小说</h1>
          <p>把灵感整理成一个真正可阅读的故事。先确定作品类型，再一步步补完标题、简介、分类与正文。</p>

          <div class="editor-hero-pills">
            <span class="count-pill">当前类型：{{ isMicro ? '微小说' : '连载小说' }}</span>
            <span class="count-pill">标签 {{ parsedTags.length }}/5</span>
            <span class="count-pill">正文字数 {{ totalWords }}</span>
          </div>
        </div>

        <WritingPen />
      </header>

      <section class="editor-layout">
        <div class="editor-main">
          <section class="editor-section">
            <div class="editor-section-heading">
              <div>
                <p class="eyebrow">BASIC INFO</p>
                <h2>基础信息</h2>
              </div>
              <span class="editor-section-note">先把读者第一眼会看到的信息准备好</span>
            </div>

            <label>
              小说标题
              <input v-model.trim="form.title" required maxlength="200" placeholder="给故事一个让人心动的名字" />
            </label>

            <label>
              简介
              <textarea v-model.trim="form.description" maxlength="5000" placeholder="这部小说讲述了什么？它的气质、人物或冲突是什么？"></textarea>
            </label>

            <div class="editor-meta-grid">
              <label>
                小说分类
                <select v-model="form.category" required>
                  <option v-for="category in NOVEL_CATEGORIES" :key="category" :value="category">
                    {{ category }}
                  </option>
                </select>
              </label>

              <label>
                小说标签
                <input v-model.trim="tagInput" maxlength="120" placeholder="例如：校园，奇幻，成长（最多 5 个）" />
              </label>
            </div>

            <div v-if="parsedTags.length" class="tag-preview editor-tag-preview">
              <span v-for="tag in parsedTags" :key="tag" class="tag-chip">{{ tag }}</span>
            </div>
          </section>

          <section class="editor-section">
            <div class="editor-section-heading">
              <div>
                <p class="eyebrow">NOVEL TYPE</p>
                <h2>选择创作方式</h2>
              </div>
              <span class="editor-section-note">不同类型会影响正文结构与后续更新方式</span>
            </div>

            <div class="type-grid type-grid-upgraded">
              <label class="type-card type-card-upgraded" :class="{ active: form.type === 'MICRO' }">
                <input v-model="form.type" type="radio" value="MICRO" />
                <div class="type-card-top">
                  <strong>微小说</strong>
                  <span class="type-badge">一次发布</span>
                </div>
                <span>适合短篇表达。只发布一次，直接展示完整正文，不包含章节与后续续写。</span>
              </label>

              <label class="type-card type-card-upgraded" :class="{ active: form.type === 'SERIAL' }">
                <input v-model="form.type" type="radio" value="SERIAL" />
                <div class="type-card-top">
                  <strong>连载小说</strong>
                  <span class="type-badge">持续更新</span>
                </div>
                <span>从第一章开始发布，适合中长篇故事，后续可以继续增加章节。</span>
              </label>
            </div>
          </section>

          <section class="editor-section">
            <div class="editor-section-heading">
              <div>
                <p class="eyebrow">{{ isMicro ? 'MICRO STORY' : 'FIRST CHAPTER' }}</p>
                <h2>{{ isMicro ? '正文内容' : '开篇内容' }}</h2>
              </div>
              <span class="editor-section-note">
                {{ isMicro ? '微小说建议尽量一气呵成，完成一个完整情绪弧线' : '第一章负责建立人物、气氛与阅读期待' }}
              </span>
            </div>

            <template v-if="isMicro">
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
              <details class="editor-advanced-box">
                <summary>世界观与大纲（可选）</summary>

                <label>
                  世界观
                  <textarea v-model.trim="form.worldSetting" maxlength="20000" placeholder="人物、时代、规则、情绪基调……"></textarea>
                </label>

                <label>
                  故事大纲
                  <textarea v-model.trim="form.outlineContent" maxlength="20000" placeholder="可以公开展示的大纲内容"></textarea>
                </label>
              </details>

              <div class="checks editor-checks">
                <label><input v-model="form.allowIfBranch" type="checkbox" /> 允许未来创建 IF 分支</label>
                <label><input v-model="form.allowBid" type="checkbox" /> 允许未来发起主线竞标</label>
              </div>

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
          </section>
        </div>

        <aside class="editor-sidebar">
          <section class="cute-panel editor-side-card">
            <p class="eyebrow">PUBLISH CHECK</p>
            <h3>发布前检查</h3>
            <ul class="editor-checklist">
              <li :class="{ done: !!form.title.trim() }">标题已填写</li>
              <li :class="{ done: !!form.description.trim() }">简介已填写</li>
              <li :class="{ done: !!parsedTags.length }">标签已添加</li>
              <li :class="{ done: totalWords > 0 }">{{ isMicro ? '正文已开始撰写' : '第一章已开始撰写' }}</li>
            </ul>
          </section>

          <section class="cute-panel editor-side-card">
            <p class="eyebrow">CREATION TIP</p>
            <h3>当前建议</h3>
            <div v-if="completionTips.length" class="editor-tip-list">
              <span v-for="tip in completionTips" :key="tip" class="tag-chip">{{ tip }}</span>
            </div>
            <p v-else class="editor-side-success">已经很完整啦，可以准备发布了。</p>
          </section>

          <section class="cute-panel editor-side-card">
            <p class="eyebrow">STORY SNAPSHOT</p>
            <h3>作品快照</h3>
            <div class="editor-snapshot">
              <strong>{{ form.title || '未命名作品' }}</strong>
              <span>{{ form.category }} · {{ isMicro ? '微小说' : '连载小说' }}</span>
              <p>{{ form.description || '简介会显示在这里，帮助你快速确认作品对外呈现的感觉。' }}</p>
            </div>
          </section>
        </aside>
      </section>

      <div class="editor-submit-bar">
        <div>
          <strong>{{ isMicro ? '发布后将直接进入微小说详情页' : '发布后将进入小说详情页，可继续追加章节' }}</strong>
          <span>{{ saving ? '正在为你保存作品…' : '确认无误后就让故事开始被看见吧。' }}</span>
        </div>

        <button class="primary publish cute-primary" :disabled="saving">
          {{ saving ? '正在发布…' : isMicro ? '发布微小说' : '发布小说与第一章' }}
        </button>
      </div>

      <p v-if="error" class="form-message">{{ error }}</p>
    </form>
  </main>
</template>
