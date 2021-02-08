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
        <h2>Manage datasets</h2>
        <b-card no-body>
          <b-card-header header-tag="nav">
            <Menu />
          </b-card-header>
          <b-card-body>
            <div>
              <b-row>
                <b-col>
                  <table-listing
                    :fields="fields"
                    :items="items"
                    :is-busy="isBusy"
                  >
                    <template v-slot:cell(actions)="data">
                      <b-popover
                        :target="`delete-dataset-${data.index}-button`"
                        triggers="manual"
                        placement="auto"
                      >
                        <template v-slot:title>
                          <b-button
                            @click="hidePopover(`delete-dataset-${data.index}-button`)"
                            class="close"
                            aria-label="Close">
                            <span class="d-inline-block" aria-hidden="true">&times;</span>
                          </b-button>
                          Confirm
                        </template>
                        <div class="text-center">
                          <b-alert show variant="danger">Are you sure you want to delete dataset {{ data.item.name }}?<br/><br/>This action cannot be reversed.</b-alert>
                          <b-button
                            @click="hidePopover(`delete-dataset-${data.index}-button`);deleteDataset(data.item.name)"
                            variant="primary"
                            class="mr-2"
                          >submit</b-button>
                          <b-button
                            @click="hidePopover(`delete-dataset-${data.index}-button`)"
                          >cancel</b-button>
                        </div>
                      </b-popover>
                      <b-button
                        :id="`delete-dataset-${data.index}-button`"
                        :ref="`delete-dataset-${data.index}-button`"
                        @click="showPopover(`delete-dataset-${data.index}-button`)"
                        variant="primary"
                        href="#"
                        class="mr-0 mr-md-2 mb-2 mb-md-0 d-block d-md-inline-block">
                        <FontAwesomeIcon icon="times-circle" />
                        <span class="ml-1">remove</span>
                      </b-button>
                      <b-popover
                        :target="`backup-dataset-${data.index}-button`"
                        triggers="manual"
                        placement="auto"
                      >
                        <template v-slot:title>
                          <b-button
                            @click="showPopover(`backup-dataset-${data.index}-button`)"
                            class="close"
                            aria-label="Close">
                            <span class="d-inline-block" aria-hidden="true">&times;</span>
                          </b-button>
                          Confirm
                        </template>
                        <div class="text-center">
                          <b-alert show variant="warning">Are you sure you want to create a backup of dataset {{ data.item.name }}?<br/><br/>This action may take some time to complete.</b-alert>
                          <b-button
                            @click="hidePopover(`backup-dataset-${data.index}-button`);backupDataset(data.item.name)"
                            variant="primary"
                            class="mr-2"
                          >submit</b-button>
                          <b-button
                            @click="hidePopover(`backup-dataset-${data.index}-button`)"
                          >cancel</b-button>
                        </div>
                      </b-popover>
                      <b-button
                        :id="`backup-dataset-${data.index}-button`"
                        :ref="`backup-dataset-${data.index}-button`"
                        @click="showPopover(`backup-dataset-${data.index}-button`)"
                        variant="primary"
                        href="#"
                        class="mr-0 mr-md-2 mr-0 mb-2 mb-md-0 d-block d-md-inline-block">
                        <FontAwesomeIcon icon="download" />
                        <span class="ml-1">backup</span>
                      </b-button>
                      <b-button
                        :to="`/dataset${data.item.name}/upload`"
                        variant="primary"
                        class="mr-0 mb-md-0 d-block d-md-inline-block">
                        <FontAwesomeIcon icon="upload" />
                        <span class="ml-1">add data</span>
                      </b-button>
                    </template>
                  </table-listing>
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
import Menu from '@/components/manage/Menu'
import listDatasets from '@/mixins/list-datasets'
import TableListing from '@/components/dataset/TableListing'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faTimesCircle, faDownload, faTachometerAlt } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

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
      this.$bvToast.toast(`Dataset ${datasetName} deleted`, {
        title: 'Notification',
        autoHideDelay: 5000,
        appendToast: false
      })
      this.initializeData()
    },
    async backupDataset (datasetName) {
      const response = await this.$fusekiService.backupDataset(datasetName)
      const taskId = response.data.taskId
      this.$bvToast.toast(`Backup task ${taskId} scheduled. Click on tasks for more.`, {
        title: 'Notification',
        autoHideDelay: 5000,
        appendToast: false
      })
      this.initializeData()
    },
    hidePopover (id) {
      this.$root.$emit('bv::hide::popover', id)
    },
    showPopover (id) {
      if (this.currentPopover !== null) {
        this.$root.$emit('bv::hide::popover', this.currentPopover)
      }
      this.$root.$emit('bv::show::popover', id)
      this.currentPopover = id
    }
  }
}
</script>
