<template>
  <b-modal title="Save SPARQL query" size="lg" :id="id" v-model="show" @hide="$emit('saveQueryModalHide')">
    <b-form-group label="Name">
      <b-form-input v-model="name" type="text" placeholder="Query name"></b-form-input>
    </b-form-group>
    <b-spinner v-if="loading"></b-spinner>
    <div id="save-yasqe"></div>
    <template #modal-footer>
      <b-button
        variant="outline-secondary"
        @click="show=false"
      >
        Cancel
      </b-button>
      <b-button
        variant="primary"
        @click="saveQuery"
        :disabled="!validName"
      >
        Save
      </b-button>
    </template>
  </b-modal>
</template>

<style lang="scss">
#save-yasqe {
  opacity: 0.6;
}
</style>

<script>
import { v4 as uuidv4 } from 'uuid'
import Yasqe from '@triply/yasqe'

export default {
  name: 'SaveQueryModal',
  props: {
    id: {
      type: String,
      required: true
    },
    query: {
      type: String,
      required: true
    }
  },
  data () {
    return {
      show: false,
      name: '',
      loading: true
    }
  },
  computed: {
    validName () {
      return this.name.length > 0
    }
  },
  methods: {
    saveQuery () {
      this.$store.dispatch('queryLibrary/addSavedQuery', {
        id: uuidv4(),
        name: this.name,
        query: this.query
      })
      this.show = false
    }
  },
  mounted () {
    this.$bvModal.show(this.id)
    this.$nextTick(() => {
      setTimeout(() => {
        const vm = this
        vm.yasqe = new Yasqe(document.getElementById('save-yasqe'), {
          showQueryButton: false,
          queryingDisabled: true,
          resizeable: false,
          createShareableLink: null,
          persistenceId: null,
          readOnly: 'nocursor'
        })
        vm.yasqe.setValue(this.query)
        this.loading = false
      }, 300)
    })
  }
}
</script>
