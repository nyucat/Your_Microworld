import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AuthView from '../views/AuthView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/login', component: AuthView, props: { initialMode: 'login' } },
    { path: '/register', component: AuthView, props: { initialMode: 'register' } },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})
