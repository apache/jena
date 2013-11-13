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

      /** Return the sparql query URL for this dataset, if it has one, or null */
      queryURL: function() {
        var qurl = this.endpointOfType( "query" );
        return qurl ? sprintf( "%s%s/%s", this.baseURL(), this.name(), qurl ) : null;
      },

      /**
       * Get the given relative path from the server, and return a promise object which will
       * complete with the JSON object denoted by the path.
       */
      getJSON: function( path, data ) {
        // TODO: will need to know the dataset UUID
//        var url = this.managementURL();
//        if (url) {
//          return $.getJSON( sprintf( url, path, data ) );
//        }
//        else {
//          return new $.Deferred().rejectWith( this, [{unavailable: true}] );
//        }
      }
    } );

    return Dataset;
  }
);