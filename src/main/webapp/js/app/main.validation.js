
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'fui', 'controllers/validation-controller',
       'sprintf', 'bootstrap',
       'models/validation-options',
       'services/ping-service', 'services/validation-service',
       'lib/jquery.xdomainrequest'
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