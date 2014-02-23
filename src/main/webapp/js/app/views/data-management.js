define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        dataManagementViewTpl = require( "plugins/text!templates/data-management.tpl" );

    var DataManagementView = Backbone.Marionette.ItemView.extend( {
      initialize: function(){
        this.listenTo( this.model, "change", this.onModelChange, this );
      },

      template: _.template( dataManagementViewTpl ),

      ui: {
      },

      el: "#data-management",

      events: {
//        "change #independent-variable-selection": "selectVariable",
//        "click a.action.filter": "onFilter"
      },

      templateHelpers: {
      },


      /** If the model changes, update the summary */
      onModelChange: function( event ) {
//        this.ui.summary.html( this.model.independentVar().component.range().summarise() );
      }

    });


    return DataManagementView;
  }
);
