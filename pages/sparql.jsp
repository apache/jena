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
    <%@ include file="header.inc" %>
    <% String ds = Functions.dataset(request) ; %>

    <p><b>SPARQL Query</b></p>
    <div class="moreindent">
      <form action="<%= ds%>/query" method="get">
        <textarea  style="background-color: #F0F0F0;" name="query" cols="70" rows="10"></textarea>
        <br/>
        Output XML: <input type="radio" name="output" value="xml" checked/>
	    with XSLT style sheet (leave blank for none): 
	    <input name="stylesheet" size="25" value="/xml-to-html.xsl" /> <br/>
	    JSON output: <input type="radio" name="output" value="json"/>
	    Text output: <input type="radio" name="output" value="text"/>
	    CSV output: <input type="radio" name="output" value="csv"/>
	    TSV output: <input type="radio" name="output" value="tsv"/><br/>
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
        <textarea style="background-color: #F0F0F0;" name="request" cols="70" rows="10"></textarea>
	    <br/>
        <input type="submit" value="Perform update" />
      </form>
    </div>
    <hr/>
    <p><b>File upload</b></p>
    <div class="moreindent">
      <form action="<%= ds %>/upload" enctype="multipart/form-data" method="post">
        File to upload: <input type="file" name="DATA" size="40">
        <input type="submit" value="Send">
      </form>
    </div>

    <%@ include file="trailer.inc" %>
  </body>
</html>   

