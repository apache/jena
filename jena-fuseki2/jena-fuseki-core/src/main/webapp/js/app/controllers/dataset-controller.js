/** Controller for the dataset.html page */

define(
  function( require ) {

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        DatasetSelectorView = require( "app/views/dataset-selector" ),
        TabbedViewManagerView = require( "app/views/tabbed-view-manager" ),
        FileUploadView = require( "app/views/file-upload" ),
        DatasetInfoView = require( "app/views/dataset-info" ),
        DatasetEditView = require( "app/views/dataset-edit" ),
        QueryController = require( "app/controllers/query-controller" ),
        UploadController = require( "app/controllers/upload-controller" );

    var DatasetController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( DatasetController.prototype, {

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

      /**
       * When the fuseki server is ready, we can list the initial datasets, and
       * start the tab manager to manage the tabbed content.
       **/
      onServerModelReady: function() {
        fui.views.datasetSelectorView = new DatasetSelectorView( {model: fui.models.fusekiServer} )
        fui.views.datasetSelectorView.render();

        fui.views.tabbedViewManagerView = new TabbedViewManagerView();
        fui.views.tabbedViewManagerView.render();

        fui.controllers.queryController = new QueryController();
        fui.controllers.uploadController = new UploadController();

      },

      /** Dataset has changed */
      onDatasetChanged: function( dsName ) {
        var dataset = fui.models.fusekiServer.dataset( dsName );
        fui.views.datasetInfoView = new DatasetInfoView( {model: dataset} );
        fui.views.datasetInfoView.render();

        fui.views.datasetEditView = new DatasetEditView( {model: dataset} );
        fui.views.datasetEditView.render();
      }

    } );

    return DatasetController;

  }
);