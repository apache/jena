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
      <router-link :to="`/dataset/${this.datasetName}/query`" class="nav-link" exact-active-class="active">
        <FontAwesomeIcon icon="question-circle" />
        <span class="ms-1">query</span>
      </router-link>
    </li>
    <li class="nav-item">
      <router-link :to="`/dataset/${this.datasetName}/upload`" class="nav-link" exact-active-class="active">
        <FontAwesomeIcon icon="upload" />
        <span class="ms-1">add data</span>
      </router-link>
    </li>
    <li class="nav-item">
      <router-link :to="`/dataset/${this.datasetName}/edit`" class="nav-link" exact-active-class="active">
        <FontAwesomeIcon icon="edit" />
        <span class="ms-1">edit</span>
      </router-link>
    </li>
    <li class="nav-item">
      <router-link :to="`/dataset/${this.datasetName}/info`" class="nav-link" exact-active-class="active">
        <FontAwesomeIcon icon="tachometer-alt" />
        <span class="ms-1">info</span>
      </router-link>
    </li>
  </ul>
</template>

<script>
import { library } from '@fortawesome/fontawesome-svg-core'
import { faQuestionCircle, faUpload, faTachometerAlt, faEdit } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faQuestionCircle, faUpload, faTachometerAlt, faEdit)

export default {
  name: 'DatasetMenu',

  props: {
    datasetName: {
      type: String,
      required: true
    }
  },

  components: {
    FontAwesomeIcon
  }
}
</script>
