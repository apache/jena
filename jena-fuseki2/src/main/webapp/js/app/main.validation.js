
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette',
       'app/fui', 'app/controllers/validation-controller',
       'sprintf', 'bootstrap',
       'app/models/validation-options',
       'app/services/ping-service',
       'app/services/validation-service',
       'jquery.xdomainrequest'
      ],
      function( _, $, Backbone, Marionette, fui, ValidationController ) {
        var options = { } ;

        // initialise the backbone application
        fui.controllers.validationController = new ValidationController();
        fui.start( options );

        // additional services
//        require( 'services/ping-service' ).start(); TODO restore
      });
  }
);