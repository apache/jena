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
                        :key="prefix.text"
                        class="d-inline-block me-2"
                      >
                        <!-- content for the remove prefix popover -->
                        <div class="popover" role="popover" hidden>
                          <div :ref="`remove-prefix-${prefix.text}-content`">
                            <div>Confirm</div>
                            <div class="text-center">
                              <div class="alert alert-danger">
                                Are you sure you want to remove prefix: <b>{{ prefix.text }}</b> from this dataset?
                              </div>
                              <button
                                @click="hidePopover();removePrefix(prefix)"
                                class="btn btn-primary me-2"
                              >Yes</button>
                              <button
                                @click="hidePopover()"
                                type="button"
                                class="btn btn-secondary"
                              >Cancel</button>
                            </div>
                          </div>
                        </div>
                        <span
                          :class="`badge text-bg-${getPrefixBadgeVariant(prefix)} p-2`"
                          @click.capture.self="togglePrefix(prefix)"
                          href="#"
                        >{{ prefix.text }}<button
                          v-if="prefixesWritable"
                          :id="`remove-prefix-${prefix.text}-button`"
                          :ref="`remove-prefix-${prefix.text}-button`"
                          @click.stop="showPopover(`remove-prefix-${prefix.text}`)"
                          type="button"
                          class="btn-close ms-1 remove-prefix"
                          :aria-label="`Remove prefix ${prefix.text}`"
                        ></button></span>
                      </span>
                      <span
                        v-if="prefixesWritable && !showAddPrefixForm"
                        id="add-prefix-pill"
                        class="badge add-prefix-pill p-2"
                        role="button"
                        tabindex="0"
                        aria-label="Add a prefix"
                        @click="openAddPrefixForm"
                        @keyup.enter="openAddPrefixForm"
                      >+</span>
                    </div>
                    <form
                      v-if="prefixesWritable && showAddPrefixForm"
                      @submit.prevent="addPrefix"
                      @keyup.esc="closeAddPrefixForm"
                      id="add-prefix-form"
                      class="mt-2"
                    >
                      <div class="input-group input-group-sm has-validation">
                        <input
                          v-model.trim="newPrefix.prefix"
                          id="add-prefix-name"
                          type="text"
                          :class="['form-control', addPrefixNameClass]"
                          placeholder="prefix"
                          aria-label="new prefix name"
                        />
                        <input
                          v-model.trim="newPrefix.uri"
                          id="add-prefix-uri"
                          type="text"
                          :class="['form-control', addPrefixUriClass]"
                          placeholder="URI"
                          aria-label="new prefix URI"
                        />
                        <button
                          type="submit"
                          class="btn btn-primary"
                          :disabled="addingPrefix"
                        >
                          add
                        </button>
                        <button
                          type="button"
                          id="add-prefix-cancel"
                          class="btn btn-outline-secondary"
                          @click="closeAddPrefixForm"
                        >
                          cancel
                        </button>
                        <div :class="['invalid-feedback', addPrefixNameClass === 'is-invalid' ? 'd-block' : 'd-none']">
                          Please enter a valid prefix.
                        </div>
                        <div :class="['invalid-feedback', addPrefixUriClass === 'is-invalid' ? 'd-block' : 'd-none']">
                          Please enter a valid URI.
                        </div>
                      </div>
                    </form>
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
import Menu from '@/components/dataset/Menu.vue'
import Yasqe from '@zazuko/yasqe'
import Yasr from '@zazuko/yasr'
import GeoPlugin from 'yasgui-geo-tg'
import { createShareableLink } from '@/utils/query'
import { displayError, displayNotification } from '@/utils'
import { DEFAULT_PREFIXES } from '@/utils/prefixes'
import { validatePrefixName, validatePrefixUri } from '@/utils/validation'
import { nextTick } from 'vue'
import { Popover } from 'bootstrap'
import currentDatasetMixin from '@/mixins/current-dataset'
import currentDatasetMixinNavigationGuards from '@/mixins/current-dataset-navigation-guards'

Yasr.registerPlugin('geo', GeoPlugin)

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

