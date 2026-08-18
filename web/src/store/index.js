import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    sidebarCollapsed: true,
    activeConnection: null,
    connections: []
  },
  getters: {
    isSidebarCollapsed: state => state.sidebarCollapsed,
    activeConnection: state => state.activeConnection,
    connections: state => state.connections
  },
  mutations: {
    SET_SIDEBAR_COLLAPSED(state, val) {
      state.sidebarCollapsed = val
    },
    SET_ACTIVE_CONNECTION(state, conn) {
      state.activeConnection = conn
    },
    SET_CONNECTIONS(state, list) {
      state.connections = list
    }
  },
  actions: {
    toggleSidebar({ commit, state }) {
      commit('SET_SIDEBAR_COLLAPSED', !state.sidebarCollapsed)
    },
    setActiveConnection({ commit }, conn) {
      commit('SET_ACTIVE_CONNECTION', conn)
    },
    setConnections({ commit }, list) {
      commit('SET_CONNECTIONS', list)
    }
  },
  modules: {
  }
})
