import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AuthView from '../views/AuthView.vue'
import CreateNovelView from '../views/CreateNovelView.vue'
import NovelDetailView from '../views/NovelDetailView.vue'
import ChapterReaderView from '../views/ChapterReaderView.vue'
import UserProfileView from '../views/UserProfileView.vue'
import AuthorInboxView from '../views/AuthorInboxView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView, props: { pageType: 'home' } },
    { path: '/published', component: HomeView, props: { pageType: 'published' } },
    { path: '/categories', component: HomeView, props: { pageType: 'categories' } },
    { path: '/login', component: AuthView, props: { initialMode: 'login' } },
    { path: '/register', component: AuthView, props: { initialMode: 'register' } },
    { path: '/novels/create', component: CreateNovelView, meta: { requiresAuth: true } },
    { path: '/novels/:id', component: NovelDetailView, props: true },
    { path: '/chapters/:id', component: ChapterReaderView, props: true },
    { path: '/users/:id/inbox', component: AuthorInboxView, props: true, meta: { requiresAuth: true } },
    { path: '/users/:id', component: UserProfileView, props: true },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

router.beforeEach((to) => (to.meta.requiresAuth && !localStorage.getItem('microworld-token') ? '/login' : true))

export default router
