/**
 * Controller for the embedded Qonsole component.
 *
 * Note: unlike some Qonsole installations, the endpoint URL selector dropdown
 * has been removed in favour of the dataset selector control higher up the page.
 **/

define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        qonsole = require( "qonsole" ),
        pageUtils = require( "app/util/page-utils" );

    var QueryController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( QueryController.prototype, {

      initEvents: function() {
        _.bindAll( this, "onServerModelReady", "onDatasetChanged" );

        if (fui.models.fusekiServer && fui.models.fusekiServer.get( "ready" )) {
          this.onServerModelReady();
        }
        else {
          fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
        }

        fui.vent.on( "dataset.changed", this.onDatasetChanged );
      },

      /** Initialise the qonsole component */
      initQonsole: function( datasetsConfig ) {
        var qonfig = require( "app/qonsole-config" );
        qonsole.init( qonfig );

        var dsName = fui.models.fusekiServer.selectedDatasetName();
        if (dsName) {
          this.setEndpointURL( dsName );
        }
      },

      /** When the fuseki server is ready, we can init the qonsole */
      onServerModelReady: function( event ) {
        var fusekiServer = fui.models.fusekiServer;
        var endpoints = {};
        var datasets = fusekiServer.datasets();

        this.initQonsole( {} );
      },

      /** When notified that the selected dataset name has changed, update the endpoint URL */
      onDatasetChanged: function( dsName ) {
        this.setEndpointURL( dsName );
      },

      /** Set the endpoint URL based on the selected dataset name */
      setEndpointURL: function( dsName ) {
        var dataset = fui.models.fusekiServer.dataset( dsName );
        qonsole.setCurrentEndpoint( dataset.queryURL() );
      }

    } );

    return QueryController;
  }
);
