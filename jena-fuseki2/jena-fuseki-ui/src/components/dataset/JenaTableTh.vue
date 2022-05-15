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
