/** Controller for the main index.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        qonsole = require( "lib/qonsole" );

    var ValidationController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( ValidationController.prototype, {
      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      /** When the fuseki server is ready, we can init the qonsole */
      onServerModelReady: function( event ) {
        // when ready, initialise the qonsole component
        var datasets = fui.models.fusekiServer.datasets();
        var endpoints = {};
        _.each( datasets, function( ds ) {
          var queryURL = ds.queryURL();
          if (queryURL) {
            endpoints[ds.name()] = queryURL;

            if (!endpoints["default"]) {
              endpoints["default"] = queryURL;
            }
          }
        } );

        var qonfig = require( "qonsole-config" );
        qonfig.endpoints = endpoints;
        qonsole.init( qonfig );
      }

    } );

    return ValidationController;
  }
);
