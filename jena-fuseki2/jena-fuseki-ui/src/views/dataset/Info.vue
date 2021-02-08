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
          <b-card-body>
            <div>
              <b-row>
                <b-col sm="12" md="6">
                  <h3 class="text-center">Available Services</h3>
                  <b-row
                    :key="service['srv.type']"
                    v-for="service in this.services"
                  >
                    <b-col cols="6" class="text-right">{{ service['srv.description'] }}</b-col>
                    <b-col cols="6">
                      <a :href="`/${datasetName}/${service['srv.endpoints'][0]}`">
                        /{{ datasetName }}/{{ service['srv.endpoints'][0] }}
                      </a>
                    </b-col>
                  </b-row>
                  <b-row class="my-4">
                    <b-col cols="12" align="center">
                      <h3 class="text-center">Dataset size</h3>
                      <b-form-group
                        class="mb-2"
                      >
                        <b-button
                          id="count-triples-button"
                          ref="count-triples-button"
                          variant="primary"
                          @click="$refs['count-triples-button'].disabled = true"
                        >count triples in all graphs</b-button>
                      </b-form-group>
                      <b-popover
                        target="count-triples-button"
                        triggers="focus"
                        placement="auto"
                      >
                        <template v-slot:title>
                          <b-button
                            @click="
                              $root.$emit('bv::hide::popover', 'count-triples-button');
                              $refs['count-triples-button'].disabled = isDatasetStatsLoading
                            "
                            class="close"
                            aria-label="Close">
                            <span class="d-inline-block" aria-hidden="true">&times;</span>
                          </b-button>
                          Confirm
                        </template>
                        <div class="text-center">
                          <b-alert show variant="warning">This may be slow and impose a significant load on large datasets</b-alert>
                          <b-button
                            @click="
                              $root.$emit('bv::hide::popover', 'count-triples-button');
                              countTriplesInGraphs();
                              $refs['count-triples-button'].disabled = isDatasetStatsLoading
                            "
                            variant="primary"
                            class="mr-2">submit</b-button>
                          <b-button
                            @click="
                              $root.$emit('bv::hide::popover', 'count-triples-button');
                              $refs['count-triples-button'].disabled = isDatasetStatsLoading
                            "
                          >cancel</b-button>
                        </div>
                      </b-popover>
                      <b-table
                        :fields="countGraphFields"
                        :items="countGraphItems"
                        :busy="isDatasetSizeLoading"
                        class="mt-3"
                        bordered
                        hover
                        show-empty
                        small
                      >
                        <template v-slot:table-busy>
                          <div class="text-center text-danger my-2">
                            <b-spinner class="align-middle"></b-spinner>
                            <strong>Loading...</strong>
                          </div>
                        </template>
                        <template v-slot:empty>
                          <span>No data</span>
                        </template>
                      </b-table>
                    </b-col>
                  </b-row>
                </b-col>
                <b-col sm="12" md="6">
                  <h3 class="text-center">Statistics</h3>
                  <b-table
                    :fields="statsFields"
                    :items="statsItems"
                    :foot-clone="false"
                    :busy="isDatasetStatsLoading"
                    bordered
                    hover
                    show-empty
                    small
                  >
                    <template v-slot:table-busy>
                      <div class="text-center text-danger my-2">
                        <b-spinner class="align-middle"></b-spinner>
                        <strong>Loading...</strong>
                      </div>
                    </template>
                    <template v-slot:custom-foot="scope">
                      <b-tr>
                        <b-th v-for="field in scope.fields" :key="field.key">{{ overall[field.key] }}</b-th>
                      </b-tr>
                    </template>
                  </b-table>
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
import { BUS } from '@/events'

export default {
  name: 'DatasetInfo',

  components: {
    Menu
  },

  props: {
    datasetName: {
      type: String,
      required: true
    }
  },

  data () {
    return {
      serverData: {
        datasets: []
      },
      datasetStats: {

      },
      datasetSize: null,
      isDatasetSizeLoading: false,
      isDatasetStatsLoading: true,
      popoverShow: false,
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
    currentDataset () {
      return this.serverData.datasets.find(dataset => dataset['ds.name'] === `/${this.datasetName}`)
    },
    services () {
      if (!this.currentDataset || !this.currentDataset['ds.services']) {
        return []
      }
      return this.currentDataset['ds.services'].slice().sort((left, right) => {
        return left['srv.type'].localeCompare(right['srv.type'])
      })
    },
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
          // TODO: probably better to change in the server side as "Graph Store Protocol" is returned twice,
          //       but one entry is already distinguished by a "(Read)" suffix.
          const endpoint = endpoints[endpointName].description.trim() === 'SPARQL Query'
            ? `${endpoints[endpointName].description} (${endpointName})`
            : endpoints[endpointName].description
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
      BUS.$on('connection:reset', vm.initializeData)
      vm.initializeData()
    })
  },

  async beforeRouteUpdate (from, to, next) {
    this.initializeData()
    next()
  },

  beforeRouteLeave (from, to, next) {
    BUS.$off('connection:reset')
    next()
  },

  methods: {
    async countTriplesInGraphs () {
      this.popoverShow = false
      this.isDatasetStatsLoading = true
      this.isDatasetSizeLoading = true
      this.datasetSize = await this.$fusekiService.getDatasetSize(this.datasetName)
      this.isDatasetSizeLoading = false
      this.$refs['count-triples-button'].disabled = this.isDatasetSizeLoading
      this.datasetStats = await this.$fusekiService.getDatasetStats(this.datasetName)
      this.isDatasetStatsLoading = false
    },
    initializeData () {
      this.isDatasetStatsLoading = true
      this.$fusekiService
        .getServerData()
        .then(serverData => {
          this.serverData = serverData
        })
      this.$fusekiService
        .getDatasetStats(this.datasetName)
        .then(datasetStats => {
          this.datasetStats = datasetStats
        })
      this.isDatasetStatsLoading = false
      this.datasetSize = null
    }
  }
}
</script>
