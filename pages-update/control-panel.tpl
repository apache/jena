<html>
  <head>
    <title>Fuseki - A SPARQL 1.1 Server</title>
    <link rel="stylesheet" type="text/css" href="/fuseki.css" />
  </head>

  <body>
    <h1>Fuseki Control Panel</h1>

    <div class="moreindent">
    <form action="dataset" method="post">
      Dataset: <select name="dataset">
        ${mgt.datasetsAsSelectOptions(request)}
      <div>
        <input type="submit" value="Select">
      </div>
    </form>
    </div>
  </body>
</html>