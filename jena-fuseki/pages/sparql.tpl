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
    <title>Fuseki</title>
    <link rel="stylesheet" type="text/css" href="fuseki.css" />
  </head>
  <body>
#set( $ds = $mgt.dataset($request, "") )
#set( $srvQuery = $mgt.serviceQuery($ds) )
#set( $srvUpdate = $mgt.serviceUpdate($ds) )
#set( $srvUpload= $mgt.serviceUpload($ds) )
#set( $srvGraphR = $mgt.serviceGraphRead($ds) )
#set( $srvGraphRW = $mgt.serviceGraphReadWrite($ds) )

<!-- error case ... -->
<!-- Debug
<ul>
  <li>${ds}</li>
  <li>$srvQuery</li>
  <li>$srvUpdate</li>
  <li>$srvUpload</li>
  <li>$srvGraphR</li>
  <li>$srvGraphRW</li>
</ul>
-->

    <h1>Fuseki Query</h1>
    Dataset: ${ds}
    <hr/>

    <p><b>SPARQL Query</b></p>
    <div class="moreindent">
      <form action="${ds}/${srvQuery}" method="GET"  accept-charset="UTF-8">
        <textarea name="query" cols="70" rows="10"></textarea>
        <br/>

        Output: <select name="output">
          <option value="text">Text</option>
          <option value="json">JSON</option>
          <option value="xml">XML</option>
          <option value="csv">CSV</option>
          <option value="tsv">TSV</option>
        </select>
        <br/>
	    XSLT style sheet (blank for none): 
        <input name="stylesheet" size="20" value="/xml-to-html.xsl" />
        <br/>
        <input type="checkbox" name="force-accept" value="text/plain"/>
        Force the accept header to <tt>text/plain</tt> regardless.
	    <br/>
	    <input type="submit" value="Get Results" />
      </form>
    </div>
    <hr/>

    <p><b>SPARQL Update</b></p>
    <div class="moreindent">
      <form action="${ds}/${srvUpdate}" method="post" accept-charset="UTF-8">
        <textarea name="update" cols="70" rows="10"></textarea>
	    <br/>
        <input type="submit" value="Perform update" />
      </form>
    </div>
    <hr/>
    <p><b>File upload</b></p>
    <div class="moreindent">
      <form action="${ds}/${srvUpload}" enctype="multipart/form-data" method="post">
        File: <input type="file" name="UNSET FILE NAME" size="40" multiple=""><br/>
        Graph: <input name="graph" size="20" value="default"/><br/>
        <input type="submit" value="Upload">
      </form>
    </div>
    <hr/>
      </body>
</html>   
