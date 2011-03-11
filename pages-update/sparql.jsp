<%@ page import="org.openjena.fuseki.mgt.*"%>
<%@ page import="java.util.*"%>
<%@ page contentType="text/html ; charset=UTF-8"%>
<%@ page isThreadSafe="true"%>

<html>
  <head>
    <title>Fuseki</title>
    <link rel="stylesheet" type="text/css" href="fuseki.css" />
  </head>
  <body>
    <h1>Fuseki Query</h1>
    Dataset: <%= Functions.dataset(request, "No Session") %>
    <hr/>

    <% String ds = Functions.dataset(request) ; %>

    <p><b>SPARQL Query</b></p>
    <div class="moreindent">
      <form action="<%= ds%>/query" method="get">
        <textarea  style="background-color: #F0F0F0;" name="query" cols="70" rows="10"></textarea>
        <br/>

        Output: <select name="output">
          <option value="xml">XML</option>
          <option value="json">JSON</option>
          <option value="text">Text</option>
          <option value="csv">CSV</option>
          <option value="tsv">TSV</option>
        </select>
        <br/>
	    XSLT style sheet (blank for none): 
        <input name="stylesheet" size="20" value="/xml-to-html.xsl" />
        <br/>
        <input type="checkbox" name="force-accept" value="text/plain"/>
        Force the accept header to <tt>text/plain</tt> regardless 
	    <br/>
	    <input type="submit" value="Get Results" />
      </form>
    </div>
    <hr/>

    <p><b>SPARQL Update</b></p>
    <div class="moreindent">
      <form action="<%= ds %>/update" method="post">
        <textarea style="background-color: #F0F0F0;" name="update" cols="70" rows="10"></textarea>
	    <br/>
        <input type="submit" value="Perform update" />
      </form>
    </div>
    <hr/>
    <p><b>File upload</b></p>
    <div class="moreindent">
      <form action="<%= ds %>/upload" enctype="multipart/form-data" method="post">
        File: <input type="file" name="UNSET FILE NAME" size="40"><br/>
        Graph: <input name="graph" size="20" value="default"/><br/>
        <input type="submit" value="Upload">
      </form>
    </div>
    <hr/>
      </body>
</html>   

