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
    <b-row class="mt-4">
      <b-col>
        <h1>Fuseki Documentation</h1>
        <p><a href="http://jena.apache.org/documentation/fuseki2/">Apache Jena Fuseki</a></p>
        <h2>Standards</h2>
        <ul>
          <li><a href="http://www.w3.org/TR/sparql11-query/">SPARQL 1.1 Query</a></li>
          <li><a href="http://www.w3.org/TR/sparql11-update/">SPARQL 1.1 Update</a></li>
          <li><a href="http://www.w3.org/TR/sparql11-protocol/">SPARQL 1.1 Protocol</a></li>
          <li><a href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store HTTP Protocol</a></li>
        </ul>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
export default {
  name: 'Help'
}
</script>
