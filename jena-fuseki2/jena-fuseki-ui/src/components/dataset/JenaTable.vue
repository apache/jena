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
  <table
    :aria-colcount="fields.length"
    :aria-busy="busy"
    :class="getTableClasses()"
    role="table"
  >
    <thead role="rowgroup">
      <tr role="row">
        <jena-table-th
          v-for="(field, index) of fields"
          :key="`header-field-${index}`"
          :index="index"
          :name="getFieldLabel(field)"
          :sortable="field.sortable"
          @toggle-sort-status="sortColumn"
        ></jena-table-th>
      </tr>
    </thead>
    <tbody role="rowgroup">
      <!-- busy / loading slot -->
      <tr
        v-if="busy"
        role="row"
        class="jena-table-table-busy"
      >
        <td
          :colspan="fields.length"
          role="cell"
        >
          <slot name="table-busy">
          </slot>
        </td>
      </tr>
      <!-- nothing found for filter slot -->
      <tr
        v-else-if="filter && filter.length > 0 && filteredItems.length === 0"
        role="row"
        class="jena-table-empty-filtered"
      >
        <td
          :colspan="fields.length"
          role="cell"
        >
          <div role="alert" aria-live="polite">
            <slot name="empty-filtered">
              <h4>{{ emptyFilteredText }}</h4>
            </slot>
          </div>
        </td>
      </tr>
      <!-- empty / no data slot -->
      <tr
        v-else-if="filteredItems.length === 0"
        role="row"
        class="jena-table-empty"
      >
        <td
          :colspan="fields.length"
          role="cell"
        >
          <div role="alert" aria-live="polite">
            <slot name="empty">
              <h4>{{ emptyText }}</h4>
            </slot>
          </div>
        </td>
      </tr>
      <!-- cell slot -->
      <tr
        v-else
        v-for="(item, rowIndex) of filteredItems"
        :key="`jena-table-tbody-row-${rowIndex}`"
        role="row"
        class="jena-table-cell"
      >
        <template v-for="(field, fieldIndex) of fields">
          <td
            :key="`jena-table-tbody-row-${rowIndex}-field-${fieldIndex}`"
            :aria-colindex="fieldIndex + 1"
            role="cell"
          >
            <slot
              :name="`cell(${getFieldName(field)})`"
              :item="item"
            >
              {{ item[getFieldName(field)] }}
            </slot>
          </td>
        </template>
      </tr>
    </tbody>
    <tfoot role="rowgroup">
      <slot
        name="custom-foot"
        :fields="this.fields"
      ></slot>
    </tfoot>
  </table>
</template>

<script>
import JenaTableTh from '@/components/dataset/JenaTableTh.vue'

export default {
  name: 'JenaTable',

  components: {
    JenaTableTh
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
    perPage: {
      type: Number,
      default: 5
    },
    currentPage: {
      type: Number,
      default: -1
    },
    filter: String,
    busy: {
      type: Boolean,
      default: false
    },
    emptyText: {
      type: String,
      default: 'No data'
    },
    emptyFilteredText: {
      type: String,
      default: 'No data found after filtering it'
    },
    bordered: Boolean,
    fixed: Boolean,
    hover: Boolean,
    striped: Boolean,
    small: Boolean
  },

  data () {
    return {
      sortBy: {}
    }
  },

  computed: {
    currentPageItems () {
      return this.items
    },
    filteredItems () {
      // Sorting terms.
      const sortBy = this.sortBy
      // Apply the filter - if any - and sort the list.
      const filtered = this.items
        .filter(row => {
          if (!this.filter) {
            return true
          }
          for (const field of this.fields) {
            const cell = row[this.getFieldName(field)]
            if (typeof cell === 'string') {
              return cell.toLowerCase().includes(this.filter.toLowerCase())
            }
          }
          return false
        }).sort((left, right) => {
          const keys = Object.keys(sortBy)
          if (keys.length === 0) {
            return 0
          }
          const column = Object.keys(sortBy)[0]
          const order = this.sortBy[column] === 'ascending' ? 1 : -1
          return left[column].localeCompare(right[column]) * order
        })
      // Pagination terms. If currentPage is -1, then pagination is not needed.
      const fromIndex = this.currentPage === -1 ? 0 : (this.currentPage - 1) * this.perPage
      const toIndex = this.currentPage === -1 ? filtered.length : (this.currentPage * this.perPage)
      // Now slice it - if needed - for pagination.
      return filtered.slice(fromIndex, toIndex)
    }
  },

  methods: {
    getFieldName (field) {
      if (typeof field === 'string') {
        return field
      }
      return field.key
    },
    getFieldLabel (field) {
      if (typeof field === 'string') {
        return field
      }
      return field.label
    },
    sortColumn ({ field, order }) {
      this.sortBy = {
        [field]: order
      }
    },
    getTableClasses () {
      return {
        table: true,
        'jena-table': true,
        'table-striped': this.striped,
        'table-hover': this.hover,
        'table-bordered': this.bordered,
        'table-sm': this.small,
        'table-fixed': this.fixed
      }
    }
  }
}
</script>
