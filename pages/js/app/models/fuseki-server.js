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
        sprintf = require( "sprintf" ),
        Dataset = require( "models/dataset" );

    /**
     * This model represents the core representation of the remote Fuseki
     * server. Individual datasets have their own model.
     */
    var FusekiServer = Backbone.Model.extend( {
      /** This initializer occurs when the module starts, not when the constructor is invoked */
      init: function( options ) {
        if (options.managementPort) {
          this._managementURL = sprintf( "http://%s:%s%s%%s", window.location.hostname,
                                         options.managementPort, window.location.pathname );
        }
        else {
          this._managementURL = null;
        }
      },

      /** Return the URL pattern for issuing commands to the management API, or null if no API defined */
      managementURL: function() {
        return this._managementURL;
      },

      /** Load and cache the remote server description. Trigger change event when done */
      loadServerDescription: function() {
        return this.getJSON( "datasets" ).then( this.saveServerDescription );
      },

      /** Store the server description in this model */
      saveServerDescription: function( serverDesc ) {
        // wrap each dataset JSON description as a dataset model
        var baseURL = sprintf( this.managementURL(), "datasets" );
        var datasets = _.map( serverDesc.datasets, function( d ) {
          return new Dataset( d, baseURL );
        } );

        self.set( {server: serverDesc.server, datasets: datasets} );
      },

      /**
       * Get the given relative path from the server, and return a promise object which will
       * complete with the JSON object denoted by the path.
       */
      getJSON: function( path, data ) {
        var url = this.managementURL();
        if (url) {
          return $.getJSON( sprintf( url, path ), data );
        }
        else {
          return new $.Deferred().rejectWith( this, [{unavailable: true}] );
        }
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