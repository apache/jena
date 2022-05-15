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
        <h2 class="text-center">Apache Jena Fuseki</h2>
        <div class="text-center">
          <span class="badge text-bg-secondary">{{ headerString }}</span>
        </div>
      </div>
    </div>
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
        <button
          @click="$router.push(`/dataset${data.item.name}/upload`)"
          type="button"
          class="btn btn-primary me-0 me-md-2 mb-2 mb-md-0 d-block d-md-inline-block">
          <FontAwesomeIcon icon="upload" />
          <span class="ms-1">add data</span>
        </button>
        <button
          @click="$router.push(`/dataset${data.item.name}/edit`)"
          type="button"
          class="btn btn-primary me-0 me-md-2 mb-2 mb-md-0 d-block d-md-inline-block">
          <FontAwesomeIcon icon="edit" />
          <span class="ms-1">edit</span>
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
