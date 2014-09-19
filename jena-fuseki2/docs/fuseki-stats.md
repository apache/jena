# Fuseki Service Statistics

A Fuseki server keeps detailed statistics for each dataset and each service
of a dataset keeps counters as to the number
of incoming requests, number of successful requests, number of bad requests
(i.e client errors), and number of failing requests (i.e. server errors).
Statistics are returned as JSON. They are also available via JMX.

## Structure of the Statistics Report

### SPARQL Protocol Service 

Query and Update

### SPARQL Graph Store Protocol

inc extensions - dataset HTTP verbs.

#### Other services.

File upload.

## Example

	{ "datasets" : {
		 "/ds" :
		     { "Requests" : 0 ,
	           "RequestsBad" : 0 ,
	           "RequestsGood" : 0 ,
	           "services" : {
 	              "query" :
	                 {"QueryExecErrors" : 0 ,
	                  "QueryTimeouts" : 0 ,
	                  "Requests" : 0 ,
	                  "RequestsBad" : 0 ,
	                  "RequestsGood" : 0 ,
	                  "endpoints" : [ 
	                      "query" ,
	                      "sparql"
	                    ]
	                } ,
	              "update" :
	                 {"Requests" : 0 ,
	                  "RequestsBad" : 0 ,
	                  "RequestsGood" : 0 ,
	                  "UpdateExecErrors" : 0 ,
	                  "endpoints" : [ ]
	                } ,
	              "upload" :
	                 {"Requests" : 0 ,
	                  "RequestsBad" : 0 ,
	                  "RequestsGood" : 0 ,
	                  "endpoints" : [ ]
	                } ,

	             "gspRead" :
	                 {"GSPdelete" : 0 ,
	                  "GSPdeleteBad" : 0 ,
	                  "GSPdeleteGood" : 0 ,
	                  "GSPget" : 0 ,
	                  "GSPgetBad" : 0 ,
	                  "GSPgetGood" : 0 ,
	                  "GSPhead" : 0 ,
	                  "GSPheadBad" : 0 ,
	                  "GSPheadGood" : 0 ,
	                  "GSPoptions" : 0 ,
	                  "GSPoptionsBad" : 0 ,
	                  "GSPoptionsGood" : 0 ,
	                  "GSPpatch" : 0 ,
	                  "GSPpatchBad" : 0 ,
	                  "GSPpatchGood" : 0 ,
	                  "GSPpost" : 0 ,
	                  "GSPpostBad" : 0 ,
	                  "GSPpostGood" : 0 ,
	                  "GSPput" : 0 ,
	                  "GSPputBad" : 0 ,
	                  "GSPputGood" : 0 ,
	                  "Requests" : 0 ,
	                  "RequestsBad" : 0 ,
	                  "RequestsGood" : 0 ,
	                  "endpoints" : [ "data" ]
	                } ,
	              "gspReadWrite" :
	                 {"GSPdelete" : 0 ,
	                  "GSPdeleteBad" : 0 ,
	                  "GSPdeleteGood" : 0 ,
	                  "GSPget" : 0 ,
	                  "GSPgetBad" : 0 ,
	                  "GSPgetGood" : 0 ,
	                  "GSPhead" : 0 ,
	                  "GSPheadBad" : 0 ,
	                  "GSPheadGood" : 0 ,
	                  "GSPoptions" : 0 ,
	                  "GSPoptionsBad" : 0 ,
	                  "GSPoptionsGood" : 0 ,
	                  "GSPpatch" : 0 ,
	                  "GSPpatchBad" : 0 ,
	                  "GSPpatchGood" : 0 ,
	                  "GSPpost" : 0 ,
	                  "GSPpostBad" : 0 ,
	                  "GSPpostGood" : 0 ,
	                  "GSPput" : 0 ,
	                  "GSPputBad" : 0 ,
	                  "GSPputGood" : 0 ,
	                  "Requests" : 0 ,
	                  "RequestsBad" : 0 ,
	                  "RequestsGood" : 0 ,
	                  "endpoints" : [ ]
	                }
	            }
	        }
	    }
	}
