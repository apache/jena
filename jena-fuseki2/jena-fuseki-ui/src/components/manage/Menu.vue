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
  <b-nav card-header tabs>
    <b-nav-item
      :to="`/manage`"
      exact-active-class="active"
    >
      <FontAwesomeIcon icon="database" />
      <span class="ml-1">existing datasets</span>
    </b-nav-item>
    <b-nav-item
      :to="`/manage/new`"
      exact-active-class="active"
    >
      <FontAwesomeIcon icon="plus-circle" />
      <span class="ml-1">new dataset</span>
    </b-nav-item>
    <b-nav-item
      :to="`/manage/tasks`"
      exact-active-class="active"
    >
      <FontAwesomeIcon icon="tasks" />
      <span class="ml-1">tasks</span>
    </b-nav-item>
  </b-nav>
</template>

<script>
import { library } from '@fortawesome/fontawesome-svg-core'
import { faDatabase, faPlusCircle, faTasks } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faDatabase, faPlusCircle, faTasks)

export default {
  name: 'ManageMenu',

  components: {
    FontAwesomeIcon
  }
}
</script>
