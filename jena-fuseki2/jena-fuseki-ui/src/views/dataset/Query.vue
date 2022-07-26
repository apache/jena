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
          <div class="card-body">
            <h3>SPARQL Query</h3>
            <p>To try out some SPARQL queries against the selected dataset, enter your query here.</p>
            <div>
              <div class="row">
                <div class="col">
                  <fieldset
                    class="form-group"
                  >
                    <label
                      tabindex="-1"
                      class="col-form-label pt-0"
                    >Example Queries</label>
                    <div>
                      <span
                        v-for="query of queries"
                        :key="query.text"
                        @click="setQuery(query.value)"
                        href="#"
                        class="badge text-bg-info p-2 me-2"
                      >{{ query.text }}</span>
                    </div>
                  </fieldset>
                </div>
                <div class="col">
                  <fieldset
                    class="form-group"
                  >
                    <label
                      tabindex="-1"
                      class="col-form-label pt-0"
                    >Prefixes</label>
                    <div>
                      <span
                        v-for="prefix of prefixes"
                        :key="prefix.uri"
                        :class="`badge text-bg-${getPrefixBadgeVariant(prefix)} p-2 me-2`"
                        @click.capture="togglePrefix(prefix)"
                        href="#"
                      >{{ prefix.text }}</span>
                    </div>
                  </fieldset>
                </div>
              </div>
              <div class="row">
                <div class="col-sm-12 col-md-4">
                  <fieldset
                    class="form-group"
                    aria-labelledby="sparql-endpoint-label"
                  >
                    <div class="form-row">
                      <label
                        tabindex="-1"
                        for="sparql-endpoint"
                        class="col-6 col-form-label"
                        id="sparql-endpoint-label"
                      >SPARQL Endpoint</label>
                      <div class="col">
                        <input
                          v-model="currentDatasetUrl"
                          id="sparql-endpoint"
                          type="text"
                          class="form-control"
                        />
                      </div>
                    </div>
                  </fieldset>
                </div>
                <div class="col-sm-12 col-md-4">
                  <fieldset
                    class="form-group"
                    aria-labelledby="content-type-label"
                  >
                    <div class="form-row">
                      <label
                        tabindex="-1"
                        for="content-type-select"
                        class="col-6 col-form-label"
                        id="content-type-select-label"
                      >Content Type (SELECT)</label>
                      <div class="col">
                        <select
                          v-model="contentTypeSelect"
                          id="content-type-select"
                          class="form-select"
                          aria-label="Content Type select"
                        >
                          <option
                            v-for="contentTypeSelectOption of contentTypeSelectOptions"
                            :key="contentTypeSelectOption.value"
                            :value="contentTypeSelectOption.value"
                          >
                            {{ contentTypeSelectOption.text }}
                          </option>
                        </select>
                      </div>
                    </div>
                  </fieldset>
                </div>
                <div class="col-sm-12 col-md-4">
                  <fieldset
                    class="form-group"
                    aria-labelledby="content-type-graph-label"
                  >
                    <div class="form-row">
                      <label
                        tabindex="-1"
                        for="content-type-graph"
                        class="col-6 col-form-label"
                        id="content-type-graph-label"
                      >Content Type (GRAPH)</label>
                      <div class="col">
                        <select
                          v-model="contentTypeGraph"
                          id="content-type-graph"
                          class="form-select"
                          aria-label="Content Type select"
                        >
                          <option
                            v-for="contentTypeGraphOption of contentTypeGraphOptions"
                            :key="contentTypeGraphOption.value"
                            :value="contentTypeGraphOption.value"
                          >
                            {{ contentTypeGraphOption.text }}
                          </option>
                        </select>
                      </div>
                    </div>
                  </fieldset>
                </div>
              </div>
            </div>
            <!-- This div cannot use v-if or v-show, as YASQE/YASR seem to fail to calculate the margins and
                 paddings if the element is not already rendered/existing in the DOM? -->
            <div>
              <div class="spinner-border align-middle" role="status" v-if="loading">
                <span class="visually-hidden">Loading...</span>
              </div>
              <div class="row">
                <div class="col-sm-12">
                  <div id="yasqe"></div>
                </div>
                <div class="col-sm-12">
                  <div id="yasr"></div>
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
import Yasqe from '@triply/yasqe'
import Yasr from '@triply/yasr'
import queryString from 'query-string'
import Vue from 'vue'
import currentDatasetMixin from '@/mixins/current-dataset'
import currentDatasetMixinNavigationGuards from '@/mixins/current-dataset-navigation-guards'

