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
        this._baseURL = sprintf( "http://%s:%s", window.location.hostname, window.location.port );
        this._managementURL = null;
      },

      baseURL: function() {
        return this._baseURL;
      },

      /** Return the URL for issuing commands to the management API, or null if no API defined */
      managementURL: function() {
        return this._managementURL;
      },

      /** Return the URL for getting the stats for a named dataset */
      statsURL: function( dsName ) {
        return sprintf( "%s/$/stats%s", this.managementURL(), dsName ? dsName : "" );
      },

      /** Return the list of datasets that this server knows about. Each dataset will be a Dataset model object */
      datasets: function() {
        return this.get( "datasets" );
      },

      /** Return the dataset with the given name */
      dataset: function( dsName ) {
        return _.find( this.datasets(), function( ds ) {return dsName === ds.name();} )
      },

      /** Load and cache the remote server description. Trigger change event when done */
      loadServerDescription: function() {
        var self = this;
        return this.getJSON( "/$/status" ).done( function( data ) {
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
          mgmtURL = bURL.replace( ":" + window.location.port, ":" + serverDesc.admin.port );
          this._managementURL = mgmtURL;
        }

        var datasets = _.map( serverDesc.datasets, function( d ) {
          return new Dataset( d, bURL, mgmtURL );
        } );

        this.set( {server: serverDesc.server, datasets: datasets} );
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