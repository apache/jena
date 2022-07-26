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
    <div class="row" v-if="filterable">
      <div class="col-12 input-group has-validation align-items-center my-2 g-0">
        <div class="col g-0">
          <div class="input-group">
            <input
              v-model="filter"
              :placeholder="placeholder"
              type="search"
              id="filterInput"
              class="form-control"
            />
            <button
              :disabled="!filter"
              @click="filter = ''"
              type="button"
              class="btn btn-secondary input-group-text"
            >
              Clear
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-12 g-0">
        <jena-table
          :current-page="currentPage"
          :fields="fields"
          :filter="filter"
          :items="items"
          :busy="isBusy"
          :per-page="perPage"
          :empty-text="emptyText"
          :empty-filtered-text="emptyFilteredText"
          bordered
          fixed
          hover
          striped
        >
          <template #table-busy>
            <div class="text-center text-danger my-2">
              <div class="spinner-border align-middle" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
              <strong>Loading...</strong>
            </div>
          </template>
          <template v-for="(_, slot) of $slots" #[slot]="scope">
            <slot :name="slot" v-bind="scope" />
          </template>
        </jena-table>
      </div>
    </div>
    <div class="row g-0">
      <div class="col-12">
        <pagination
          :value="currentPage"
          :per-page="perPage"
          :total-rows="items.length"
          @input="currentPage = $event"
        >
        </pagination>
      </div>
    </div>
  </div>
</template>

<script>
import JenaTable from '@/components/dataset/JenaTable'
import Pagination from '@/components/dataset/Pagination.vue'

export default {
  name: 'TableListing',

  emits: [
    'update:currentPage'
  ],

  components: {
    JenaTable,
    Pagination
  },

  props: {
    fields: {
      type: Array,
      required: true
    },
    items: {
      type: Array,
      required: true
    },
    isBusy: {
      type: Boolean,
      default: null
    },
    placeholder: {
      type: String,
      default: 'Filter datasets'
    },
    emptyText: {
      type: String,
      default: 'No datasets created'
    },
    emptyFilteredText: {
      type: String,
      default: 'No datasets found'
    },
    filterable: {
      type: Boolean,
      default: true
    }
  },

  data () {
    return {
      perPage: 5,
      currentPage: 1,
      filter: ''
    }
  }
}
</script>
