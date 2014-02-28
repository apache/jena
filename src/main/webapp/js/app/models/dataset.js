/**
 * Backbone model denoting the remote Fuseki server.
 */
define(
  function( require ) {
    "use strict";

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        sprintf = require( "sprintf" );

    /**
     * This model represents the core representation of the remote Fuseki
     * server. Individual datasets have their own model.
     */
    var Dataset = Backbone.Model.extend( {
      initialize: function( datasetDescription, baseURL, mgmtURL ) {
        this.set( datasetDescription );
        this.set( {baseURL: baseURL, mgmtURL: mgmtURL} );
      },

      baseURL: function() {
        return this.get( "baseURL" );
      },

      mgmtURL: function() {
        return this.get( "mgmtURL" );
      },

      name: function() {
        return this.get( "ds.name" );
      },

      services: function() {
        return this.get( "ds.services" );
      },

      serviceTypes: function() {
        return _.map( this.services(), function( s ) {return s["srv.type"];} );
      },

      /** Return the first service that has the given type */
      serviceOfType: function( serviceType ) {
        return _.find( this.services(), function( s ) {
          return s["srv.type"] === serviceType;
        } );
      },

      /** Return the first endpoint of the first service that has the given type */
      endpointOfType: function( serviceType ) {
        var service = this.serviceOfType( serviceType );
        return service && _.first( service["srv.endpoints"] );
      },

      /* Return URL for a service of a given type or null, if no such service */
	    endpointURL: function( serviceType ) {
        var url = this.endpointOfType( serviceType );
        return url ? sprintf( "%s%s/%s", this.baseURL(), this.name(), url ) : null;
      } ,

      /** Return the sparql query URL for this dataset, if it has one, or null */
      queryURL: function() {
        return this.endpointURL( "query" ) ;
      } ,

      /** Return the sparql update URL for this dataset, if it has one, or null */
      updateURL: function() {
        return this.endpointURL( "update" ) ;
      } ,

      /** Return the sparql upload URL for this dataset, if it has one, or null */
      uploadURL: function() {
        return this.endpointURL( "upload" ) ;
      }

    } );

    return Dataset;
  }
);