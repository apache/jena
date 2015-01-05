/**
 * Reusable component that encapsulates selecting a dataset to work on in a given page.
 * Takes the FusekiServer as a model, and populates a select control to choose one of the
 * current datasets. If the dataset changes, this view will update the `selectedDatasetName`
 * on the model, and trigger the event `dataset.changed`.
 **/

define(
  function( require ) {
    "use strict";

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        sprintf = require( "sprintf" ),
        datasetSelectorTemplate = require( "plugins/text!app/templates/dataset-selector.tpl" );

    var DatasetSelectorView = Backbone.Marionette.ItemView.extend( {

      initialize: function(){
        this.listenTo( this.model, "change", this.render, this );
      },

      template: _.template( datasetSelectorTemplate ),

      el: ".dataset-selector-container",

      ui: {
        select: ".dataset-selector select"
      },

      events: {
        "change .dataset-selector select": "onChangeDataset"
      },

      /**
       * After rendering, set up the dataset picker and notify the rest of the
       * app if the default dataset name is known.
       */
      onRender: function() {
        var selector = $('.selectpicker');
        selector.selectpicker('refresh');

        if (selector.val()) {
          this.unHideDatasetElements();
          this.onChangeDataset();
        }
      },

      /**
       * Respond to a change in the dataset name selection by updating
       * the underlying model. TODO: should also update the application
       * URL.
       */
      onChangeDataset: function( e ) {
        var newDatasetName = this.ui.select.val();
        this.model.set( "selectedDatasetName", newDatasetName );
        this.notifyDatasetName( newDatasetName );
      },

      /**
       * Ensure that elements that should be visible when a dataset is known
       * are not hidden, and vice-versa.
       */
      unHideDatasetElements: function() {
        $(".no-dataset").addClass( "hidden" );
        $(".with-dataset").removeClass( "hidden" );
      },

      /** Trigger an event to notify other components that the dataset
       * name has been selected.
       */
      notifyDatasetName: function( dsName ) {
        fui.vent.trigger( "dataset.changed", dsName || this.ui.select.val() );
      }


    });


    return DatasetSelectorView;
  }
);
