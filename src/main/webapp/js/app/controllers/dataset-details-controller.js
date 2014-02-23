/** Controller for the admin/data-management.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        DatasetDetailsView = require( "views/dataset-details" );

    var DatasetDetailsController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( DatasetDetailsController.prototype, {
      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      /** When the fuseki server is ready, we can list the initial datasets */
      onServerModelReady: function( event ) {
        new DatasetDetailsView( {model: this.assembleViewModel( fui )} ).render();
      },

      /** Assemble a model which we can pass to the dataset details view to render */
      assembleViewModel: function( fui ) {
        var viewModel = {};

        this.setEditOrCreate( viewModel );
        viewModel.fusekiServer = fui.models.fusekiServer;

        return viewModel;
      },

      /** If the dataset ID was given in the location param, we use that otherwise it's a new dataset */
      setEditOrCreate: function( viewModel ) {
        var dsId = this.getDatasetId();
        if (dsId) {
          viewModel.datasetId = dsId;
          viewModel.commitAction = "update dataset"
          viewModel.newDataset = false;
        }
        else {
          viewModel.newDataset = true;
          viewModel.commitAction = "create new dataset"
        }
      },

      getDatasetId: function() {
        var dsId = $.trim( location.hash );
        return (dsId == "") ? null : dsId.substr(1).replace( "/", "" );
      }

    } );

    return DatasetDetailsController;
  }
);
