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
        "click a.action.commit.simple": "onCommitSimple",
        "click a.action.upload": "onCommitUpload"
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
                                 .done( this.showDataManagementPage )
                                 .fail( this.showFailureMessage );
        }
      },

      onCommitUpload: function( e ) {
        e.preventDefault();

        if (this.validateUploadForm()) {
          $("#uploadForm").ajaxSubmit( {
                            success: this.showDataManagementPage,
                            error: this.showFailureMessage
                           });
        }
      },

      showDataManagementPage: function( e ) {
        location = "admin-data-management.html";
      },

      /** Todo: need to do a better job of responding to errors */
      showFailureMessage: function( jqXHR, textStatus, errorThrown ) {
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

      validateUploadForm: function() {
        this.clearValidation();

        if (! $("input[name=assemblerFile]").val()) {
          $(".assemblerFileValidation").removeClass("hidden")
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
