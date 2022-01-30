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
              <div v-show="$refs.upload && $refs.upload.dropActive" class="drop-active">
                <h3>Drop files to upload</h3>
              </div>
              <b-row>
                <b-col sm="12">
                  <h3>Upload files</h3>
                  <p>Load data into the default graph of the currently selected dataset, or the given named graph.
                    You may upload any RDF format, such as Turtle, RDF/XML or TRiG.</p>
                  <b-form>
                    <b-form-group
                      id="dataset-graph-name-group"
                      label="Dataset graph name"
                      label-for="dataset-graph-name"
                      label-cols="12"
                      label-cols-sm="4"
                      label-cols-md="4"
                      label-cols-lg="2"
                      label-size="sm"
                    >
                      <b-form-input
                        pattern="[^\s]+"
                        oninvalid="this.setCustomValidity('Enter a valid dataset graph name')"
                        oninput="this.setCustomValidity('')"
                        id="dataset-graph-name"
                        v-model="form.datasetGraphName"
                        type="text"
                        placeholder="Leave blank for default graph"
                        trim
                      ></b-form-input>
                    </b-form-group>
                    <b-form-group
                      id="dataset-files"
                      label="Files to upload"
                      label-for="add-files-action-dropdown"
                      label-cols="12"
                      label-cols-sm="4"
                      label-cols-md="4"
                      label-cols-lg="2"
                      label-size="sm"
                    >
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
                        class="btn btn-success"
                      >
                        <FontAwesomeIcon icon="plus" />
                        <span class="ml-2">select files</span>
                      </file-upload>
                      <b-button
                        v-if="!$refs.upload || !$refs.upload.active"
                        @click.prevent="$refs.upload.active = true"
                        variant="primary"
                        class="ml-2 d-inline">
                        <FontAwesomeIcon icon="upload" />
                        <span class="ml-2">upload all</span>
                      </b-button>
                      <b-button
                        v-else
                        @click.prevent="$refs.upload.active = false"
                        variant="primary"
                        class="ml-2 d-inline">
                        <FontAwesomeIcon icon="times-circle" />
                        <span class="ml-2">stop upload</span>
                      </b-button>
                    </b-form-group>
                  </b-form>
                </b-col>
              </b-row>
              <b-row>
                <b-col>
                  <b-table
                    :fields="datasetTableFields"
                    :items="datasetTableItems"
                    stacked="lg"
                    empty-text="No files selected"
                    bordered
                    fixed
                    hover
                    show-empty
                  >
                    <template v-slot:cell(size)="data">
                      {{ readableFileSize(data.item.size) }}
                    </template>
                    <template v-slot:cell(speed)="data">
                      {{ readableFileSize(data.item.speed) }}/s
                    </template>
                    <template v-slot:cell(status)="data">
                      <b-progress
                        :variant="getFileStatus(data.item)"
                        :value="data.item.progress"
                        :max="100"
                        :precision="2"
                        show-progress></b-progress>
                      <span class="small">Triples uploaded:&nbsp;</span>
                      <span v-if="data.item.response.tripleCount" class="small">
                        {{ data.item.response.tripleCount }}
                      </span>
                      <span v-else class="small">0</span>
                    </template>
                    <template v-slot:cell(actions)="data">
                      <b-button
                        @click.prevent="data.item.success || data.item.error === 'compressing' ? false : $refs.upload.update(data.item, {active: true})"
                        variant="outline-primary"
                        class="mr-0 mb-2 d-block"
                      >
                        <FontAwesomeIcon icon="upload" />
                        <span class="ml-2">upload now</span>
                      </b-button>
                      <b-button
                        @click.prevent="remove(data.item)"
                        variant="outline-primary"
                        class="mr-0 mb-md-0 d-block d-md-inline-block"
                      >
                        <FontAwesomeIcon icon="minus-circle" />
                        <span class="ml-2">remove</span>
                      </b-button>
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
import FileUpload from 'vue-upload-component'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faPlus, faUpload, faTimesCircle, faMinusCircle } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faPlus, faUpload, faTimesCircle, faMinusCircle)

export default {
  name: 'DatasetUpload',

  components: {
    Menu,
    FontAwesomeIcon,
    FileUpload
  },

  props: {
    datasetName: {
      type: String,
      required: true
    }
  },

  data () {
    return {
      form: {
        datasetGraphName: null,
        datasetFiles: null
      },
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
        data: {
        },
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
      const params = (this.form.datasetGraphName && this.form.datasetGraphName !== '') ? `?graph=${this.form.datasetGraphName}` : ''
      return this.$fusekiService.getFusekiUrl(`/${this.datasetName}/data${params}`)
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
    }
  }
}
</script>
