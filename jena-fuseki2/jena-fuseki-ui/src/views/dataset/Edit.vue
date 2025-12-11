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
                  <CodeMirror
                    ref="graph-editor"
                    class="form-control"
                    :str-value="content"
                    :b-allow-update="bAllowUpdate"
                    :cm-extender="cmOptions"
                    @ready="(objReady) => { cmView = objReady.view; }"
                    @update-value="(strNew) => { updateChanges(strNew); }"
                    @destroy="() => { cmView = null; }"
                  />
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
import CodeMirror from '@/components/dataset/CodeMirror.vue';
import { StreamLanguage } from '@codemirror/language'
import { turtle } from "@codemirror/legacy-modes/mode/turtle"
import { faTimes, faCheck } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import currentDatasetMixin from '@/mixins/current-dataset'
import currentDatasetMixinNavigationGuards from '@/mixins/current-dataset-navigation-guards'
import { displayError, displayNotification } from '@/utils'

library.add(faTimes, faCheck)

const MAX_EDITABLE_SIZE = 10000

export default {
  name: 'DatasetEdit',

  components: {
    Menu,
    TableListing,
    CodeMirror,
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
      bAllowUpdate: true,
      content: '', // ...always comes from CodeMirror editor except on initial load
      cmView: null, // TODO: Add error message display when CodeMirror view reports error
      cmOptions: {
        basic: true,
        extensions: [
          StreamLanguage.define(turtle)
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

  watch: {
    services (newVal) {
      if (newVal && newVal['gsp-rw'] && Object.keys(newVal['gsp-rw']).length > 0) {
        // ...nothing to do...
      }
    }
  },

  beforeRouteLeave (to, from, next) {
    next()
  },

  methods: {
    listCurrentGraphs: async function () {
      this.loadingGraphs = true
      this.loadingGraph = true
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
        alert('The dataset is too large (> ' + MAX_EDITABLE_SIZE + ') for the editor')
        return
      }
      this.loadingGraph = true
      this.selectedGraph = graphName
      try {
        const result = await this.$fusekiService.fetchGraph(
          this.datasetName,
          this.services['gsp-rw']['srv.endpoints'],
          graphName)
        this.bAllowUpdate = true;
        this.content = result.data // ...reactive update to CodeMirror editor
      } catch (error) {
        displayError(this, error)
      } finally {
        this.loadingGraph = null
      }
    },
    discardChanges: function () {
      this.selectedGraph = ''
      this.bAllowUpdate = true;
      this.content = '' // ...reactive update to CodeMirror editor
    },
    updateChanges: function (strNew) {
      this.bAllowUpdate = false; // ...since the update came from the editor, don't update itself
      this.content = strNew; // ...reactive update does NOT change CodeMirror editor
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
