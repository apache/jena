/** Controller for the main index.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        ValidationOptions = require( "app/views/validation-options" ),
        ValidationService = require( "app/services/validation-service" );

    var ValidationController = function() {
      this.initServices();
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( ValidationController.prototype, {
      initEvents: function() {
        fui.vent.on( "models.validation-options.ready", this.onValidationOptionsModelReady );
        $(".validation").on( "click", "a.perform-validation", function( event ) {
          fui.services.validation.performValidation( fui.views.validationOptions.model );
        } );
      },

      onValidationOptionsModelReady: function( e ) {
        fui.views.validationOptions = new ValidationOptions( {model: fui.models.validationOptions} );
      },

      initServices: function() {
        fui.services.validation = new ValidationService( "#query-edit-cm", "#validation-output-cm" );
        fui.services.validation.init();
      }

    } );

    return ValidationController;
  }
);
