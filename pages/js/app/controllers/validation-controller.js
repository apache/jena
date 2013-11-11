/** Controller for the main index.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        ValidationOptions = require( "views/validation-options" ),
        ValidationService = require( "services/validation-service" );

    var ValidationController = function() {
      this.initEvents();
      this.initServices();
    };

    // add the behaviours defined on the controller
    _.extend( ValidationController.prototype, {
      initEvents: function() {
        fui.vent.on( "models.validation-options.ready", this.onValidationOptionsModelReady );
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
