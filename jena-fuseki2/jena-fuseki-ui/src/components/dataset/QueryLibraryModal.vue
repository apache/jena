<template>
  <b-modal title="SPARQL Query Library" size="lg" :id="id" v-model="show" @hide="$emit('queryLibraryModalHide')">
    <b-list-group>
      <b-list-group-item
        class="d-flex justify-content-between align-items-center"
        button
        v-for="item in $store.getters['queryLibrary/listSavedQueries']"
        :key="item.id"
        @click="handleLoad(item.id)"
      >
        {{ item.name }}
        <div>
          <b-button variant="outline-warning" size="sm" class="mr-2" @click.stop="handleEdit(item.id)">
            <FontAwesomeIcon icon="pen-to-square" />
          </b-button>
          <b-button variant="outline-danger" size="sm" @click.stop="handleDelete(item.id)">
            <FontAwesomeIcon icon="trash" />
          </b-button>
        </div>
      </b-list-group-item>
    </b-list-group>
    <EditQueryModal v-if="editQueryModal" id="query-library" @editQueryModalHide="editQueryModal=false" :editId="editId"/>
    <template #modal-footer>
      <b-button variant="secondary" @click="show = false">
        Close
      </b-button>
    </template>
  </b-modal>
</template>

<script>
import EditQueryModal from '@/components/dataset/EditQueryModal'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faTrash, faPenToSquare } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faTrash, faPenToSquare)

export default {
  name: 'QueryLibraryModal',
  components: {
    EditQueryModal,
    FontAwesomeIcon
  },
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data () {
    return {
      show: false,
      editQueryModal: false,
      editId: ''
    }
  },
  mounted () {
    this.$bvModal.show(this.id)
  },
  methods: {
    handleLoad (id) {
      const query = this.$store.getters['queryLibrary/getSavedQuery'](id)
      this.$emit('loadSavedQuery', query.query)
      this.show = false
    },
    handleEdit (id) {
      this.editId = id
      this.editQueryModal = true
    },
    handleDelete (id) {
      const query = this.$store.getters['queryLibrary/getSavedQuery'](id)
      if (confirm(`Are you sure you want to delete "${query.name}"?`)) {
        this.$store.dispatch('queryLibrary/deleteSavedQuery', id)
      }
    }
  }
}
</script>
