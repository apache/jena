/**
 * This view encapsulates a single uploadable file
 */

define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        uploadableFileTemplate = require( "plugins/text!app/templates/uploadable-file.tpl" );

    var UploadableFileView = Backbone.Marionette.ItemView.extend( {
      initialize: function(){
      },

      tagName: "li",

      template: _.template( uploadableFileTemplate ),

      events: {
        "click .action-remove-upload": "onActionRemoveUpload",
        "click .action-upload-file": "onActionUploadFile"
      },

      onActionRemoveUpload: function( e ) {
        e.preventDefault();
        fui.vent.trigger( "upload.remove", this.model );
      },

      onActionUploadFile: function( e ) {
        e.preventDefault();
        fui.vent.trigger( "upload.perform", this.model );
      }

    });

    return UploadableFileView;
  }
);
