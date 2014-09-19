<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Fuseki - SPARQL</title>

    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/yasqe.min.css" rel="stylesheet">
    <link href="css/yasr.min.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>
  <body >
  
  
  
    <!-- Fixed navbar -->
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#"><img style="width: 22px; height: 22px;"src="imgs/jena.png"/> Fuseki</a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li class="active"><a href="#"><span class="glyphicon glyphicon-play"></span> SPARQL</a></li>
            <li ><a href="upload.tpl"><span class="glyphicon glyphicon-circle-arrow-up"></span> Upload Data</a></li>
            <li><a target="_blank" href="http://jena.apache.org/documentation/serving_data/index.html"><span class="glyphicon glyphicon-book"></span> Fuseki Documentation</a></li>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown"><span class="glyphicon glyphicon-info-sign"></span> Standards <span class="caret"></span></a>
              <ul class="dropdown-menu" role="menu">
			     <li> <a target="_blank" href="http://www.w3.org/TR/sparql11-query/">SPARQL 1.1 Query</a></li>
			      <li> <a target="_blank" href="http://www.w3.org/TR/sparql11-update/">SPARQL 1.1 Update</a></li>
			      <li> <a target="_blank" href="http://www.w3.org/TR/sparql11-protocol/">SPARQL 1.1 Protocol</a></li>
			      <li> <a target="_blank" href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Uniform HTTP Protocol for Managing RDF Graphs</a>
              </ul>
            </li>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown"><span class="glyphicon glyphicon-check"></span> Validators <span class="caret"></span></a>
              <ul class="dropdown-menu" role="menu">
			      <li><a href="query-validator.html">SPARQL query validator</a></li>
			      <li><a href="iri-validator.html">IRI validator</a></li>
              </ul>
            </li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div>

 <div class="container-fluid">
	<div class="sink">
		<div class="control form-horizontal">
			<label>Dataset<select class="form-control" style="width:auto"; id="datasetSelector">
		      		<option value="-">None</option>
		      </select></label>
		      <label>Named Graph<input style="width: 300px;" type="text" class="form-control" id="ngInput"></label>
		      <div style="display:none" id="datasetWarning" role="alert" class="alert alert-warning">No dataset selected <strong>and</strong> no named graph specified. Make sure you specify the named graph in your query</div>
		</div>
	</div>
	<div id="yasqe"></div>
	<div id="yasr"></div>

</div> <!-- /container -->
<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="js/jquery-1.11.1.min.js"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="js/bootstrap.min.js"></script>
<script src="js/yasqe.min.js"></script>
<script src="js/yasr.min.js"></script>
<script src="js/index.js"></script>	
<script type="text/javascript">

var dsQueryEndpoints = {};
var dsUpdateEndpoints = {};

#foreach ( $mapEntry in $mgt.datasetsQuery($request).entrySet() )
   dsQueryEndpoints["$mapEntry.key"] = "$mapEntry.value";
#end
#foreach ( $mapEntry in $mgt.datasetsUpdate($request).entrySet() )
   dsUpdateEndpoints["$mapEntry.key"] = "$mapEntry.value";
#end

</script>
  </body>
</html>
