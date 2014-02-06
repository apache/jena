define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        datasetStatsViewTpl = require( "plugins/text!templates/dataset-stats.tpl" );

    var DatasetStatsView = Backbone.Marionette.ItemView.extend( {
      initialize: function(){
      },

      template: _.template( datasetStatsViewTpl ),

      ui: {
      },

      el: "#dataset-stats",

      events: {
//        "click a.action.commit.simple": "onCommitSimple",
//        "click a.action.upload": "onCommitUpload"
      },

      templateHelpers: {
      },

      serializeData: function() {
        return this.model;
      }

      // event handlers


    });


    return DatasetStatsView;
  }
);