const SELECT_TRIPLES_QUERY = `SELECT ?subject ?predicate ?object
WHERE {
  ?subject ?predicate ?object
}
LIMIT 25`

const SELECT_CLASSES_QUERY = `PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT DISTINCT ?class ?label ?description
WHERE {
  ?class a owl:Class.
  OPTIONAL { ?class rdfs:label ?label}
  OPTIONAL { ?class rdfs:comment ?description}
}
LIMIT 25`

export default {
  name: 'DatasetQuery',

  components: {
    Menu
  },

  mixins: [
    currentDatasetMixin
  ],

  ...currentDatasetMixinNavigationGuards,

  data () {
    return {
      loading: true,
      contentTypeSelect: 'application/sparql-results+json',
      contentTypeSelectOptions: [
        { value: 'application/sparql-results+json', text: 'JSON' },
        { value: 'application/sparql-results+xml', text: 'XML' },
        { value: 'text/csv', text: 'CSV' },
        { value: 'text/tab-separated-values', text: 'TSV' }
      ],
      contentTypeGraph: 'text/turtle',
      contentTypeGraphOptions: [
        { value: 'text/turtle', text: 'Turtle' },
        { value: 'application/ld+json', text: 'JSON-LD' },
        { value: 'application/n-triples', text: 'N-Triples' },
        { value: 'application/rdf+xml', text: 'XML' }
      ],
      queries: [
        {
          value: SELECT_TRIPLES_QUERY,
          text: 'Selection of triples'
        },
        {
          value: SELECT_CLASSES_QUERY,
          text: 'Selection of classes'
        }
      ],
      prefixes: [
        { uri: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#', text: 'rdf' },
        { uri: 'http://www.w3.org/2000/01/rdf-schema#', text: 'rdfs' },
        { uri: 'http://www.w3.org/2002/07/owl#', text: 'owl' },
        { uri: 'http://www.w3.org/2001/XMLSchema#', text: 'xsd' }
      ],
      currentQueryPrefixes: [],
      currentDatasetUrl: ''
    }
  },

  computed: {
    datasetUrl () {
      if (!this.datasetName || this.services === null || !this.services.query || !this.services.query['srv.endpoints'] || this.services.query['srv.endpoints'].length === 0) {
        return ''
      }
      return `/${this.datasetName}/${this.services.query['srv.endpoints'][0]}`
    }
  },

  created () {
    this.yasqe = null
    this.yasr = null
    this.$nextTick(() => {
      setTimeout(() => {
        const vm = this

        document.getElementById('yasr').innerHTML = ''
        document.getElementById('yasqe').innerHTML = ''

        // results area
        vm.yasr = new Yasr(
          document.getElementById('yasr'),
          {
            // we do not want to save the results, otherwise we will have query results showing in different
            // dataset views
            persistenceId: null
          }
        )
        // query editor
        // NOTE: the full screen functionality was removed from YASQE: https://github.com/Triply-Dev/YASGUI.YASQE-deprecated/issues/139#issuecomment-573656137
        vm.yasqe = new Yasqe(
          document.getElementById('yasqe'),
          {
            showQueryButton: true,
            resizeable: false,
            requestConfig: {
              endpoint: this.$fusekiService.getFusekiUrl(this.currentDatasetUrl)
            },
            /**
             * Based on YASGUI code, but modified to avoid parsing the Vue Route query
             * hash. Note that we cannot use `document.location.hash` since it could
             * contain the ?query=... too. Instead, we must use Vue Route path value.
             *
             * @param {Yasqe} yasqe
             */
            createShareableLink: function (yasqe) {
              return (
                document.location.protocol +
                '//' +
                document.location.host +
                document.location.pathname +
                document.location.search +
                '#' +
                vm.$route.path +
                '?query=' +
                // Same as YASGUI does, good idea to avoid security problems...
                queryString.stringify(queryString.parse(yasqe.getValue()))
              )
            }
          }
        )
        vm.yasqe.on('queryResponse', (yasqe, response, duration) => {
          vm.yasqe.saveQuery()
          vm.yasr.setResponse(response, duration)
        })
        if (this.$route.query.query !== undefined) {
          vm.setQuery(this.$route.query.query)
        }
        this.syncYasqePrefixes()
        this.loading = false
      }, 300)
    })
  },

  beforeRouteUpdate (from, to, next) {
    Vue.nextTick(() => {
      if (this.$route.query.query !== undefined) {
        // N.B: a blank value, like query=, will clear the query editor. Not sure if
        //      desirable, but this can be easily modified later if necessary.
        this.setQuery(this.$route.query.query)
      }
    })
    const mixinBeforeRouteUpdate = currentDatasetMixinNavigationGuards.beforeRouteEnter
    mixinBeforeRouteUpdate(from, to, next)
  },

  watch: {
    datasetUrl: function (val, oldVal) {
      this.currentDatasetUrl = val
    },
    currentDatasetUrl: function (val, oldVal) {
      if (this.yasqe) {
        this.yasqe.options.requestConfig.endpoint = this.$fusekiService.getFusekiUrl(val)
      }
    },
    contentTypeSelect: function (val, oldVal) {
      if (this.yasqe) {
        this.yasqe.options.requestConfig.acceptHeaderSelect = this.contentTypeSelect
      }
    },
    contentTypeGraph: function (val, oldVal) {
      if (this.yasqe) {
        this.yasqe.options.requestConfig.acceptHeaderGraph = this.contentTypeGraph
      }
    }
  },

  methods: {
    setQuery (query) {
      // Passing this query value through queryString.stringify(.parse) creates an
      // invalid query. Tested some XSS values with Chrome and FFox, and couldn't
      // trigger a popup/alert by modifying the query passed, looks like YASQE does
      // the query cleaning before displaying it.
      // See: https://github.com/payloadbox/xss-payload-list
      this.yasqe.setValue(query)
      this.syncYasqePrefixes()
    },
    getPrefixBadgeVariant (prefix) {
      if (this.currentQueryPrefixes.includes(prefix.uri)) {
        return 'primary'
      }
      return 'light'
    },
    syncYasqePrefixes () {
      const prefixes = this.yasqe.getPrefixesFromQuery()
      this.currentQueryPrefixes = []
      for (const uri of Object.values(prefixes)) {
        this.currentQueryPrefixes.push(uri)
      }
    },
    togglePrefix (prefix) {
      const newPrefix = {
        [prefix.text]: prefix.uri
      }
      if (this.currentQueryPrefixes.includes(prefix.uri)) {
        this.yasqe.removePrefixes(newPrefix)
        this.currentQueryPrefixes.splice(this.currentQueryPrefixes.indexOf(prefix.uri), 1)
      } else {
        this.yasqe.addPrefixes(newPrefix)
        this.currentQueryPrefixes.push(prefix.uri)
      }
    }
  }
}
</script>

<style lang="scss">
@import '~@triply/yasqe/build/yasqe.min.css';
@import '~@triply/yasr/build/yasr.min.css';

// N.B: these were copied from an old release of YASR due to this
//      change: https://github.com/TriplyDB/Yasgui/commit/19521998f035e718d3f1d5cfa6073ce2e34242e7
//      for more: https://github.com/apache/jena/pull/1153
.yasr table.dataTable {
  border: 1px solid rgb(217, 217, 217);
  border-image-source: initial;
  border-image-slice: initial;
  border-image-repeat: initial;
  tbody {
    tr {
      td {
        border-top: 1px solid #ddd;
      }
      &:last-of-type {
        td {
          border-bottom: 1px solid #ddd;
        }
      }
      &:nth-child(even) {
        background-color: #f9f9f9;
      }
    }
  }
}
</style>
