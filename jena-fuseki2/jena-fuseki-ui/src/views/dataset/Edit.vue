<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<template>
  <div class="container-fluid">
    <div class="row mt-4">
      <div class="col-12">
        <h2>/{{ datasetName }}</h2>
        <div class="card">
          <nav class="card-header">
            <Menu :dataset-name="datasetName" />
          </nav>
          <div class="card-body" v-if="services !== null && (!services['gsp-rw'] || services['gsp-rw'].length === 0)">
            <div class="alert alert-warning">
              No service for adding data available. The Graph Store Protocol service should be configured to allow adding data.
            </div>
          </div>
          <div class="card-body" v-else>
            <h3>Available Graphs</h3>
            <div>
              <div class="row mb-2">
                <div class="col-sm-12 col-md-4">
                  <div class="mb-2">
                    <button
                      :disabled="loadingGraphs"
                      @click="listCurrentGraphs()"
                      type="button"
                      class="btn btn-primary"
                    >
                      list current graphs
                    </button>
                  </div>
                  <ul class="list-group">
                    <li
                      class="list-group-item"
                      v-if="!loadingGraphs"
                    >
                      <span v-if="graphs.length === 0">Click to list current graphs</span>
                      <table-listing
                        :fields="fields"
                        :items="items"
                        :is-busy="loadingGraphs"
                        :filterable="false"
                        v-else
                      >
                        <template #cell(name)="data">
                          <a href="#" @click.prevent="fetchGraph(data.item.name)">
                            {{ data.item.name }}
                          </a>
                        </template>
                      </table-listing>
                    </li>
                    <li
                      class="list-group-item placeholder-glow"
                      v-else
                    >
                      <span class="placeholder col-10"></span>
                      <span class="placeholder col-5"></span>
                      <span class="placeholder col-8"></span>
                    </li>
                  </ul>
                </div>
                <div class="col">
                  <div class="spinner-border" role="status" v-if="loadingGraph">
                    <span class="visually-hidden">Loading...</span>
                  </div>
                  <div v-else>
                    <span class="input-group-text">graph</span>
                    <input
                      :placeholder="selectedGraph !== '' ? selectedGraph : 'choose a graph from the list'"
                      type="text"
                      class="form-control"
                      aria-label="graph name"
                      disabled
                    />
                  </div>
                  <textarea
                    ref="graph-editor"
                    :value="content"
                    @update:modelValue="content = $event"
                    class="form-control"
                  ></textarea>
                  <div class="mt-2 text-right">
                    <button
                      @click="discardChanges()"
                      type="button"
                      class="btn btn-secondary me-2"
                    >
                      <FontAwesomeIcon icon="times" />
                      <span class="ms-1">discard changes</span>
                    </button>
                    <button
                      :disabled="saveGraphDisabled"
                      @click="saveGraph()"
                      type="button"
                      class="btn btn-info"
                    >
                      <FontAwesomeIcon icon="check" />
                      <span
                        class="ms-1"
                      >save</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Menu from '@/components/dataset/Menu.vue'
import TableListing from '@/components/dataset/TableListing.vue'
import { EditorView, keymap, lineNumbers } from '@codemirror/view'
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands'
import { syntaxHighlighting, defaultHighlightStyle } from '@codemirror/language'
import { turtle } from 'codemirror-lang-turtle'
import {
  faTimes,
  faCheck
} from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import currentDatasetMixin from '@/mixins/current-dataset'
import currentDatasetMixinNavigationGuards from '@/mixins/current-dataset-navigation-guards'
import { displayError, displayNotification } from '@/utils'

library.add(faTimes, faCheck)

const MAX_EDITABLE_SIZE = 10000

/**
 * In CodeMirror 5, the static CodeMirror.fromTextArea was used in Jena UI to
 * sync an area in the page to the CodeMirror editor. That was removed in the
 * 6.x release, https://codemirror.net/docs/migration/.
 *
 * @param textarea
 * @param extensions
 * @returns {EditorView}
 */
