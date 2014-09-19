define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" );

    var ValidationOptions = Backbone.Marionette.ItemView.extend( {
      initialize: function(){
        _.bindAll( this, "onValidateAs", "onOutputFormat", "onModelChange" );
        this.listenTo( this.model, "change", this.onModelChange, this );
      },

      el: ".validation",

      events: {
        "click .validate-as-options a": "onValidateAs",
        "click .output-format-options a": "onOutputFormat",
      },

      templateHelpers: {
      },

      onValidateAs: function( e ) {
        e.preventDefault();
        var elem = $(e.currentTarget);
        this.model.setValidateAs( elem.data( "validate-as" ) );
        this.$el.find(".validate-as-options a").removeClass("active");
        elem.addClass("active");

        if (this.model.validateAsQuery()) {
          this.$el.find(".output-format-options").removeClass("hidden");
        }
        else {
          this.$el.find(".output-format-options").addClass("hidden");
        }
      },

      onOutputFormat: function( e ) {
        e.preventDefault();
        var elem = $(e.currentTarget);
        this.model.setOutputFormat( elem.data( "output-format" ) );
        this.$el.find(".output-format-options a").removeClass("active");
        elem.addClass("active");
      },

      onModelChange: function( event ) {
      }

    });


    return ValidationOptions;
  }
);
