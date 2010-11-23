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

    <div class="moreindent">
      <form action="<%= ds%>/query" method="get">
        <textarea name="query" cols="70" rows="30"></textarea>
        <br/>
        Output XML: <input type="radio" name="output" value="xml" checked/>
	    with XSLT style sheet (leave blank for none): 
	    <input name="stylesheet" size="25" value="/xml-to-html.xsl" /> <br/>
	    or JSON output: <input type="radio" name="output" value="json"/> <br/>
	    or text output: <input type="radio" name="output" value="text"/> <br/>
	    or CSV output: <input type="radio" name="output" value="csv"/> <br/>
	    or TSV output: <input type="radio" name="output" value="tsv"/> <br/>
        Force the accept header to <tt>text/plain</tt> regardless 
 	    <input type="checkbox" name="force-accept" value="text/plain"/>	  <br/>
	    <input type="submit" value="Get Results" />
      </form>
    </div>
    <%@ include file="trailer.inc" %>
  </body>
</html>   

