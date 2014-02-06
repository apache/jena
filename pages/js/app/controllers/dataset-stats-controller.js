/** Controller for the admin-stats.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        DatasetStatsView = require( "views/dataset-stats" );

    var DatasetStatsController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( DatasetStatsController.prototype, {
      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      /** When the fuseki server is ready, we can list the initial datasets */
      onServerModelReady: function( event ) {
        new DatasetStatsView( {model: this.assembleViewModel( fui )} ).render();
      },

      /** Assemble a model which we can pass to the dataset details view to render */
      assembleViewModel: function( fui ) {
        var viewModel = {};

        viewModel.fusekiServer = fui.models.fusekiServer;

        return viewModel;
      },

      getDatasetId: function() {
        var dsId = $.trim( location.hash );
        return (dsId == "") ? null : dsId.substr(1).replace( "/", "" );
      }

    } );

    return DatasetStatsController;
  }
);
