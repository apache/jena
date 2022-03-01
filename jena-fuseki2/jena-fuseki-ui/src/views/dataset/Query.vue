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
            <b-card-title>SPARQL Query</b-card-title>
            <p>To try out some SPARQL queries against the selected dataset, enter your query here.</p>
            <div>
              <b-row>
                <b-col>
                  <b-form-group
                    label="Example Queries"
                  >
                    <b-badge
                      v-for="query of queries"
                      :key="query.text"
                      variant="info"
                      class="p-2 mr-2"
                      href="#"
                      @click="setQuery(query.value)"
                    >{{ query.text }}</b-badge>
                  </b-form-group>
                </b-col>
                <b-col>
                  <b-form-group
                    label="Prefixes"
                  >
                    <b-badge
                      v-for="prefix of prefixes"
                      :key="prefix.uri"
                      :variant="getPrefixBadgeVariant(prefix)"
                      class="p-2 mr-2"
                      href="#"
                      @click.capture="togglePrefix(prefix)"
                    >{{ prefix.text }}</b-badge>
                  </b-form-group>
                </b-col>
              </b-row>
              <b-row>
                <b-col sm="12" md="4">
                  <b-form-group
                    label="SPARQL Endpoint"
                    label-cols="6"
                  >
                    <b-form-input
                      :value="`${datasetName}/sparql`"
                      v-model="datasetUrl"
                    ></b-form-input>
                  </b-form-group>
                </b-col>
                <b-col sm="12" md="4">
                  <b-form-group
                    label="Content Type (SELECT)"
                    label-cols="6"
                  >
                    <b-form-select
                      :options="contentTypeSelectOptions"
                      v-model="contentTypeSelect"
                    ></b-form-select>
                  </b-form-group>
                </b-col>
                <b-col sm="12" md="4">
                  <b-form-group
                    label="Content Type (GRAPH)"
                    label-cols="6"
                  >
                    <b-form-select
                      :options="contentTypeGraphOptions"
                      v-model="contentTypeGraph"
                    ></b-form-select>
                  </b-form-group>
                </b-col>
              </b-row>
            </div>
            <!-- This div cannot use v-if or v-show, as YASQE/YASR seem to fail to calculate the margins and
                 paddings if the element is not already rendered/existing in the DOM? -->
            <div>
              <b-spinner v-if="loading"></b-spinner>
              <b-row>
                <b-col sm="12">
                  <div id="yasqe"></div>
                </b-col>
                <b-col sm="12">
                  <div id="yasr"></div>
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
import Yasqe from '@triply/yasqe'
import Yasr from '@triply/yasr'
import queryString from 'query-string'
import Vue from 'vue'

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

  props: {
    datasetName: {
      type: String,
      required: true
    }
  },

  data () {
    return {
      loading: true,
      yasqe: null,
      yasr: null,
      datasetUrl: `/${this.datasetName}/sparql`,
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
      currentQueryPrefixes: []
    }
  },

  created () {
    this.$nextTick(() => {
      setTimeout(() => {
        const vm = this
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
              endpoint: this.$fusekiService.getFusekiUrl(`/${vm.datasetName}/sparql`)
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
    next()
  },

  watch: {
    datasetUrl: function (val, oldVal) {
      this.yasqe.options.requestConfig.endpoint = this.$fusekiService.getFusekiUrl(this.datasetUrl)
    },
    contentTypeSelect: function (val, oldVal) {
      this.yasqe.options.requestConfig.acceptHeaderSelect = this.contentTypeSelect
    },
    contentTypeGraph: function (val, oldVal) {
      this.yasqe.options.requestConfig.acceptHeaderGraph = this.contentTypeGraph
    }
  },

  beforeRouteLeave (to, from, next) {
    next()
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
