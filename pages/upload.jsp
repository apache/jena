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
    <h1>Fuseki SPARQL Update</h1>
    <%@ include file="header.inc" %>
    <% String ds = Functions.dataset(request) ; %>
    <div class="moreindent">
      <form action="<%= ds %>/upload" enctype="multipart/form-data" method="post">
        <p>File to upload: <input type="file" name="DATA" size="40"></p>
        <input type="submit" value="Send">
      </form>
    </div>
    <%@ include file="trailer.inc" %>
  </body>
</html>