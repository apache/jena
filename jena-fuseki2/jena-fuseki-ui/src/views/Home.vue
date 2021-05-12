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
    <b-row class="mt-2">
      <b-col cols="12">
        <h2 class="text-center">Apache Jena Fuseki</h2>
        <div class="text-center">
          <b-badge>{{ headerString }}</b-badge>
        </div>
      </b-col>
    </b-row>
    <table-listing
      :fields="fields"
      :items="items"
      :is-busy="isBusy"
    >
      <template v-slot:cell(actions)="data">
        <b-button
          :to="`/dataset${data.item.name}/query`"
          variant="primary"
          class="mr-0 mr-md-2 mb-2 mb-md-0 d-block d-md-inline-block">
          <FontAwesomeIcon icon="question-circle" />
          <span class="ml-1">query</span>
        </b-button>
        <b-button
          :to="`/dataset${data.item.name}/upload`"
          variant="primary"
          class="mr-0 mr-md-2 mb-2 mb-md-0 d-block d-md-inline-block">
          <FontAwesomeIcon icon="upload" />
          <span class="ml-1">add data</span>
        </b-button>
        <b-button
          :to="`/dataset${data.item.name}/edit`"
          variant="primary"
          class="mr-0 mr-md-2 mb-2 mb-md-0 d-block d-md-inline-block">
          <FontAwesomeIcon icon="edit" />
          <span class="ml-1">edit</span>
        </b-button>
        <b-button
          :to="`/dataset${data.item.name}/info`"
          variant="primary"
          class="mr-0 mb-md-0 d-block d-md-inline-block">
          <FontAwesomeIcon icon="tachometer-alt" />
          <span class="ml-1">info</span>
        </b-button>
      </template>
    </table-listing>
  </b-container>
</template>

<script>
import listDatasets from '@/mixins/list-datasets'
import TableListing from '@/components/dataset/TableListing'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faQuestionCircle, faUpload, faTachometerAlt, faEdit } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faQuestionCircle, faUpload, faTachometerAlt, faEdit)

export default {
  name: 'Home',

  mixins: [
    listDatasets
  ],

  components: {
    'table-listing': TableListing,
    FontAwesomeIcon
  },

  computed: {
    /**
     * Fuseki backend response.
     *
     * @type {null|{
     *   built: string,
     *   datasets: [],
     *   startDateTime: string,
     *   uptime: number,
     *   version: string
     * }}
     */
    headerString () {
      if (!this.serverData) {
        return ''
      }
      return `Version ${this.serverData.version}. Uptime ${this.convertUptime(this.serverData.uptime)}`
    }
  },

  methods: {
    convertUptime (uptime) {
      const s = uptime % 60
      const m = Math.floor((uptime / 60) % 60)
      const h = Math.floor((uptime / (60 * 60)) % 24)
      const d = Math.floor((uptime / (60 * 60 * 24)))
      return `${(d > 0 ? d + 'd' : '')} ${(h > 0 ? h + 'h' : '')} ${m}m ${(s < 9 ? '0' + s : s)}s`
    }
  }
}
</script>
