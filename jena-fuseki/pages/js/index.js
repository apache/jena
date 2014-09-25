var datasetEl = $("#datasetSelector");

var testLocalStorage = function(){
    var test = 'test';
    try {
        localStorage.setItem(test, test);
        localStorage.removeItem(test);
        return true;
    } catch(e) {
        return false;
    }
}
var storeDsInLs = function() {
	localStorage.setItem("selectedDs", datasetEl.val());
}
var localStorageSupported = testLocalStorage();

var populateDatasetSelect = function() {
	var selectFirstDs = true;
	var storedSelectedDs = (localStorageSupported && localStorage.getItem("selectedDs"));
	if (storedSelectedDs && (storedSelectedDs in dsQueryEndpoints || storedSelectedDs == "-")) {
		selectFirstDs = false;
	}
	for (var ds in dsQueryEndpoints) {
		$("<option value='" + ds + "'>" + ds + "</option>").prop("selected", (selectFirstDs || ds == storedSelectedDs)).appendTo(datasetEl);
		selectFirstDs = false;
	}
}
var yasqe;
$(document).ready(function() {
	
	populateDatasetSelect();
	
	yasqe = YASQE(document.getElementById("yasqe"), {
		sparql: {
			endpoint: "sparql",
			showQueryButton: true,
		},
		createShareLink: null,
	});
	var yasr = YASR(document.getElementById("yasr"), {
		//this way, the URLs in the results are prettified using the defined prefixes in the query
		getUsedPrefixes: yasqe.getPrefixesFromQuery
	});
	 
	/**
	* Set some of the hooks to link YASR and YASQE
	*/
	yasqe.options.sparql.handlers.success =  function(data, textStatus, xhr) {
		yasr.setResponse({response: data, contentType: xhr.getResponseHeader("Content-Type")});
	};
	yasqe.options.sparql.handlers.error = function(xhr, textStatus, errorThrown) {
		var exceptionMsg = textStatus + " (#" + xhr.status + ")";
		if (errorThrown && errorThrown.length) exceptionMsg += ": " + errorThrown;
		yasr.setResponse({exception: exceptionMsg});
	};
	
	
	var updateDs = function() {
	    var getEndpoint = function() {
	    	
	    	var dsEndpoints = (yasqe.getQueryMode() == "update"? dsUpdateEndpoints: dsQueryEndpoints);
	    	var endpoint = "sparql"
	    	if (dataset != "-") {
	    		endpoint = dataset + "/" + dsEndpoints[dataset];
	    	}
	    	return endpoint;
	    };
	    
		var ng = ngEl.val();
		ng = (ng? ng.trim(): "");
		var dataset = datasetEl.val();
		
		/**
		* update warning
		**/
		if (ng == "" && dataset == "-") {
			dsWarning.show(400);
		} else {
			dsWarning.hide(400);
		}
		
		/**
		* set default graph
		**/
		var defaultGraphs = [];
		if (ng.length) defaultGraphs.push(ng);
		yasqe.options.sparql.defaultGraphs = defaultGraphs;
		
		/**
		* set endpoint
		**/
		yasqe.options.sparql.endpoint = getEndpoint();
	}
	datasetEl.change(updateDs).change(storeDsInLs);
	var ngEl = $("#ngInput").on("keyup", updateDs);
	yasqe.on("change", updateDs);//query type may change from select to inserts or vice versa
	var dsWarning = $("#datasetWarning");
	
	
	//finally, make sure we initialize all values (warning msg, endpoint and named graphs)
	updateDs();
});
