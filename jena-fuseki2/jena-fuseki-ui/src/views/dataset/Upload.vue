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
          <div class="card-body" v-if="this.services !== null && (!this.services['gsp-rw'] || this.services['gsp-rw'].length === 0)">
            <div class="alert alert-warning">No service for adding data available. The Graph Store Protocol service should be configured to allow adding data.</div>
          </div>
          <div class="card-body" v-else>
            <div v-show="$refs.upload && $refs.upload.dropActive" class="drop-active">
              <h3>Drop files to upload</h3>
            </div>
            <div class="row">
              <div class="col-sm-12">
                <h3>Upload files</h3>
                <p>Load data into the default graph of the currently selected dataset, or the given named graph.
                  You may upload any RDF format, such as Turtle, RDF/XML or TRiG.</p>
                <form ref="upload-form" novalidate>
                  <div
                    id="dataset-graph-name-group"
                    role="group"
                    class="form-row form-group"
                  >
                    <label
                      for="dataset-graph-name"
                      class="col-sm-4 col-md-4 col-lg-2 col-12 col-form-label col-form-label-sm"
                    >Dataset graph name</label>
                    <div class="col input-group has-validation">
                      <input
                        v-model="datasetGraphName"
                        :class="graphNameClasses"
                        id="dataset-graph-name"
                        ref="dataset-graph-name"
                        type="text"
                        placeholder="Leave blank for default graph"
                      />
                      <div class="invalid-feedback">
                        Invalid graph name. Please remove any spaces.
                      </div>
                    </div>
                  </div>
                  <div
                    id="dataset-files"
                    role="group"
                    class="form-row form-group"
                  >
                    <label
                      for="add-files-action-dropdown"
                      class="col-sm-4 col-md-4 col-lg-2 col-12 col-form-label col-form-label-sm"
                    >Files to upload</label>
                    <div class="col has-validation">
                      <file-upload
                        ref="upload"
                        v-model="upload.files"
                        :post-action="postActionUrl"
                        :extensions="upload.extensions"
                        :accept="upload.accept"
                        :multiple="upload.multiple"
                        :directory="upload.directory"
                        :size="upload.size || 0"
                        :thread="upload.thread < 1 ? 1 : (upload.thread > 5 ? 5 : upload.thread)"
                        :headers="upload.headers"
                        :data="upload.data"
                        :drop="upload.drop"
                        :drop-directory="upload.dropDirectory"
                        :add-index="upload.addIndex"
                        :class="fileUploadClasses"
                      >
                        <FontAwesomeIcon icon="plus" />
                        <span class="ms-2">select files</span>
                      </file-upload>
                      <button
                        v-if="!$refs.upload || !$refs.upload.active"
                        @click.prevent="uploadAll()"
                        type="button"
                        class="btn btn-primary ms-2 d-inline">
                        <FontAwesomeIcon icon="upload" />
                        <span class="ms-2">upload all</span>
                      </button>
                      <button
                        v-else
                        @click.prevent="$refs.upload.active = false"
                        type="button"
                        class="btn btn-primary ms-2 d-inline">
                        <FontAwesomeIcon icon="times-circle" />
                        <span class="ms-2">stop upload</span>
                      </button>
                      <div class="invalid-feedback">
                        Invalid upload files. Please select at least one file to upload.
                      </div>
                    </div>
                  </div>
                </form>
              </div>
            </div>
            <div class="row">
              <div class="col">
                <jena-table
                  :fields="datasetTableFields"
                  :items="datasetTableItems"
                  empty-text="No files selected"
                  bordered
                  fixed
                  hover
                >
                  <template v-slot:cell(size)="data">
                    {{ readableFileSize(data.item.size) }}
                  </template>
                  <template v-slot:cell(speed)="data">
                    {{ readableFileSize(data.item.speed) }}/s
                  </template>
                  <template v-slot:cell(status)="data">
                    <div class="progress">
                      <div
                        :class="`progress-bar bg-${getFileStatus(data.item)}`"
                        :style="`width: ${data.item.progress}%`"
                        :aria-valuenow="`${data.item.progress}`"
                        aria-valuemin="0"
                        aria-valuemax="100"
                        role="progressbar"
                      >{{ data.item.progress }}</div>
                    </div>
                    <span class="small">Triples uploaded:&nbsp;</span>
                    <span v-if="data.item.response.tripleCount" class="small">
                      {{ data.item.response.tripleCount }}
                    </span>
                    <span v-else class="small">0</span>
                  </template>
                  <template v-slot:cell(actions)="data">
                    <button
                      @click.prevent="data.item.success || data.item.error === 'compressing' ? false : $refs.upload.update(data.item, {active: true})"
                      type="button"
                      class="btn btn-outline-primary me-0 mb-2 d-block"
                    >
                      <FontAwesomeIcon icon="upload" />
                      <span class="ms-2">upload now</span>
                    </button>
                    <button
                      @click.prevent="remove(data.item)"
                      type="button"
                      class="btn btn-outline-primary me-0 mb-md-0 d-block d-md-inline-block"
                    >
                      <FontAwesomeIcon icon="minus-circle" />
                      <span class="ms-2">remove</span>
                    </button>
                  </template>
                </jena-table>
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
import FileUpload from 'vue-upload-component'
import JenaTable from '@/components/dataset/JenaTable'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faPlus, faUpload, faTimesCircle, faMinusCircle } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import currentDatasetMixin from '@/mixins/current-dataset'

