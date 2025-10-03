import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Products from '../views/Products.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Cart from '../views/Cart.vue'
import Checkout from '../views/Checkout.vue'
import ProductDetail from '../views/ProductDetail.vue'
import Profile from '../views/Profile.vue'
import Addresses from '../views/Addresses.vue'
import ResetPassword from '../views/ResetPassword.vue'
import Prescriptions from '../views/Prescriptions.vue'
import OrderConfirmation from '../views/OrderConfirmation.vue'

import AdminLogin from '../views/admin/AdminLogin.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import ProductManagement from '../views/admin/ProductManagement.vue'
import InventoryManagement from '../views/admin/InventoryManagement.vue'
import OrderManagement from '../views/admin/OrderManagement.vue'
import PrescriptionValidation from '../views/admin/PrescriptionValidation.vue'
import UserManagement from '../views/admin/UserManagement.vue'

const routes = [
  { 
    path: '/', 
    name: 'Home',
    component: Home 
  },
  { 
    path: '/products', 
    name: 'Products',
    component: Products 
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: ProductDetail
  },
  { 
    path: '/login', 
    name: 'Login',
    component: Login 
  },
  { 
    path: '/register', 
    name: 'Register',
    component: Register 
  },  
  { 
    path: '/cart', 
    name: 'Cart',
    component: Cart 
  },
  { 
    path: '/checkout', 
    name: 'Checkout',
    component: Checkout,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: '/addresses',
    name: 'Addresses',
    component: Addresses,
    meta: { requiresAuth: true }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPassword,
    meta: { requiresAuth: true }
  },
  {
    path: '/prescriptions',
    name: 'Prescriptions',
    component: Prescriptions,
    meta: { requiresAuth: true }
  },
  {
    path: '/order-confirmation',
    name: 'OrderConfirmation',
    component: OrderConfirmation,
    meta: { requiresAuth: true }
  },
  {
    path: '/payment-method',
    name: 'PaymentMethod',
    component: () => import('../components/checkout/PaymentMethod.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin
  },
  {
  path: '/orders',
  name: 'Orders',
  component: () => import('../views/Orders.vue')
  },

  {
    path: '/admin',
    component: AdminDashboard,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboardOverview',
        component: { template: '<div>Bem-vindo ao Painel Administrativo!</div>' } 
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: ProductManagement
      },
      {
        path: 'inventory',
        name: 'AdminInventory',
        component: InventoryManagement
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: OrderManagement
      },
      {
        path: 'prescriptions',
        name: 'AdminPrescriptions',
        component: PrescriptionValidation
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: UserManagement
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('authToken')
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  const isAdmin = user.role === 'admin'
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    if (to.path.startsWith('/admin')) {
      next('/admin/login')
    } else {
      next('/login')
    }
  } else if (to.meta.requiresAdmin && !isAdmin) {
    next('/')
  } else {
    next()
  }
})

export default router
