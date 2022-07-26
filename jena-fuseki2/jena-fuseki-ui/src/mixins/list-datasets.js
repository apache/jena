/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { BUS } from '@/events'

export default {
  data () {
    return {
      serverData: null,
      fields: [
        {
          key: 'name',
          label: 'name',
          sortable: true,
          sortDirection: 'asc'
        },
        'actions'
      ]
    }
  },

  computed: {
    /**
     * Table items, computed from the server data datasets.
     *
     * @returns {*[]|*}
     */
    items () {
      if (!this.serverData) {
        return []
      }
      return this
        .serverData.datasets
        .map(dataset => {
          return {
            name: dataset['ds.name']
          }
        })
        .sort((a, b) => {
          return a.name.localeCompare(b.name)
        })
    },
    isBusy () {
      if (this.serverData === null) {
        return true
      }
      // N.B.: For Vue-3, it's better to return a `null` value so the value is
      // not set, as otherwise Vue-3 raises a warning about false still
      // setting the value to the component.
      return null
    }
  },

  created () {
    let route = this.$route.matched[0]
    if (!route)
      return;
    route.beforeRouteEnter = this.beforeRouteEnter
  },

  methods: {
    initializeData () {
      const vm = this
      return vm.$fusekiService
        .getServerData()
        .then(serverData => {
          vm.serverData = serverData
        })
    }
  }
}
