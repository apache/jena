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

      el: "#stats",

      events: {
//        "click a.action.commit.simple": "onCommitSimple",
//        "click a.action.upload": "onCommitUpload"
      },

      templateHelpers: {
      },

      modelEvents: {
        'change': "modelChanged"
      },

      modelChanged: function() {
          this.render();
      }

      // event handlers


    });


    return DatasetStatsView;
  }
);
