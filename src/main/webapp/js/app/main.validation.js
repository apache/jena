
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

        // TODO: these variables need to be set dynamically based on the current server config
        var options = {
          serverPort: 3030,
          managementPort: 3131
        };

        // initialise the backbone application
        fui.controllers.validationController = new ValidationController();
        fui.start( options );

        // additional services
//        require( 'services/ping-service' ).start(); TODO restore
      });
  }
);