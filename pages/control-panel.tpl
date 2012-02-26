<html>
  <head>
    <title>Fuseki - A SPARQL 1.1 Server</title>
    <link rel="stylesheet" type="text/css" href="/fuseki.css" />
  </head>

  <body>
    <h1>Fuseki Control Panel</h1>

#set( $datasets = $mgt.datasets($request) )

    <div class="moreindent">
    <form action="dataset" method="post">
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