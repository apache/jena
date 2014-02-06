/** Controller for the admin-stats.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        DatasetStatsView = require( "views/dataset-stats" ),
        DatasetStatsModel = require( "models/dataset-stats" ),
        PageUtils = require( "util/page-utils" );

    var DatasetStatsController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( DatasetStatsController.prototype, {
      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      onServerModelReady: function( event ) {
        var dsName = PageUtils.queryParam( "ds" );
        fui.models.datasetStats = new DatasetStatsModel( fui.models.fusekiServer, {dsName: dsName} );
        new DatasetStatsView( {model: fui.models.datasetStats} ).render();
      }

    } );

    return DatasetStatsController;
  }
);
