import { createStore } from 'vuex'
import VuexPersistence from 'vuex-persist'
import { queryLibraryStore } from './queryLibraryStore'

export default createStore({
  modules: {
    queryLibrary: queryLibraryStore
  },
  plugins: [new VuexPersistence({
    storage: window.localStorage,
    modules: ['queryLibrary']
  }).plugin]
})
