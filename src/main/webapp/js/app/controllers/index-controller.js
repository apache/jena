/** Controller for the main index.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        qonsole = require( "lib/qonsole" ),
        DatasetSelectionListView = require( "views/dataset-selection-list" );

    var IndexController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( IndexController.prototype, {
      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      /** When the fuseki server is ready, we can init the qonsole */
      onServerModelReady: function( event ) {
        new DatasetSelectionListView( {model: fui.models.fusekiServer} ).render();
      }

    } );

    return IndexController;
  }
);
