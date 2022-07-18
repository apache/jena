const getWithId = (array, id) => {
  const idx = array.map(x => x.id).indexOf(id)
  const idMatch = array[idx]
  return [idMatch, idx]
}

export const queryLibraryStore = {
  namespaced: true,
  state: () => ({
    savedQueries: [
      {
        id: '1234',
        name: 'xyz',
        query: 'SELECT *'
      }
    ]
  }),
  getters: {
    listSavedQueries (state) {
      return state.savedQueries.sort((a, b) => a.name.localeCompare(b.name))
    },
    getSavedQuery: (state) => (payload) => {
      const query = getWithId(state.savedQueries, payload)[0]
      return query
    }
  },
  mutations: {
    addSavedQuery (state, payload) {
      const tempSavedQueries = [...state.savedQueries]
      tempSavedQueries.push(payload)
      state.savedQueries = tempSavedQueries
    },
    editSavedQuery (state, payload) {
      const tempSavedQueries = [...state.savedQueries]
      const index = getWithId(tempSavedQueries, payload.id)[1]
      if (index !== -1) {
        tempSavedQueries[index] = payload
        state.savedQueries = tempSavedQueries
      }
    },
    deleteSavedQuery (state, payload) {
      const tempSavedQueries = [...state.savedQueries]
      const index = getWithId(tempSavedQueries, payload)[1]
      if (index !== -1) {
        tempSavedQueries.splice(index, 1)
        state.savedQueries = tempSavedQueries
      }
    }
  },
  actions: {
    addSavedQuery ({ commit }, payload) {
      commit('addSavedQuery', payload)
    },
    editSavedQuery ({ commit }, payload) {
      commit('editSavedQuery', payload)
    },
    deleteSavedQuery ({ commit }, payload) {
      commit('deleteSavedQuery', payload)
    }
  }
}
