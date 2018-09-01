/** Controller for the file uploader component */

define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        sprintf = require("sprintf"),
        pageUtils = require( "app/util/page-utils" ),
        fui = require( "app/fui" ),
        FileUploadView = require( "app/views/file-upload" );

    var UploadController = function() {
      this.initialize();
    };

    _.extend( UploadController.prototype, {

      /** Initialize the controler */
      initialize: function() {
        if (fui.models.fusekiServer && fui.models.fusekiServer.get( "ready" )) {
          this.onServerModelReady();
        }
        else {
          _.bindAll( this, "onServerModelReady" );
          fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
        }

      },

      /** When the fuseki server is ready, we can set up the initial view */
      onServerModelReady: function( event ) {
        var fusekiServer = fui.models.fusekiServer;

        fui.views.fileUploadView = new FileUploadView();
        fui.views.fileUploadView.render();
      },
    } );

    return UploadController;
  }
);
