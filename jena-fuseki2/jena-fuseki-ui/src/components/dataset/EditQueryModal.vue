<template>
  <b-modal title="Edit SPARQL query" size="lg" :id="id" v-model="show" @hide="$emit('editQueryModalHide')">
    <b-form-group label="Name">
      <b-form-input v-model="editQuery.name" type="text" placeholder="Query name"></b-form-input>
    </b-form-group>
    <b-spinner v-if="loading"></b-spinner>
    <div id="edit-yasqe"></div>
    <template #modal-footer>
      <b-button variant="outline-secondary" @click="show = false">
        Cancel
      </b-button>
      <b-button variant="primary" @click="saveQuery" :disabled="!validName">
        Save
      </b-button>
    </template>
  </b-modal>
</template>

<script>
import Yasqe from '@triply/yasqe'

export default {
  name: 'EditQueryModal',
  props: {
    id: {
      type: String,
      required: true
    },
    editId: {
      type: String,
      required: true
    }
  },
  data () {
    return {
      show: false,
      editQuery: {
        name: '',
        query: ''
      },
      yasqe: null,
      loading: true
    }
  },
  computed: {
    validName () {
      return this.editQuery.name.length > 0
    }
  },
  methods: {
    saveQuery () {
      this.$store.dispatch('queryLibrary/editSavedQuery', this.editQuery)
      this.show = false
    }
  },
  mounted () {
    this.editQuery = this.$store.getters['queryLibrary/getSavedQuery'](this.editId)
    this.$bvModal.show(this.id)
    this.$nextTick(() => {
      setTimeout(() => {
        const vm = this
        vm.yasqe = new Yasqe(document.getElementById('edit-yasqe'), {
          showQueryButton: false,
          queryingDisabled: true,
          resizeable: false,
          createShareableLink: null,
          persistenceId: null
        })
        vm.yasqe.setValue(this.editQuery.query)
        this.loading = false
        vm.yasqe.on('change', (yasqe) => { this.editQuery.query = yasqe.getValue() })
      }, 300)
    })
  }
}
</script>