// The shared defaults in the {text, uri} shape the badge template uses.
const defaultPrefixes = () => DEFAULT_PREFIXES.map(p => ({ text: p.prefix, uri: p.uri }))

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
      prefixes: defaultPrefixes(),
      currentQueryPrefixes: [],
      currentDatasetUrl: '',
      currentPopover: null,
      addingPrefix: false,
      showAddPrefixForm: false,
      newPrefix: {
        prefix: '',
        uri: ''
      },
      // Validation state is only displayed after the first submit attempt.
      addPrefixValidated: false
    }
  },

  computed: {
    datasetUrl () {
      if (!this.datasetName || this.services === null || !this.services.query || !this.services.query['srv.endpoints'] || this.services.query['srv.endpoints'].length === 0) {
        return ''
      }
      return `/${this.datasetName}/${this.services.query['srv.endpoints'][0]}`
    },

    /**
     * The current dataset's prefixes service, or null if it does not declare one.
     *
     * @returns {{endpoint: string, writable: boolean}|null}
     */
    prefixesService () {
      if (!this.services) {
        return null
      }
      const svc = this.services['prefixes-rw'] || this.services['prefixes-r'] || null
      return svc
        ? {
            endpoint: svc['srv.endpoints'][0],
            writable: !!this.services['prefixes-rw']
          }
        : null
    },

    /**
     * True iff the current dataset's prefixes can be edited from the UI.
     */
    prefixesWritable () {
      return !!(this.prefixesService && this.prefixesService.writable)
    },

    /**
     * Bootstrap validation class for the new-prefix name input; empty
     * until the first submit attempt, live-updating afterwards.
     */
    addPrefixNameClass () {
      if (!this.addPrefixValidated) {
        return ''
      }
      return validatePrefixName(this.newPrefix.prefix) ? 'is-valid' : 'is-invalid'
    },

    /**
     * Bootstrap validation class for the new-prefix URI input; empty
     * until the first submit attempt, live-updating afterwards.
     */
    addPrefixUriClass () {
      if (!this.addPrefixValidated) {
        return ''
      }
      return validatePrefixUri(this.newPrefix.uri) ? 'is-valid' : 'is-invalid'
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
            persistenceId: null,
            // Enable geo plugin alongside default table
            pluginOrder: ['table', 'response', 'geo'],
          }
        )
        // Curried function to create shareable links. YASQE expects a function
        // that accepts only an instance of YASQE.
        const curriedCreateShareableLink = yasqe => {
          return createShareableLink(yasqe.getValue(), vm.$route.path)
        }
        // query editor
        // NOTE: the full screen functionality was removed from YASQE: https://github.com/Triply-Dev/YASGUI.YASQE-deprecated/issues/139#issuecomment-573656137
        vm.yasqe = new Yasqe(
          document.getElementById('yasqe'),
          {
            showQueryButton: true,
            resizeable: true,
            requestConfig: {
              acceptHeaderGraph: this.contentTypeGraph,
              endpoint: this.$fusekiService.getFusekiUrl(this.currentDatasetUrl)
            },
            createShareableLink: curriedCreateShareableLink
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
    nextTick(() => {
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
    /* eslint-disable no-unused-vars */
    datasetUrl: function (val, oldVal) {
      this.currentDatasetUrl = val
    },
    currentDatasetUrl: function (val, oldVal) {
      if (this.yasqe) {
        this.yasqe.options.requestConfig.endpoint = this.$fusekiService.getFusekiUrl(val)
      }
    },
    prefixesService: function (val, oldVal) {
      this.closeAddPrefixForm()
      this.loadPrefixes()
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
    /* eslint-enable no-unused-vars */
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
    },
    /**
     * Replaces the default prefix list with the prefixes of the current
     * dataset. The defaults are restored when the dataset has no prefixes
     * service, when its prefix store is empty, and when fetching fails.
     */
    async loadPrefixes () {
      this.hidePopover()
      if (!this.prefixesService) {
        this.prefixes = defaultPrefixes()
        return
      }
      try {
        const res = await this.$fusekiService
          .getPrefixes(this.datasetName, this.prefixesService.endpoint)
        this.prefixes = res.data.length !== 0
          ? res.data.map(p => ({ text: p.prefix, uri: p.uri }))
          : defaultPrefixes()
      } catch (error) {
        this.prefixes = defaultPrefixes()
        displayError(this, error)
      }
    },
    /**
     * Validates both form fields for adding a prefix, turning on the
     * live validation display.
     *
     * @returns {boolean} true iff both fields are valid
     */
    validateAddPrefixForm () {
      this.addPrefixValidated = true
      return validatePrefixName(this.newPrefix.prefix) && validatePrefixUri(this.newPrefix.uri)
    },
    /**
     * Clears the add-prefix form fields and their validation state.
     */
    resetAddPrefixForm () {
      this.newPrefix = {
        prefix: '',
        uri: ''
      }
      this.addPrefixValidated = false
    },
    /**
     * Shows the add-prefix form (replacing the "+" pill) and focuses
     * its first input.
     */
    openAddPrefixForm () {
      this.showAddPrefixForm = true
      this.$nextTick(() => {
        const input = document.getElementById('add-prefix-name')
        if (input) {
          input.focus()
        }
      })
    },
    /**
     * Hides the add-prefix form (restoring the "+" pill) and resets it.
     */
    closeAddPrefixForm () {
      this.showAddPrefixForm = false
      this.resetAddPrefixForm()
    },
    /**
     * Adds or replaces a prefix mapping on the dataset via its
     * read-write prefixes endpoint, then refetches the list.
     */
    async addPrefix () {
      if (this.addingPrefix || !this.prefixesWritable) {
        return
      }
      if (!this.validateAddPrefixForm()) {
        return
      }
      this.addingPrefix = true
      try {
        await this.$fusekiService
          .updatePrefix(this.datasetName, this.prefixesService.endpoint, this.newPrefix.prefix, this.newPrefix.uri)
        displayNotification(this, `Prefix ${this.newPrefix.prefix} added`)
        this.closeAddPrefixForm()
        await this.loadPrefixes()
      } catch (error) {
        // Surface the server's validation message
        displayError(this, (error.response && error.response.data) || error)
      } finally {
        this.addingPrefix = false
      }
    },
    /**
     * Removes a prefix mapping from the dataset via its read-write
     * prefixes endpoint, then refetches the list.
     *
     * @param {{text: string, uri: string}} prefix - The prefix badge entry to remove.
     */
    async removePrefix (prefix) {
      try {
        await this.$fusekiService
          .removePrefix(this.datasetName, this.prefixesService.endpoint, prefix.text)
        displayNotification(this, `Prefix ${prefix.text} removed`)
        await this.loadPrefixes()
      } catch (error) {
        displayError(this, (error.response && error.response.data) || error)
      }
    },
    /**
     * Opens the confirmation popover for the given element id prefix,
     * closing any other popover first.
     *
     * @param {string} id - Id prefix shared by the popover's trigger button.
     */
    showPopover (id) {
      if (this.currentPopover !== null) {
        if (this.currentPopover.__id === id) {
          return
        }
        this.hidePopover()
      }
      const unwrap = ref => Array.isArray(ref) ? ref[0] : ref
      const content = unwrap(this.$refs[`${id}-content`])
      const trigger = unwrap(this.$refs[`${id}-button`])
      const popover = new Popover(trigger, {
        html: true,
        content,
        trigger: 'manual',
        placement: 'auto'
      })
      popover.__id = id
      popover.show()
      this.currentPopover = popover
    },
    /**
     * Closes the currently open confirmation popover, if any.
     */
    hidePopover () {
      if (this.currentPopover === null) {
        return
      }
      this.currentPopover.hide()
      this.currentPopover.dispose()
      this.currentPopover = null
    }
  },

  beforeUnmount () {
    this.hidePopover()
  }
}
</script>

<style lang="scss">
@import '@zazuko/yasqe/build/yasqe.min.css';
@import '@zazuko/yasr/build/yasr.min.css';

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
.yasr .yasr_btnGroup .select_geo .plugin_icon {
  margin-bottom: 20%;
}
.badge .btn-close.remove-prefix {
  font-size: .65em;
}
.badge.add-prefix-pill {
  cursor: pointer;
  color: #6c757d;
  background-color: transparent;
  border: 1px dashed #adb5bd;
  opacity: .45;
  transition: opacity .15s ease-in-out;
}
.badge.add-prefix-pill:hover,
.badge.add-prefix-pill:focus {
  opacity: 1;
}
</style>
