/** Component for creating a new dataset with a few simple options */

define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        DatasetSimpleCreateTpl = require( "plugins/text!app/templates/dataset-simple-create.tpl" );

    var DatasetSimpleCreate = Backbone.Marionette.ItemView.extend( {

      initialize: function() {
        _.bindAll( this, "onCommitSimple", "clearWarnings" );
      },

      template: _.template( DatasetSimpleCreateTpl ),

      ui: {
      },

      el: "#dataset-simple-create",

      events: {
        "click a.action.commit.simple": "onCommitSimple",
        "submit form": "onCommitSimple",
        "keydown input[name=dbName]": "clearWarnings"
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
          fui.models.fusekiServer.updateOrCreateDataset( null, options )
                                 .done( this.showDataManagementPage )
                                 .fail( this.showFailureMessage );
        }
      },

//      onCommitUpload: function( e ) {
//        e.preventDefault();
//
//        if (this.validateUploadForm()) {
//          $("#uploadForm").ajaxSubmit( {
//                            success: this.showDataManagementPage,
//                            error: this.showFailureMessage
//                           });
//        }
//      },
//
      showDataManagementPage: function( e ) {
        location = "?tab=datasets";
      },

      /** Todo: need to do a better job of responding to errors */
      showFailureMessage: function( jqXHR, textStatus, errorThrown ) {
        $(".errorOutput").html( sprintf( "<p class='has-error'>Sorry, that didn't work because:</p><pre>%s</pre>", errorThrown || textStatus ) );
      },

      /** Clear current warning states */
      clearWarnings: function() {
        this.clearValidation();
        $(".errorOutput").empty();
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


    return DatasetSimpleCreate;
  }
);
