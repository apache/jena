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
      <form action="<%= ds %>/update" method="post">
        <textarea style="background-color: #F0F0F0;" name="request" cols="70" rows="20"></textarea>
        <input type="submit" value="Perform update" />
      </form>
    </div>
  </body>
</html>   

