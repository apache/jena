define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        datasetDetailsViewTpl = require( "plugins/text!templates/dataset-details.tpl" );

    var DatasetDetailsView = Backbone.Marionette.ItemView.extend( {
      initialize: function(){
      },

      template: _.template( datasetDetailsViewTpl ),

      ui: {
      },

      el: "#dataset-details",

      events: {
//        "change #independent-variable-selection": "selectVariable",
//        "click a.action.filter": "onFilter"
      },

      templateHelpers: {
      },

      serializeData: function() {
        return this.model;
      }


    });


    return DatasetDetailsView;
  }
);
