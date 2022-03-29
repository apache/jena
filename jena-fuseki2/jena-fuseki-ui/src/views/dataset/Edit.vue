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
  <b-container fluid>
    <b-row class="mt-4">
      <b-col cols="12">
        <h2>/{{ this.datasetName }}</h2>
        <b-card no-body>
          <b-card-header header-tag="nav">
            <Menu :dataset-name="datasetName" />
          </b-card-header>
          <b-card-body v-if="!this.services['gsp-rw'] || this.services['gsp-rw'].length === 0">
            <b-alert show variant="warning">No service for adding data available. The Graph Store Protocol service should be configured to allow adding data.</b-alert>
          </b-card-body>
          <b-card-body v-else>
            <b-card-title>Available Graphs</b-card-title>
            <div>
              <b-row class="mb-2">
                <b-col sm="12" md="4">
                  <div class="mb-2">
                    <b-button
                      @click="listCurrentGraphs()"
                      :disabled="!!loadingGraphs"
                      variant="primary"
                    >list current graphs</b-button>
                  </div>
                  <b-list-group>
                    <b-list-group-item
                      v-if="!loadingGraphs"
                    >
                      <span v-if="this.graphs.length === 0">Click to list current graphs</span>
                      <table-listing
                        :fields="fields"
                        :items="items"
                        :is-busy="loadingGraphs"
                        :filterable="false"
                        v-else
                      >
                        <template v-slot:cell(name)="data">
                          <a href="#" @click.prevent="fetchGraph(data.item.name)">
                            {{ data.item.name }}
                          </a>
                        </template>
                      </table-listing>
                    </b-list-group-item>
                    <b-list-group-item
                      v-else
                    >
                      <b-skeleton animation="wave" width="85%"></b-skeleton>
                      <b-skeleton animation="wave" width="55%"></b-skeleton>
                      <b-skeleton animation="wave" width="70%"></b-skeleton>
                    </b-list-group-item>
                  </b-list-group>
                </b-col>
                <b-col>
                  <b-overlay :show="loadingGraph">
                    <b-input-group prepend="graph" class="mb-2">
                      <b-form-input
                        :placeholder="selectedGraph !== '' ? selectedGraph : 'choose a graph from the list'"
                        disabled
                      ></b-form-input>
                    </b-input-group>
                    <b-textarea
                      ref="graph-editor"
                      v-model="content"
                    >
                    </b-textarea>
                    <div class="mt-2 text-right">
                      <b-button
                        class="mr-2"
                        variant="secondary"
                        @click="discardChanges()"
                      >
                        <FontAwesomeIcon icon="times" />
                        <span class="ml-1">discard changes</span>
                      </b-button>
                      <b-button
                        :disabled="selectedGraph === '' || loadingGraph || loadingGraphs"
                        variant="info"
                        @click="saveGraph()"
                      >
                        <FontAwesomeIcon icon="check" />
                        <span
                          class="ml-1"
                        >save</span>
                      </b-button>
                    </div>
                  </b-overlay>
                </b-col>
              </b-row>
            </div>
          </b-card-body>
        </b-card>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import Menu from '@/components/dataset/Menu'
import TableListing from '@/components/dataset/TableListing'
import CodeMirror from 'codemirror'
import 'codemirror/mode/turtle/turtle'
import {
  faTimes,
  faCheck
} from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import currentDatasetMixin from '@/mixins/current-dataset'
import { displayError } from '@/utils'

library.add(faTimes, faCheck)

const MAX_EDITABLE_SIZE = 10000

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

  data () {
    return {
      loadingGraphs: false,
      loadingGraph: false,
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
      codemirrorEditor: null,
      cmOptions: {
        mode: 'text/turtle',
        lineNumbers: true
        // readOnly: 'nocursor'
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
    }
  },

  watch: {
    code (newVal) {
      this.codeChanged(newVal)
    }
  },

  created () {
    this.$nextTick(() => {
      const element = this.$refs['graph-editor']
      this.codemirrorEditor = CodeMirror.fromTextArea(element.$el, this.cmOptions)
      this.codemirrorEditor.on('change', cm => {
        this.content = cm.getValue()
      })
    })
  },

  beforeRouteLeave (to, from, next) {
    next()
  },

  methods: {
    codeChanged (newVal) {
      const scrollInfo = this.codemirrorEditor.getScrollInfo()
      this.codemirrorEditor.setValue(newVal)
      this.content = newVal
      this.codemirrorEditor.scrollTo(scrollInfo.left, scrollInfo.top)
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
        this.loadingGraphs = false
        this.loadingGraph = false
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
        const result = await this.$fusekiService.fetchGraph(this.datasetName, graphName)
        this.code = result.data
      } catch (error) {
        console.error(error)
        this.$bvToast.toast(`${error}`, {
          title: 'Error',
          noAutoHide: true,
          appendToast: false
        })
      } finally {
        this.loadingGraph = false
      }
    },
    discardChanges: function () {
      this.selectedGraph = ''
      this.code = ''
    },
    saveGraph: async function () {
      this.loadingGraph = true
      try {
        await this.$fusekiService.saveGraph(this.datasetName, this.selectedGraph, this.content)
        this.$bvToast.toast(`Graph updated for dataset "${this.datasetName}"`, {
          title: 'Notification',
          autoHideDelay: 5000,
          appendToast: false
        })
      } catch (error) {
        this.$bvToast.toast(`${error}`, {
          title: 'Error',
          noAutoHide: true,
          appendToast: false
        })
      } finally {
        this.loadingGraph = false
      }
    }
  }
}
</script>

<style lang="scss">
@import '~codemirror';
</style>