library.add(faPlus, faUpload, faTimesCircle, faMinusCircle)

export default {
  name: 'DatasetUpload',

  components: {
    Menu,
    FontAwesomeIcon,
    FileUpload,
    JenaTable
  },

  mixins: [
    currentDatasetMixin
  ],

  data () {
    return {
      datasetGraphName: null,
      datasetFiles: null,
      graphNameClasses: [
        'form-control'
      ],
      fileUploadClasses: [
        'btn',
        'btn-success'
      ],
      upload: {
        files: [],
        accept: '', // e.g. 'ttl,xml,rdf,...'
        minSize: 0,
        // size: 1024 * 1024 * 10,
        multiple: true,
        directory: false,
        drop: true,
        dropDirectory: true,
        addIndex: false,
        thread: 3,
        name: 'file',
        headers: { // e.g. CSRF headers
        },
        data: {},
        autoCompress: 1024 * 1024,
        uploadAuto: false,
        isOption: false
        // addData: {
        //   show: false,
        //   name: '',
        //   type: '',
        //   content: ''
        // },
        // editFile: {
        //   show: false,
        //   name: ''
        // }
      },
      datasetTableFields: [
        {
          key: 'name',
          label: 'name',
          sortable: true,
          sortDirection: 'asc'
        },
        {
          key: 'size',
          label: 'size',
          sortable: true,
          sortDirection: 'asc'
        },
        {
          key: 'speed',
          label: 'speed'
        },
        {
          key: 'status',
          label: 'status'
        },
        {
          key: 'actions',
          label: 'actions'
        }
      ]
    }
  },

  computed: {
    datasetTableItems () {
      if (!this.upload.files) {
        return []
      }
      return this.upload.files
        .map(file => {
          return Object.assign(file, {
            status: this.getFileStatus(file)
          })
        })
    },
    postActionUrl () {
      if (this.services === null || !this.services['gsp-rw'] || this.services['gsp-rw'].length === 0) {
        return ''
      }
      const params = (this.datasetGraphName && this.datasetGraphName !== '') ? `?graph=${this.datasetGraphName}` : ''
      const dataEndpoint = this.services['gsp-rw']['srv.endpoints'].find(endpoint => endpoint !== '') || ''
      return this.$fusekiService.getFusekiUrl(`/${this.datasetName}/${dataEndpoint}${params}`)
    }
  },

  watch: {
    datasetGraphName () {
      this.validateGraphName()
    },
    upload: {
      handler () {
        this.validateFiles()
      },
      deep: true,
      immediate: false
    }
  },

  methods: {
    getFileStatus (file) {
      if (file.error) {
        // eslint-disable-next-line no-console
        console.error(file)
        return 'danger'
      }
      if (file.success) {
        return 'success'
      }
      if (file.active) {
        return 'warning'
      }
      return ''
    },
    remove (file) {
      this.$refs.upload.remove(file)
    },
    /**
     * Return file size in bytes in a human-readable form.
     *
     * Copied from the Backbone.js code with modifications.
     *
     * Replaced sprintf usage by .toFixed(2).
     */
    readableFileSize: function (size) {
      const k = 1024
      const m = k * k

      if (size >= m) {
        return `${(size / m).toFixed(2).replace(/\.?0*$/, '')}mb`
      } else if (size >= k) {
        return `${(size / k).toFixed(2).replace(/\.?0*$/, '')}kb`
      } else {
        return `${size} bytes`
      }
    },
    uploadAll () {
      if (this.validateForm()) {
        this.$refs.upload.active = true
      }
    },
    validateForm () {
      return this.validateGraphName() && this.validateFiles()
    },
    validateGraphName () {
      // No spaces allowed in graph names.
      const pattern = /^[^\s]+$/
      const graphName = this.$refs['dataset-graph-name'].value
      if (graphName === '' || pattern.test(graphName)) {
        this.graphNameClasses = ['form-control is-valid']
        return true
      }
      this.graphNameClasses = ['form-control is-invalid']
      return false
    },
    validateFiles () {
      if (this.upload.files !== null && this.upload.files.length > 0) {
        this.fileUploadClasses = [
          'btn',
          'btn-success',
          'is-valid'
        ]
        return true
      }
      this.fileUploadClasses = [
        'btn',
        'btn-success',
        'is-invalid'
      ]
      return false
    }
  }
}
</script>
