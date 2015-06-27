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
<html>
  <head>
    <title>Fuseki - A SPARQL 1.1 Server</title>
    <link rel="stylesheet" type="text/css" href="fuseki.css" />
  </head>

  <body>
    <h1>Fuseki Control Panel</h1>

#set( $datasets = $mgt.datasets($request) )
#set( $action   = $mgt.actionDataset($request) )

    <div class="moreindent">
    <form action="${action}" method="post">
      Dataset: <select name="dataset">
#foreach($ds in $datasets)
        <option value="${ds}">${ds}</option>
#end
      <div>
        <input type="submit" value="Select">
      </div>
    </form>
    </div>
  </body>
</html>