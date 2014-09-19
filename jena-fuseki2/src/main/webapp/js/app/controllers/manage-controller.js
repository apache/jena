/** Controller for the admin/data-management.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        TabbedViewManagerView = require( "app/views/tabbed-view-manager" ),
        DatasetSimpleCreateView = require( "app/views/dataset-simple-create" ),
        DatasetManagementView = require( "app/views/dataset-management" );

    var ManageController = function() {
      this.initEvents();
    };

    _.extend( ManageController.prototype, {

      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      /** When the fuseki server is ready, we can list the initial datasets */
      onServerModelReady: function( event ) {
        fui.views.datasetManagement = new DatasetManagementView( {model: fui.models.fusekiServer} );
        fui.views.datasetManagement.render();

        fui.views.tabbedViewManagerView = new TabbedViewManagerView();
        fui.views.tabbedViewManagerView.render();

        fui.views.datasetSimpleCreate = new DatasetSimpleCreateView();
        fui.views.datasetSimpleCreate.render();
      }

    } );

    return ManageController;
  }
);
