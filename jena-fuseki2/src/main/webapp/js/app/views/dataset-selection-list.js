/**
 * This view presents a list of the available datasets for the user to interact
 * with.
 */

define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        datasetSelectionListTemplate = require( "plugins/text!app/templates/dataset-selection-list.tpl" );

    var DatasetSelectionListView = Backbone.Marionette.ItemView.extend( {
      initialize: function(){
//        _.bindAll(this, "onFilter", "onModelChange");
        this.listenTo( this.model, "change", this.onModelChange, this );
      },

      template: _.template( datasetSelectionListTemplate ),

      el: "#dataset-selection-list",

      ui: {
      },

      events: {
//        "change #independent-variable-selection": "selectVariable",
//        "click a.action.filter": "onFilter"
      },

      templateHelpers: {
      },

//      /** Update the model when the user changes the selection */
//      selectVariable: function( event ) {
//        this.model.set( "independentVarSelection", this.ui.variableSelection.val() );
//      },
//
//      /** User wants to open the filter dialog */
//      onFilter: function( event ) {
//        var varModel = bgViz.models.variablesConfig.independentVar();
//        var rangeType = varModel.component.range().rangeType();
//        var viewName = rangeType.replace(/([a-z])([A-Z])/g, "$1-$2").toLowerCase();
//
//        bgViz.layouts.filterDialog.showFilter( viewName, varModel );
//      },

      /** If the model changes, update the summary */
      onModelChange: function( event ) {
//        this.ui.summary.html( this.model.independentVar().component.range().summarise() );
      }

    });


    return DatasetSelectionListView;
  }
);
