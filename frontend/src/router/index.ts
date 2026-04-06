import { createRouter, createWebHistory } from 'vue-router'
import AppShell from '../layouts/AppShell.vue'
import BookDetailPage from '../pages/BookDetailPage.vue'
import BookEditorPage from '../pages/BookEditorPage.vue'
import DashboardPage from '../pages/DashboardPage.vue'
import LibraryPage from '../pages/LibraryPage.vue'
import ScanPage from '../pages/ScanPage.vue'
import SearchPage from '../pages/SearchPage.vue'
import ShelvesPage from '../pages/ShelvesPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppShell,
      children: [
        { path: '', name: 'dashboard', component: DashboardPage },
        { path: 'library', name: 'library', component: LibraryPage },
        { path: 'library/new', name: 'book-create', component: BookEditorPage },
        { path: 'library/:id', name: 'book-detail', component: BookDetailPage, props: true },
        { path: 'library/:id/edit', name: 'book-edit', component: BookEditorPage, props: true },
        { path: 'scan', name: 'scan', component: ScanPage },
        { path: 'search', name: 'search', component: SearchPage },
        { path: 'shelves', name: 'shelves', component: ShelvesPage },
      ],
    },
  ],
})

export default router
