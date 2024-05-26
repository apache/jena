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
        <h2>Tasks</h2>
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
                    :items="tasksReversed"
                    :is-busy="isBusy"
                    placeholder="Filter tasks"
                    empty-text="No tasks created"
                    empty-filtered-text="No tasks found"
                  />
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
import Menu from '@/components/manage/Menu.vue'
import TableListing from '@/components/dataset/TableListing.vue'

export default {
  name: 'ManageTasks',

  data () {
    return {
      polling: null,
      tasks: [],
      isBusy: true,
      fields: [
        {
          key: 'task',
          label: 'task',
          sortable: true,
          sortDirection: 'asc'
        },
        {
          key: 'taskId',
          label: 'task ID',
          sortable: true,
          sortDirection: 'asc'
        },
        {
          key: 'started',
          label: 'started',
          sortable: true,
          sortDirection: 'asc'
        },
        {
          key: 'finished',
          label: 'finished',
          sortable: true,
          sortDirection: 'asc'
        }
      ]
    }
  },

  computed: {
    tasksReversed () {
      if (!this.tasks) {
        return []
      }
      return [...this.tasks].reverse()
    }
  },

  components: {
    Menu,
    'table-listing': TableListing
  },

  async beforeRouteEnter (to, from, next) {
    next(vm => {
      vm.$fusekiService
        .getTasks()
        .then(response => {
          vm.tasks = response.data
          vm.isBusy = null
        })
    })
  },

  mounted () {
    const vm = this
    this.polling = setInterval(async () => {
      vm.isBusy = true
      vm.$fusekiService
        .getTasks()
        .then(response => {
          vm.tasks = response.data
          vm.isBusy = null
        })
    }, 10000)
  },

  beforeRouteLeave (to, from, next) {
    if (this.polling) {
      clearInterval(this.polling)
    }
    next()
  }
}
</script>
