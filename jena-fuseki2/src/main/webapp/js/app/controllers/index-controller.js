/** Controller for the main index.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        sprintf = require( "sprintf" ),
        DatasetSelectionListView = require( "app/views/dataset-selection-list" );

    var IndexController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( IndexController.prototype, {

      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      onServerModelReady: function() {
        new DatasetSelectionListView( {model: fui.models.fusekiServer} ).render();
        this.displayVersion();
      },

      /** Display the fuseki software version */
      displayVersion: function() {
        var sd = fui.models.fusekiServer.get( "serverDescription" );
        var version = sd.version;
        var uptime = sd.uptime;
        var s = uptime % 60;
        var m = Math.floor( (uptime / 60) % 60 );
        var h = Math.floor( (uptime / (60 * 60)) % 24 );
        var d = Math.floor( (uptime / (60 * 60 * 24)) );

        var status = sprintf( "Version %s. Uptime: %s %s %dm %02ds",
                              version,
                              (d > 0 ? d + "d" : ""),
                              (h > 0 ? h + "h" : ""),
                              m, s );
        $('.host-details').html( status );
      }

    } );

    return IndexController;
  }
);
