/**
 * Backbone model denoting the remote Fuseki server.
 */

define(
  function( require ) {
    "use strict";

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        sprintf = require( "sprintf" ),
        Dataset = require( "app/models/dataset" ),
        PageUtils = require( "app/util/page-utils" );

    var DATASETS_MANAGEMENT_PATH = "/$/datasets";

    /**
     * This model represents the core representation of the remote Fuseki
     * server. Individual datasets have their own model.
     */
    var FusekiServer = Backbone.Model.extend( {
      /** This initializer occurs when the module starts, not when the constructor is invoked */
      init: function( options ) {
        this._baseURL = this.currentRootPath();
        this._managementURL = null;
        this.set( "selectedDatasetName", PageUtils.queryParam( "ds" ) )
      },

      baseURL: function() {
        return this._baseURL;
      },

      /** Return the URL from which we extract the details of the current server */
      serverDetailsURL: function() {
        return sprintf( "%s/$/server", this.baseURL() );
      },

      /** Return the URL for issuing commands to the management API, or null if no API defined */
      managementURL: function() {
        return this._managementURL;
      },

      /** Return the URL for getting the stats for all datasets */
      statsURL: function() {
        return sprintf( "%s/$/stats", this.managementURL() );
      },

      /** Return the list of datasets that this server knows about. Each dataset will be a Dataset model object */
      datasets: function() {
        return this.get( "datasets" );
      },

      /** Return the dataset with the given name */
      dataset: function( dsName ) {
        return _.find( this.datasets(), function( ds ) {return dsName === ds.name();} )
      },

      /** Return the name of the currently selected dataset, if known */
      selectedDatasetName: function() {
        return this.get( "selectedDatasetName" );
      },

      /** Return the dataset that is currently selected, or null */
      selectedDataset: function() {
        var dsName = this.selectedDatasetName();
        return dsName && this.dataset( dsName );
      },

      /** Load and cache the remote server description. Trigger change event when done */
      loadServerDescription: function() {
          var self = this;
          return this.getJSON( this.serverDetailsURL() )
              .done( function( data ) {
                  self.saveServerDescription( data );
              } )
              .then( function() {
                  fui.vent.trigger( "models.fuseki-server.ready" );
              });
      },

      /** Store the server description in this model */
      saveServerDescription: function( serverDesc ) {
        // wrap each dataset JSON description as a dataset model
        var bURL = this.baseURL();
        var mgmtURL = bURL;

        if (serverDesc.admin) {
	       // This is too simple.  window.location.port may be empty and matches protocol.
	       //mgmtURL = bURL.replace( ":" + window.location.port, ":" + serverDesc.admin.port );
           //console.log("managementURL -- s/"+window.location.port+"/"+serverDesc.admin.port+"/") ;
	       var path = window.location.pathname.replace( /\/[^/]*$/, "" ) ;
 	       mgmtURL = sprintf( "%s//%s:%s%s",  window.location.protocol, window.location.hostname, serverDesc.admin, path );
        }
	    this._managementURL = mgmtURL ;
	
        var datasets = _.map( serverDesc.datasets, function( d ) {
          return new Dataset( d, bURL, mgmtURL + DATASETS_MANAGEMENT_PATH );
        } );

        datasets.sort( function( ds0, ds1 ) {
          if (ds0.name() > ds1.name()) {
            return 1;
          }
          else if (ds0.name() < ds1.name()) {
            return -1;
          }
          else
            return 0;
        } );

        this.set( {
          serverDescription: serverDesc,
          datasets: datasets,
          ready: true
        } );
      },

      /**
       * Get the given relative path from the server, and return a promise object which will
       * complete with the JSON object denoted by the path.
       */
      getJSON: function( path, data ) {
        return $.getJSON( path, data );
      },

      /** Update or create a dataset by posting to its endpoint */
      updateOrCreateDataset: function( datasetId, data ) {
        var url = sprintf( "%s/$/datasets%s", this.managementURL(),
                           datasetId ? ("/" + datasetId) : ""
                         );

        return $.ajax( url,
                       { data: data,
                         method: "post"
                       }
                     );
      },

      /** Extract the server root path from the current window href */
      currentRootPath: function() {
        var path = window.location.pathname.replace( /\/[^/]*$/, "" );

		/*
		console.log("window.location="+window.location) ;
		console.log("window.location.href="+window.location.href) ;
		console.log("window.location.protocol="+window.location.protocol) ;
		console.log("window.location.host="+window.location.host) ;
		console.log("window.location.hostname="+window.location.hostname) ;
		console.log("window.location.port="+window.location.port) ;
		console.log("window.location.pathname="+window.location.pathname) ;
		console.log("window.location.origin="+window.location.origin) ;
		console.log("window.location.hash="+window.location.hash) ;
		console.log("window.location.search="+window.location.search) ;
	    console.log("path='"+path+"'") ;
		*/
	
		var path2 ;
		var port = window.location.port ;
		//console.log("port='"+port+"'") ;
		if ( !port || 0 === port.length ) {
		    // No explicit port.
		    path2 = sprintf( "%s//%s%s",  window.location.protocol, window.location.hostname, path ) ;
		} else {
		    path2 = sprintf( "%s//%s:%s%s",  window.location.protocol, window.location.hostname, window.location.port, path );
		}
	    //console.log("path2='"+path2+"'") ;
		return path2 ;
      }
    } );

    // when the models module starts, automatically load the server description
    fui.models.addInitializer( function( options ) {
      var fusekiServer = new FusekiServer();
      fui.models.fusekiServer = fusekiServer;

      fusekiServer.init( options );
      fusekiServer.loadServerDescription();
    } );

    return FusekiServer;
  }
);
