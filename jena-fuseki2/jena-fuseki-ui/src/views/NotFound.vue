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
  <b-container fluid>
    <b-row class="mt-2">
      <b-col cols="12">
        <div class="jumbotron">
          <h1 class="display-4">404 Page Not Found</h1>
          <p class="lead">
            <b-btn variant="primary" href="/">
              <FontAwesomeIcon icon="arrow-left" /> Back to Home
            </b-btn>
          </p>
        </div>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import { library } from '@fortawesome/fontawesome-svg-core'
import { faArrowLeft } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faArrowLeft)

export default {
  name: 'NotFound',

  components: {
    FontAwesomeIcon
  }
}
</script>
