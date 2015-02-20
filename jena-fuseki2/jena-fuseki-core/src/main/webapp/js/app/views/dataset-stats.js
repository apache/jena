define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        datasetStatsViewTpl = require( "plugins/text!app/templates/dataset-stats.tpl" );

    var DatasetStatsView = Backbone.Marionette.ItemView.extend( {
      initialize: function() {
        _.bindAll( this, "onShowTab" );

        fui.vent.on( "shown.bs.tab", this.onShowTab );
      },

      template: _.template( datasetStatsViewTpl ),

      ui: {
      },

      el: "#statistics",

      modelEvents: {
        'change': "modelChanged"
      },

      modelChanged: function() {
          this.render();
      },

      onShowTab: function( tab ) {
        if (tab.attr("href") === "#info") {
          this.model.refresh();
        }
      }

    });


    return DatasetStatsView;
  }
);
