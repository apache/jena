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
  <th
    :aria-sort="ariaSort"
    :tabindex="tabIndex"
    @click="toggleSortStatus()"
    role="columnheader"
    scope="col"
  >
    <div>{{ name }}</div>
  </th>
</template>

<script>
export default {
  name: 'JenaTableTh',

  props: {
    index: {
      type: Number,
      default: -1
    },
    name: {
      type: String,
      required: true
    },
    sortable: {
      type: Boolean,
      default: false
    }
  },

  data () {
    return {
      sortStatus: 'none'
    }
  },

  computed: {
    ariaSort: function () {
      if (this.sortable === true) {
        return this.sortStatus
      }
      return null
    },
    tabIndex: function () {
      if (this.index === 0) {
        return '0'
      }
      return null
    }
  },

  emits: [
    'toggle-sort-status'
  ],

  methods: {
    toggleSortStatus () {
      if (this.sortable === true) {
        if (this.sortStatus !== 'ascending') {
          this.sortStatus = 'ascending'
        } else {
          this.sortStatus = 'descending'
        }
        this.$emit('toggle-sort-status', {
          field: this.name,
          order: this.sortStatus
        })
      }
    }
  }
}
</script>
