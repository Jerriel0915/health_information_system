import Vue from 'vue'
import Vuex from 'vuex'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login, logout, getInfo } from '@/api/login'

Vue.use(Vuex)

const store = new Vuex.Store({
    state: {
        token: getToken(),
        name: '',
        avatar: '',
        roles: [],
        permissions: []
    },
    mutations: {
        SET_TOKEN: (state, token) => {
            state.token = token
        },
        SET_NAME: (state, name) => {
            state.name = name
        },
        SET_AVATAR: (state, avatar) => {
            state.avatar = avatar
        },
        SET_ROLES: (state, roles) => {
            state.roles = roles
        },
        SET_PERMISSIONS: (state, permissions) => {
            state.permissions = permissions
        }
    },
    actions: {
        // 登录
        Login({ commit }, userInfo) {
            const username = userInfo.username.trim()
            const password = userInfo.password
            const code = userInfo.code || ''
            const uuid = userInfo.uuid || ''
            return new Promise((resolve, reject) => {
                login({ username, password, code, uuid }).then(res => {
                    if (res.code === 200) {
                        const token = res.token
                        setToken(token)
                        commit('SET_TOKEN', token)
                        resolve()
                    } else {
                        reject(res.msg)
                    }
                }).catch(error => {
                    reject(error)
                })
            })
        },

        // 获取用户信息
        GetInfo({ commit, state }) {
            return new Promise((resolve, reject) => {
                getInfo().then(res => {
                    if (res.code === 200) {
                        const user = res.data.user
                        const avatar = user.avatar || ''
                        const name = user.userName || user.nickName
                        commit('SET_NAME', name)
                        commit('SET_AVATAR', avatar)
                        commit('SET_ROLES', res.data.roles)
                        commit('SET_PERMISSIONS', res.data.permissions)
                        resolve(res.data)
                    } else {
                        reject(res.msg)
                    }
                }).catch(error => {
                    reject(error)
                })
            })
        },

        // 退出登录
        LogOut({ commit, state }) {
            return new Promise((resolve, reject) => {
                logout().then(() => {
                    commit('SET_TOKEN', '')
                    commit('SET_ROLES', [])
                    commit('SET_PERMISSIONS', [])
                    removeToken()
                    resolve()
                }).catch(error => {
                    reject(error)
                })
            })
        },

        // 前端退出
        FedLogOut({ commit }) {
            return new Promise(resolve => {
                commit('SET_TOKEN', '')
                removeToken()
                resolve()
            })
        }
    }
})

export default store