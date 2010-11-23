<%@ page import="org.openjena.fuseki.mgt.*"%>
<%@ page contentType="text/html ; charset=UTF-8"%>
<%@ page isThreadSafe="true"%>
<html>
  <head>
    <title>Fuseki - A SPARQL 1.1 Server</title>
    <link rel="stylesheet" type="text/css" href="/fuseki.css" />
  </head>

  <body>
    <h1>Fuseki</h1>
    <% String ds = Functions.dataset(request) ; %>
     <ul>
      <li> <a href="sparql.jsp">SPARQL</a></li>
      <li> <a href="upload.jsp">File upload</a></li>
    </ul>
<!--
    <p>Actions on dataset <i><%= ds %></i></p>
    <ul>
      <li> <a href="query.jsp">Query</a></li>
      <li> <a href="update.jsp">Update</a></li>
      <li> <a href="upload.jsp">File upload</a></li>
    </ul>
-->
  </body>
</html>