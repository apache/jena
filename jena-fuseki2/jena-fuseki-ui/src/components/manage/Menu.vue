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
  <ul class="nav nav-tabs card-header-tabs">
    <li class="nav-item">
      <router-link :to="`/manage`" class="nav-link" exact-active-class="active">
        <FontAwesomeIcon icon="database" />
        <span class="ms-1">existing datasets</span>
      </router-link>
    </li>
    <li class="nav-item">
      <router-link :to="`/manage/new`" class="nav-link" exact-active-class="active">
        <FontAwesomeIcon icon="plus-circle" />
        <span class="ms-1">new dataset</span>
      </router-link>
    </li>
    <li class="nav-item">
      <router-link :to="`/manage/tasks`" class="nav-link" exact-active-class="active">
        <FontAwesomeIcon icon="tasks" />
        <span class="ms-1">tasks</span>
      </router-link>
    </li>
  </ul>
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
