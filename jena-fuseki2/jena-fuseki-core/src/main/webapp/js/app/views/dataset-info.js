/** Component for showing detailed information about a dataset */

define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        DatasetInfoTpl = require( "plugins/text!app/templates/dataset-info.tpl" ),
        DatasetStatsView = require( "app/views/dataset-stats" ),
        DatasetStatsModel = require( "app/models/dataset-stats" );

    var DatasetInfo = Backbone.Marionette.ItemView.extend( {

      initialize: function() {
        _.bindAll( this, "onModelChanged", "onCountGraphs" );

        this.showStatistics( true );
        this.model.on( "change", this.onModelChanged );
      },

      template: _.template( DatasetInfoTpl ),

      ui: {
        stats: "#statistics",
        count: ".count-graphs"
      },

      el: "#info .with-dataset",

      events: {
        "click .count-graphs": "onCountGraphs"
      },

      templateHelpers: {
      },

      serializeData: function() {
        return this.model;
      },

      /** Alias for the model */
      dataset: function() {
        return this.model;
      },

      // event handlers

      onModelChanged: function() {
        if (!this.model.counting) {
          this.render();
          this.showStatistics( false );
        }
      },

      onCountGraphs: function( e ) {
        e.preventDefault();
        this.model.count();
      },

      showStatistics: function( keep ) {
        var self = this;

        this.model
            .statistics( keep )
            .done( function( data ) {
                     var statsModel = new DatasetStatsModel( self.dataset(), data );
                     new DatasetStatsView( {model: statsModel} ).render();
                   } );
      }

    });


    return DatasetInfo;
  }
);
