/** Controller for the admin/data-management.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        DataManagementView = require( "views/data-management" );

    var DataManagementController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( DataManagementController.prototype, {
      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      /** When the fuseki server is ready, we can list the initial datasets */
      onServerModelReady: function( event ) {
        new DataManagementView( {model: fui.models.fusekiServer} ).render();
      }

    } );

    return DataManagementController;
  }
);
