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
        <h2>/{{ this.datasetName }}</h2>
        <div class="card">
          <nav class="card-header">
            <Menu :dataset-name="datasetName" />
          </nav>
          <div class="card-body">
            <div>
              <div class="row">
                <div class="col-sm-12 col-md-6">
                  <h3 class="text-center">Available Services</h3>
                  <div
                    :key="service['srv.type']"
                    v-for="service in this.services"
                  >
                    <div class="row" v-for="endpoint of service['srv.endpoints']" :key="endpoint">
                      <div class="col-6 text-right">{{ service['srv.description'] }}</div>
                      <div class="col-6">
                        <a :href="`/${datasetName}/${endpoint}`">
                          /{{ datasetName }}/{{ endpoint }}
                        </a>
                      </div>
                    </div>
                  </div>
                  <div class="row my-4">
                    <div class="col-12 text-center">
                      <h3>Dataset size</h3>
                      <div class="mb-2">
                        <div hidden>
                          <div ref="count-triples-content">
                            <div class="text-center">
                              <div class="alert alert-warning">This may be slow and impose a significant load on large datasets.</div>
                              <button
                                @click="
                                  countTriplesInGraphs();
                                  $refs['count-triples-button'].disabled = isDatasetStatsLoading
                                "
                                id="count-triples-submit-button"
                                type="button"
                                class="btn btn-primary me-2">submit</button>
                              <button
                                class="btn btn-secondary"
                                @click="
                                  $refs['count-triples-button'].disabled = isDatasetStatsLoading
                                "
                              >cancel</button>
                            </div>
                          </div>
                        </div>
                        <button
                          ref="count-triples-button"
                          id="count-triples-button"
                          type="button"
                          class="btn btn-primary"
                          data-bs-toggle="popover"
                          data-bs-placement="auto"
                          data-bs-trigger="focus"
                          title="Confirm"
                        >
                          count triples in all graphs
                        </button>
                      </div>
                      <jena-table
                        :fields="countGraphFields"
                        :items="countGraphItems"
                        :busy="isDatasetSizeLoading"
                        id="dataset-size-table"
                        class="mt-3"
                        bordered
                        hover
                        small
                      >
                        <template v-slot:table-busy>
                          <div class="text-center text-danger my-2">
                            <div class="spinner-border align-middle" role="status">
                              <span class="visually-hidden">Loading...</span>
                            </div>
                            <strong>Loading...</strong>
                          </div>
                        </template>
                        <template v-slot:empty>
                          <span>No data</span>
                        </template>
                      </jena-table>
                    </div>
                  </div>
                </div>
                <div class="col-sm-12 col-md-6">
                  <h3 class="text-center">Statistics</h3>
                  <jena-table
                    :fields="statsFields"
                    :items="statsItems"
                    :busy="isDatasetStatsLoading"
                    id="statistics-table"
                    bordered
                    hover
                    small
                  >
                    <template v-slot:table-busy>
                      <div class="text-center text-danger my-2">
                        <div class="spinner-border align-middle" role="status">
                          <span class="visually-hidden">Loading...</span>
                        </div>
                        <strong>Loading...</strong>
                      </div>
                    </template>
                    <template v-slot:custom-foot="scope">
                      <tr>
                        <th v-for="field in scope.fields" :key="field.key">{{ overall[field.key] }}</th>
                      </tr>
                    </template>
                  </jena-table>
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
import Menu from '@/components/dataset/Menu'
import { displayError } from '@/utils'
import currentDatasetMixin from '@/mixins/current-dataset'
import { Popover } from 'bootstrap'
import JenaTable from '@/components/dataset/JenaTable'

export default {
  name: 'DatasetInfo',

  components: {
    JenaTable,
    Menu
  },

  mixins: [
    currentDatasetMixin
  ],

  data () {
    return {
      datasetStats: {},
      datasetSize: null,
      isDatasetSizeLoading: false,
      statsFields: [
        {
          key: 'endpoint',
          label: 'Endpoint'
        },
        {
          key: 'total',
          label: 'Requests',
          sortable: true,
          sortDirection: 'asc'
        },
        {
          key: 'good',
          label: 'Good',
          sortable: true,
          sortDirection: 'asc'
        },
        {
          key: 'bad',
          label: 'Bad',
          sortable: true,
          sortDirection: 'asc'
        }
      ],
      countGraphFields: [
        {
          key: 'name',
          label: 'graph name',
          sortable: true,
          sortDirection: 'asc'
        },
        {
          key: 'triples',
          label: 'triples',
          sortable: true,
          sortDirection: 'asc'
        }
      ]
    }
  },

  computed: {
    statsItems () {
      if (!this.datasetStats || !this.datasetStats.datasets) {
        return []
      }
      const dataset = this.datasetStats.datasets[`/${this.datasetName}`]
      const endpoints = dataset.endpoints
      if (!dataset || !endpoints) {
        return []
      }
      // collect the stats of each endpoint
      const items = Object.keys(endpoints)
        .map(endpointName => {
          const endpoint = !endpointName.startsWith('_') ? `${endpoints[endpointName].description} (${endpointName})` : endpoints[endpointName].description
          return {
            endpoint: endpoint,
            operation: endpoints[endpointName].operation,
            total: endpoints[endpointName].Requests,
            good: endpoints[endpointName].RequestsGood,
            bad: endpoints[endpointName].RequestsBad
          }
        })
      items.sort((left, right) => {
        return left.operation.localeCompare(right.operation)
      })
      return items
    },
    overall () {
      if (!this.datasetStats || !this.datasetStats.datasets) {
        return []
      }
      const dataset = this.datasetStats.datasets[`/${this.datasetName}`]
      return {
        endpoint: 'Overall',
        total: dataset.Requests,
        good: dataset.RequestsGood,
        bad: dataset.RequestsBad
      }
    },
    countGraphItems () {
      if (!this.datasetSize) {
        return []
      }
      return Object.keys(this.datasetSize)
        .map(key => {
          return {
            name: key,
            triples: this.datasetSize[key]
          }
        })
    }
  },

  beforeRouteEnter (from, to, next) {
    next(async vm => {
      vm.datasetSize = null
    })
  },

  async beforeRouteUpdate (from, to, next) {
    this.datasetSize = null
    next()
  },

  mounted: function () {
    // Initialize the Bootstrap Popover
    const popoverOptions = {
      html: true,
      content: this.$refs['count-triples-content']
    }
    const popoverElement = this.$refs['count-triples-button']
    // TBD: will it be garbage collected?
    // eslint-disable-next-line no-new
    new Popover(popoverElement, popoverOptions)
  },

  methods: {
    async countTriplesInGraphs () {
      this.isDatasetSizeLoading = true
      try {
        this.datasetSize = await this.$fusekiService.getDatasetSize(this.currentDataset['ds.name'], this.services.query['srv.endpoints'][0])
        this.$refs['count-triples-button'].disabled = this.isDatasetSizeLoading
        this.datasetStats = await this.$fusekiService.getDatasetStats(this.datasetName)
      } catch (error) {
        displayError(this, error)
      } finally {
        this.isDatasetSizeLoading = false
        this.$refs['count-triples-button'].disabled = this.isDatasetSizeLoading
      }
    }
  }
}
</script>
