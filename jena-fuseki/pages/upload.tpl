<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Fuseki - Upload</title>

    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>
  <body>
  
  
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
            <li><a href="./"><span class="glyphicon glyphicon-play"></span> SPARQL</a></li>
            <li class="active" ><a href="#"><span class="glyphicon glyphicon-circle-arrow-up"></span> Upload Data</a></li>
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
			      <li><a href="update-validator.html">SPARQL update validator</a></li>
			      <li><a href="iri-validator.html">IRI validator</a></li>
              </ul>
            </li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div>
    
    
<div class="container"

	<div class="row">
		<div class="col-md-12">
			<form role="form" class="form-horizontal"  id="uploadForm" action='' enctype="multipart/form-data" method="post">
				<div class="form-group">
					<label class="col-sm-2 control-label" for="datasetSelector">Dataset</label>
					<div class="col-sm-10">
						<select class="form-control" style="width:auto;" id="datasetSelector">
								#foreach($ds in $datasets)
								<option value="${ds}">${ds}</option>
							#end
				      </select>
				   </div>
				</div>
				<div class="form-group">
					<label class="col-sm-2 control-label" for="fileInput">File</label>
					<div class="col-sm-10">
						<input id="fileInput" class="form-control" type="file" name="UNSET FILE NAME" size="40" multiple="">
					</div>
				</div>
				<div class="form-group">
					<label class="col-sm-2 control-label" for="nGraph">Graph</label>
					<div class="col-sm-10">
						<input id="nGraph" class="form-control" name="graph" size="40" value="default"/>
					</div>
				</div>
				<div class="form-group">
					<div class="col-sm-10 col-sm-offset-2">
						<input class='btn btn-primary' type="submit" value="Upload">
					</div>
				</div>
			</form>		
		</div>
	
	</div>
</div>

		        
		        
		        
    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/bootstrap.min.js"></script>
    
    <script type="text/javascript">
    	var datasetEl =  $("#datasetSelector");
    	var uploadLocations = {};

		#foreach ( $mapEntry in $mgt.serviceUploads().entrySet() )
		   uploadLocations["$mapEntry.key"] = "$mapEntry.value";
		#end
		
		for (var ds in uploadLocations) {
			$("<option value=" + ds + ">" + ds + "</option>").appendTo(datasetEl);
		}
		$("#uploadForm").submit(function() {
			$(this).attr('action', datasetEl.val() + "/" + uploadLocations[datasetEl.val()]); 
		});
	
    
    </script>
  </body>
</html>