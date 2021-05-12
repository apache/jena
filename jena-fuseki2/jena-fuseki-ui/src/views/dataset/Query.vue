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
                <b-col sm="12" md="6">
                  <b-form-group
                    label="Content Type (SELECT)"
                    label-cols="6"
                  >
                    <b-form-select
                      :options="contentTypeSelectOptions"
                      v-model="contentTypeSelect"
                      @change="onYasqeOptionsChange"
                    ></b-form-select>
                  </b-form-group>
                </b-col>
                <b-col sm="12" md="6">
                  <b-form-group
                    label="Content Type (GRAPH)"
                    label-cols="6"
                  >
                    <b-form-select
                      :options="contentTypeGraphOptions"
                      v-model="contentTypeGraph"
                      @change="onYasqeOptionsChange"
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
              endpoint: `/${vm.datasetName}/sparql`
            }
          }
        )
        vm.yasqe.on('queryResponse', (yasqe, response, duration) => {
          vm.yasqe.saveQuery()
          vm.yasr.setResponse(response, duration)
        })
        this.onYasqeOptionsChange()
        this.syncYasqePrefixes()
        this.loading = false
      })
    }, 300)
  },

  beforeRouteLeave (to, from, next) {
    next()
  },

  methods: {
    onYasqeOptionsChange () {
      this.yasqe.options.requestConfig.acceptHeaderSelect = this.contentTypeSelect
      this.yasqe.options.requestConfig.acceptHeaderGraph = this.contentTypeGraph
    },
    setQuery (query) {
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
</style>
