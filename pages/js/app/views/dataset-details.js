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
        "click a.action.commit.simple": "onCommitSimple"
//        "change #independent-variable-selection": "selectVariable",
//        "click a.action.filter": "onFilter"
      },

      templateHelpers: {
      },

      serializeData: function() {
        return this.model;
      },

      // event handlers

      onCommitSimple: function( e ) {
        e.preventDefault();

        if (this.validateSimpleForm()) {
          var options = $("#simple-edit form").serializeArray();
          fui.models.fusekiServer.updateOrCreateDataset( this.model.name, options )
                                 .success( this.onUpdateOrCreateSuccess )
                                 .fail( this.onUpdateOrCreateFail );
        }
      },

      onUpdateOrCreateSuccess: function( e ) {
        location = "admin-data-management.html";
      },

      /** Todo: need to do a better job of responding to errors */
      onUpdateOrCreateFail: function( jqXHR, textStatus, errorThrown ) {
        $(".errorOutput").html( sprintf( "<p class='has-error'>Sorry, that didn't work because:</p><pre>%s</pre>", errorThrown || textStatus ) );
      },

      // validation

      validateSimpleForm: function() {
        this.clearValidation();

        if (! $("input[name=dbName]").val()) {
          $(".dbNameValidation").removeClass("hidden")
                                .parents(".form-group" )
                                .addClass( "has-error" );
          return false;
        }

        return true;
      },

      clearValidation: function() {
        $(".has-error").removeClass( "has-error" );
        $(".has-warning").removeClass( "has-warning" );
      }

    });


    return DatasetDetailsView;
  }
);
