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
    <b-row v-if="filterable">
      <b-col cols="12">
        <b-form-group
          label-cols-sm="12"
          label-align-sm="left"
          label-size="md"
          label-for="filterInput"
          class="mb-2"
        >
          <b-input-group size="md">
            <b-form-input
              v-model="filter"
              :placeholder="placeholder"
              type="search"
              id="filterInput"
            ></b-form-input>
            <b-input-group-append>
              <b-button :disabled="!filter" @click="filter = ''">Clear</b-button>
            </b-input-group-append>
          </b-input-group>
        </b-form-group>
      </b-col>
    </b-row>
    <b-row>
      <b-col cols="12">
        <b-table
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
          show-empty
          striped
        >
          <template v-slot:table-busy>
            <div class="text-center text-danger my-2">
              <b-spinner class="align-middle"></b-spinner>
              <strong>Loading...</strong>
            </div>
          </template>
          <template v-slot:empty="scope">
            <h4>{{ scope.emptyText }}</h4>
          </template>
          <template v-slot:emptyfiltered="scope">
            <h4>{{ scope.emptyFilteredText }}</h4>
          </template>
          <template v-for="(_, slot) of $scopedSlots" v-slot:[slot]="scope"><slot :name="slot" v-bind="scope"/></template>
        </b-table>
      </b-col>
    </b-row>
    <b-row no-gutters>
      <b-col cols="12">
        <b-pagination
          v-model="currentPage"
          :per-page="perPage"
          :total-rows="items.length"
          align="center"
          size="md"
          class="mb-2 mx-0"
        >
        </b-pagination>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import tableMixin from '@/mixins/table'

export default {
  name: 'TableListing',

  mixins: [
    tableMixin
  ],

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
      default: false
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
  }
}
</script>
