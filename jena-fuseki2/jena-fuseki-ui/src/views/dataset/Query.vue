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
            <div class="row mb-3">
              <div class="col">
                <fieldset class="form-group">
                  <label tabindex="-1" class="col-form-label pt-0">Example Queries</label>
                  <div>
                    <button
                      v-for="query of queries"
                      :key="query.text"
                      type="button"
                      class="badge text-bg-info p-2 me-2 border-0"
                      @click="setQuery(query.value)"
                    >
                      {{ query.text }}
                    </button>
                  </div>
                </fieldset>
              </div>
              <div class="col">
                <fieldset class="form-group">
                  <label tabindex="-1" class="col-form-label pt-0">Prefixes</label>
                  <div>
                    <button
                      v-for="prefix of prefixes"
                      :key="prefix.uri"
                      type="button"
                      class="fuseki-prefix"
                      :class="`badge text-bg-${isPrefixActive(prefix) ? 'primary' : 'light'} p-2 me-2 border-0`"
                      :aria-pressed="isPrefixActive(prefix)"
                      @mousedown.prevent
                      @click="togglePrefix(prefix)"
                    >
                      {{ prefix.text }}
                    </button>
                  </div>
                </fieldset>
              </div>
            </div>
            <div class="mb-3">
              <label for="sparql-endpoint" class="form-label">SPARQL Endpoint</label>
              <input
                v-model="currentDatasetUrl"
                id="sparql-endpoint"
                type="text"
                class="form-control"
              />
            </div>
            <div ref="editorContainer" class="fuseki-ldforge"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Menu from '@/components/dataset/Menu.vue'
import { createEditor } from '@ldforge/editor'
import { createHttpExecutor, createQueryEndpointExecutor } from '@ldforge/core'
import { createTableRenderer, createBooleanRenderer, createJsonRenderer } from '@ldforge/renderers'
import { createGeoRenderer } from '@ldforge/geo'
import '@ldforge/editor/styles.css'
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
  components: { Menu },
  mixins: [currentDatasetMixin],
  ...currentDatasetMixinNavigationGuards,

  data () {
    return {
      currentDatasetUrl: `/${this.datasetName}/sparql`,
      currentQueryPrefixes: {},
      prefixes: [
        { uri: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#', text: 'rdf' },
        { uri: 'http://www.w3.org/2000/01/rdf-schema#', text: 'rdfs' },
        { uri: 'http://www.w3.org/2002/07/owl#', text: 'owl' },
        { uri: 'http://www.w3.org/2001/XMLSchema#', text: 'xsd' }
      ],
      queries: [
        { value: SELECT_TRIPLES_QUERY, text: 'Selection of triples' },
        { value: SELECT_CLASSES_QUERY, text: 'Selection of classes' }
      ]
    }
  },

  computed: {
    datasetUrl () {
      const endpoints = this.services?.query?.['srv.endpoints']
      return endpoints?.length ? `/${this.datasetName}/${endpoints[0]}` : `/${this.datasetName}/sparql`
    }
  },

  created () {
    // Keep the imperative editor outside Vue's reactive state.
    this.editor = null
  },

  mounted () {
    this.mountEditor()
  },

  beforeUnmount () {
    this.editor?.destroy()
    this.editor = null
  },

  watch: {
    datasetUrl (value) {
      this.currentDatasetUrl = value
    },
    currentDatasetUrl () {
      // A response from the previous endpoint must not appear for the new one.
      this.editor?.cancel()
    },
    datasetName () {
      this.currentDatasetUrl = this.datasetUrl
      this.mountEditor()
    },
    '$route.query.query' (query) {
      if (typeof query === 'string') this.setQuery(query)
    }
  },

  methods: {
    mountEditor () {
      if (!this.$refs.editorContainer) return
      this.editor?.destroy()
      this.editor = null
      const storageKey = `fuseki:ldforge:query:${this.datasetName}`
      let query = SELECT_TRIPLES_QUERY
      try {
        query = localStorage.getItem(storageKey) ?? query
      } catch {
        // Editing remains available when browser storage is disabled.
      }
      const sharedQuery = new URL(window.location.href).searchParams.get('query') ?? this.$route.query.query
      if (typeof sharedQuery === 'string') query = sharedQuery
      this.editor = createEditor(this.$refs.editorContainer, {
        locale: 'en',
        // Main blue from src/assets/logo.svg; LDForge derives the complete UI theme.
        themeMainColor: '#1a80b9',
        query,
        shareBaseUrl: new URL(this.$router.resolve({ path: this.$route.path }).href, document.baseURI).href,
        shareEndpoint: () => this.getEndpointUrl(),
        executor: createQueryEndpointExecutor({ executor: {
          execute: request => {
            if (!this.currentDatasetUrl.trim()) throw new Error('Choose a SPARQL endpoint first.')
            const endpoint = this.getEndpointUrl()
            return createHttpExecutor({ endpoint }).execute(request)
          }
        } }),
        renderers: [
          createTableRenderer(), createBooleanRenderer(), createJsonRenderer(), createGeoRenderer()
        ],
        onQueryChange: value => {
          this.syncPrefixes()
          try {
            localStorage.setItem(storageKey, value)
          } catch {
            // Storage failure must not interrupt editing.
          }
        }
      })
      this.syncPrefixes()
    },
    syncPrefixes () {
      this.currentQueryPrefixes = this.editor?.getPrefixes() ?? {}
    },
    isPrefixActive (prefix) {
      return Object.values(this.currentQueryPrefixes).includes(prefix.uri)
    },
    togglePrefix (prefix) {
      if (this.isPrefixActive(prefix)) {
        // Remove declarations by alias, including aliases typed by the user.
        for (const [alias, uri] of Object.entries(this.currentQueryPrefixes)) {
          if (uri === prefix.uri) this.editor.removePrefix(alias)
        }
      } else {
        this.editor.addPrefix(prefix.text, prefix.uri)
      }
      this.syncPrefixes()
    },
    setQuery (query) {
      this.editor?.setQuery(query)
    },
    getEndpointUrl () {
      return new URL(
        this.$fusekiService.getFusekiUrl(this.currentDatasetUrl),
        document.baseURI
      ).href
    }
  }
}
</script>

<style>
.fuseki-ldforge {
  margin-top: 1rem;
  min-width: 0;
}
.fuseki-prefix.text-bg-primary {
  background-color: #1a80b9 !important;
}
.fuseki-ldforge .ldforge-button-group[data-button-group='query'] > button:first-child {
  color: #fff;
}
</style>
