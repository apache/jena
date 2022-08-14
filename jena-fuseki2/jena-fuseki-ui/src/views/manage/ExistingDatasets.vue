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
        <h2>Manage datasets</h2>
        <div class="card">
          <nav class="card-header">
            <Menu />
          </nav>
          <div class="card-body">
            <div>
              <div class="row">
                <div class="col">
                  <table-listing
                    :fields="fields"
                    :items="items"
                    :is-busy="isBusy"
                  >
                    <template v-slot:empty>
                      <h4>No datasets created - <router-link to="/manage/new">add one</router-link></h4>
                    </template>
                    <template v-slot:cell(actions)="data">
                      <button
                        @click="$router.push(`/dataset${data.item.name}/query`)"
                        type="button"
                        class="btn btn-primary me-0 me-md-2 mb-2 mb-md-0 d-block d-md-inline-block">
                        <FontAwesomeIcon icon="question-circle" />
                        <span class="ms-1">query</span>
                      </button>
                      <!-- content for the delete dataset popover -->
                      <div class="popover" role="popover" hidden>
                        <div :ref="`delete-dataset-${data.item.name}-content`">
                          <div>
                            Confirm
                          </div>
                          <div class="text-center">
                            <div class="alert alert-danger">Are you sure you want to delete dataset {{ data.item.name }}?<br/><br/>This action cannot be reversed.</div>
                            <button
                              @click="hidePopover();deleteDataset(data.item.name)"
                              class="btn btn-primary me-2"
                            >submit</button>
                            <button
                              @click="hidePopover()"
                              type="button"
                              class="btn btn-secondary"
                            >cancel</button>
                          </div>
                        </div>
                      </div>
                      <button
                        :id="`delete-dataset-${data.item.name}-button`"
                        :ref="`delete-dataset-${data.item.name}-button`"
                        @click="showPopover(`delete-dataset-${data.item.name}`)"
                        type="button"
                        href="#"
                        class="btn btn-primary me-0 me-md-2 mb-2 mb-md-0 d-block d-md-inline-block"
                      >
                        <FontAwesomeIcon icon="times-circle" />
                        <span class="ms-1">remove</span>
                      </button>
                      <div class="popover" role="popover" hidden>
                        <div :ref="`backup-dataset-${data.item.name}-content`">
                          <div>
                            Confirm
                          </div>
                          <div class="text-center">
                            <div class="alert alert-danger">Are you sure you want to create a backup of dataset {{ data.item.name }}?<br/><br/>This action may take some time to complete.</div>
                            <button
                              @click="hidePopover();backupDataset(data.item.name)"
                              type="button"
                              class="btn btn-primary me-2"
                            >submit</button>
                            <button
                              @click="hidePopover()"
                              type="button"
                              class="btn btn-secondary"
                            >cancel</button>
                          </div>
                        </div>
                      </div>
                      <button
                        :id="`backup-dataset-${data.item.name}-button`"
                        :ref="`backup-dataset-${data.item.name}-button`"
                        @click="showPopover(`backup-dataset-${data.item.name}`)"
                        type="button"
                        href="#"
                        class="btn btn-primary me-0 me-md-2 me-0 mb-2 mb-md-0 d-block d-md-inline-block"
                      >
                        <FontAwesomeIcon icon="download" />
                        <span class="ms-1">backup</span>
                      </button>
                      <button
                        @click="$router.push(`/dataset${data.item.name}/upload`)"
                        type="button"
                        class="btn btn-primary me-0 me-md-2 me-0 mb-2 mb-md-0 d-block d-md-inline-block">
                        <FontAwesomeIcon icon="upload" />
                        <span class="ms-1">add data</span>
                      </button>
                      <button
                        @click="$router.push(`/dataset${data.item.name}/info`)"
                        type="button"
                        class="btn btn-primary me-0 mb-md-0 d-block d-md-inline-block">
                        <FontAwesomeIcon icon="tachometer-alt" />
                        <span class="ms-1">info</span>
                      </button>
                    </template>
                  </table-listing>
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
import Menu from '@/components/manage/Menu'
import listDatasets from '@/mixins/list-datasets'
import TableListing from '@/components/dataset/TableListing'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faTimesCircle, faDownload, faTachometerAlt } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { Popover } from 'bootstrap'
import { displayNotification } from '@/utils'

library.add(faTimesCircle, faDownload, faTachometerAlt)

export default {
  name: 'ManageExistingDatasets',

  mixins: [
    listDatasets
  ],

  data () {
    return {
      currentPopover: null
    }
  },

  components: {
    FontAwesomeIcon,
    Menu,
    'table-listing': TableListing
  },

  methods: {
    async deleteDataset (datasetName) {
      await this.$fusekiService.deleteDataset(datasetName)
      displayNotification(this, `Dataset ${datasetName} deleted`)
      this.initializeData()
    },
    async backupDataset (datasetName) {
      const response = await this.$fusekiService.backupDataset(datasetName)
      const taskId = response.data.taskId
      displayNotification(this, `Backup task ${taskId} scheduled. Click on tasks for more.`)
      this.initializeData()
    },
    hidePopover () {
      this.currentPopover.hide()
      this.currentPopover = null
    },
    showPopover (id) {
      if (this.currentPopover !== null) {
        if (this.currentPopover.__id === id) {
          return
        }
        this.hidePopover()
      }
      const popoverOptions = {
        html: true,
        content: this.$refs[`${id}-content`],
        trigger: 'manual',
        placement: 'auto'
      }
      const popoverElement = this.$refs[`${id}-button`]
      this.currentPopover = new Popover(popoverElement, popoverOptions)
      this.currentPopover.__id = id
      this.currentPopover.show()
    }
  }
}
</script>
