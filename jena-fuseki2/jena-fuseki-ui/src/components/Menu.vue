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
  <nav class="navbar navbar-expand-md bg-light">
    <div class="container-fluid mx-2 mx-md-0">
      <router-link to="/" class="navbar-brand">
        <img alt="Apache Jena logo" width="52px" src="../assets/logo.svg"> Apache Jena Fuseki
      </router-link>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav">
          <li class="nav-item">
            <router-link to="/" class="nav-link" exact-active-class="active">
              <FontAwesomeIcon icon="database" /> datasets
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/manage" class="nav-link" exact-active-class="active">
              <FontAwesomeIcon icon="cogs" /> manage
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/documentation" class="nav-link" exact-active-class="active">
              <FontAwesomeIcon icon="info-circle" /> help
            </router-link>
          </li>
        </ul>
        <ul class="navbar-nav ms-auto">
          <ServerStatus />
        </ul>
      </div>
    </div>
  </nav>
</template>

<script>
import { library } from '@fortawesome/fontawesome-svg-core'
import { faDatabase, faCogs, faInfoCircle } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import ServerStatus from '@/components/ServerStatus'

library.add(faDatabase, faCogs, faInfoCircle)

export default {
  name: 'Menu',

  components: {
    FontAwesomeIcon,
    ServerStatus
  }
}
</script>