function editorFromTextArea(textarea, extensions) {
  const view = new EditorView({doc: textarea.value, extensions})
  textarea.parentNode.insertBefore(view.dom, textarea)
  textarea.style.display = "none"
  if (textarea.form) textarea.form.addEventListener("submit", () => {
    textarea.value = view.state.doc.toString()
  })
  return view
}


export default {
  name: 'DatasetEdit',

  components: {
    Menu,
    TableListing,
    FontAwesomeIcon
  },

  mixins: [
    currentDatasetMixin
  ],

  ...currentDatasetMixinNavigationGuards,

  data () {
    return {
      loadingGraphs: null,
      loadingGraph: null,
      graphs: [],
      fields: [
        {
          key: 'name',
          label: 'name',
          sortable: true,
          sortDirection: 'asc'
        },
        {
          key: 'count',
          label: 'count',
          sortable: true,
          sortDirection: 'asc'
        }
      ],
      selectedGraph: '',
      content: '',
      code: '',
      cmOptions: {
        extensions: [
          lineNumbers(),
          history(),
          turtle(),
          syntaxHighlighting(defaultHighlightStyle),
          keymap.of([...defaultKeymap, ...historyKeymap])
        ]
      }
    }
  },

  computed: {
    items () {
      if (!this.graphs) {
        return []
      }
      return Object.entries(this.graphs)
        .map(graph => {
          return {
            name: graph[0],
            count: graph[1]
          }
        })
    },
    saveGraphDisabled () {
      return this.selectedGraph === '' || this.loadingGraph || this.loadingGraphs
    }
  },

  created () {
    this.codemirrorEditor = null
  },

  watch: {
    code (newVal) {
      this.codeChanged(newVal)
    },
    services (newVal) {
      if (newVal && newVal['gsp-rw'] && Object.keys(newVal['gsp-rw']).length > 0) {
        const element = this.$refs['graph-editor']
        this.codemirrorEditor = editorFromTextArea(element, this.cmOptions.extensions)
        EditorView.updateListener.of(v => {
          this.content = v.getValue()
        })
      }
    }
  },

  beforeRouteLeave (to, from, next) {
    next()
  },

  methods: {
    codeChanged (newVal) {
      this.codemirrorEditor.dispatch({
        changes: {
          from: 0,
          to: this.codemirrorEditor.state.doc.length,
          insert: newVal
        }
      })
    },
    listCurrentGraphs: async function () {
      this.loadingGraphs = true
      this.loadingGraph = true
      this.code = ''
      this.selectedGraph = ''
      try {
        this.graphs = await this.$fusekiService.countGraphsTriples(this.datasetName, this.services.query['srv.endpoints'][0])
      } catch (error) {
        displayError(this, error)
      } finally {
        this.loadingGraphs = null
        this.loadingGraph = null
      }
    },
    fetchGraph: async function (graphName) {
      const graph = Object.entries(this.graphs)
        .find(element => element[0] === graphName)
      if (parseInt(graph[1]) > MAX_EDITABLE_SIZE) {
        alert('Sorry, that dataset is too large to load into the editor')
        return
      }
      this.loadingGraph = true
      this.selectedGraph = graphName
      try {
        const result = await this.$fusekiService.fetchGraph(
          this.datasetName,
          this.services['gsp-rw']['srv.endpoints'],
          graphName)
        this.code = result.data
      } catch (error) {
        displayError(this, error)
      } finally {
        this.loadingGraph = null
      }
    },
    discardChanges: function () {
      this.selectedGraph = ''
      this.code = ''
    },
    saveGraph: async function () {
      if (!this.saveGraphDisabled) {
        this.loadingGraph = true
        try {
          await this.$fusekiService.saveGraph(
            this.datasetName,
            this.services['gsp-rw']['srv.endpoints'],
            this.selectedGraph,
            this.content)
          displayNotification(this, `Graph updated for dataset "${this.datasetName}"`)
        } catch (error) {
          displayError(this, error)
        } finally {
          this.loadingGraph = null
        }
      }
    }
  }
}
</script>
