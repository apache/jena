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
  <ul
    role="menubar"
    aria-disabled="false"
    aria-label="Pagination"
    class="pagination flex-wrap mb-2 mx-0 p-0 justify-content-center"
  >
    <!-- pages backward controls -->
    <li
      :aria-hidden="isBackLinkAriaDisabled"
      :class="getBackLinkClass('page-item')"
      role="presentation"
    >
      <button
        :aria-disabled="isBackLinkAriaDisabled"
        :class="getBackLinkClass('page-link')"
        @click="goToPage(1)"
        type="button"
        role="menuitem"
        tabindex="-1"
        aria-label="Go to first page"
      >
        «
      </button>
    </li>
    <li
      :aria-hidden="isBackLinkAriaDisabled"
      :class="getBackLinkClass('page-item')"
      role="presentation"
    >
      <button
        :aria-disabled="isBackLinkAriaDisabled"
        :class="getBackLinkClass('page-link')"
        @click="goToPage(currentPage - 1)"
        type="button"
        aria-label="Go to previous page"
        role="menuitem"
      >
        ‹
      </button>
    </li>
    <li
      v-if="isBackSummaryEnabled"
      role="presentation"
      class="page-link page-summary-item"
    >
      <span>…</span>
    </li>
    <!-- pages -->
    <li
      v-for="page in pages"
      :key="page"
      role="presentation"
      class="page-item"
    >
      <span
        :aria-label="`Go to page ${ page }`"
        :class="getPageLinkClass(page)"
        :aria-checked="page === currentPage"
        @click="goToPage(page)"
        role="menuitemradio"
        type="button"
        aria-posinset="1"
        aria-setsize="2"
        tabindex="0"
      >{{ page }}</span>
    </li>
    <!-- pages forward controls -->
    <li
      v-if="isAfterSummaryEnabled"
      role="presentation"
      class="page-link page-summary-item"
    >
      <span>…</span>
    </li>
    <li
      :aria-hidden="isNextLinkAriaDisabled"
      :class="getNextLinkClass('page-item')"
      role="presentation"
    >
      <button
        :aria-disabled="isNextLinkAriaDisabled"
        :class="getNextLinkClass('page-link')"
        @click="goToPage(currentPage + 1)"
        type="button"
        role="menuitem"
        aria-label="Go to last page"
      >
        ›
      </button>
    </li>
    <li
      :aria-hidden="isNextLinkAriaDisabled"
      :class="getNextLinkClass('page-item')"
      role="presentation"
    >
      <button
        :aria-disabled="isNextLinkAriaDisabled"
        :class="getNextLinkClass('page-link')"
        @click="goToPage(numberOfPages)"
        type="button"
        role="menuitem"
        aria-label="Go to next page"
      >
        »
      </button>
    </li>
  </ul>
</template>

<script>
function range (size, startAt) {
  return [...Array(size).keys()].map(i => i + startAt)
}

export default {
  name: 'Pagination',

  props: {
    totalRows: {
      type: Number,
      required: true
    },
    /* For v-mount, it is the current-page value. */
    value: {
      type: Number,
      default: 0
    },
    perPage: {
      type: Number,
      default: 5
    },
    /* Maximum pages displayed. */
    maxDisplayed: {
      type: Number,
      default: 5
    }
  },

  computed: {
    currentPage () {
      return this.value
    },
    numberOfPages () {
      return Math.ceil(this.totalRows / this.perPage) || 0
    },
    isSummaryEnabled () {
      return this.numberOfPages > this.maxDisplayed
    },
    pages () {
      if (this.totalRows === 0) {
        return []
      }
      const toBeDisplayed = Math.min(this.maxDisplayed, this.numberOfPages)
      if (!this.isBackSummaryEnabled) {
        return range(toBeDisplayed, 1)
      }
      if (!this.isAfterSummaryEnabled) {
        return range(toBeDisplayed, this.numberOfPages - (this.maxDisplayed - 1))
      }
      return range(toBeDisplayed, this.currentPage - Math.floor(this.maxDisplayed / 2))
    },
    isBackLinkAriaDisabled () {
      return this.currentPage === 1 || this.numberOfPages === 0
    },
    isNextLinkAriaDisabled () {
      return this.currentPage === this.numberOfPages || this.numberOfPages === 0
    },
    isBackSummaryEnabled () {
      return this.isSummaryEnabled && this.currentPage - Math.ceil(this.maxDisplayed / 2) > 0
    },
    isAfterSummaryEnabled () {
      return this.isSummaryEnabled && this.currentPage + Math.floor(this.maxDisplayed / 2) < this.numberOfPages
    }
  },

  emits: [
    'input'
  ],

  methods: {
    goToPage (page) {
      this.$emit('input', page)
    },
    getPageLinkClass (page) {
      const classes = ['page-link']
      if (page === this.currentPage) {
        classes.push('active')
      }
      return classes
    },
    getBackLinkClass (mainClass) {
      return {
        [mainClass]: true,
        disabled: this.currentPage === 1 || this.numberOfPages === 0
      }
    },
    getNextLinkClass (mainClass) {
      return {
        [mainClass]: true,
        disabled: this.currentPage === this.numberOfPages || this.numberOfPages === 0
      }
    }
  }
}
</script>
