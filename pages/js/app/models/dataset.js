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
      initialize: function( datasetDescription, baseURL ) {
        this.set( datasetDescription );
        this.set( {baseURL: baseURL} );
      },

      name: function() {
        return this.get( "ds.name" );
      },

      services: function() {
        return this.get( "ds.services" );
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