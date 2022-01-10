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
  <b-navbar toggleable="sm" variant="light" type="light" class="col-12">
    <b-navbar-toggle target="nav-collapse"></b-navbar-toggle>
    <b-collapse id="nav-collapse" is-nav>
      <b-navbar-brand>
        <router-link to="/">
          <img alt="Apache Jena logo" width="52px" src="../assets/logo.svg"> Apache Jena Fuseki
        </router-link>
      </b-navbar-brand>
      <b-navbar-nav>
        <b-nav-item to="/" exact-active-class="active">
          <FontAwesomeIcon icon="database" /> datasets
        </b-nav-item>
        <b-nav-item to="/manage" active-class="active">
          <FontAwesomeIcon icon="cogs" /> manage
        </b-nav-item>
        <b-nav-item to="/documentation" active-class="active">
          <FontAwesomeIcon icon="info-circle" /> help
        </b-nav-item>
      </b-navbar-nav>
      <b-navbar-nav class="ml-auto">
        <ServerStatus />
      </b-navbar-nav>
    </b-collapse>
  </b-navbar>
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
