
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'fui', 'controllers/data-management-controller',
       'sprintf', 'bootstrap',
       'models/fuseki-server', 'models/dataset',
       'views/data-management',
       'services/ping-service',
       'lib/jquery.xdomainrequest'
      ],
      function( _, $, Backbone, Marionette, fui, DataManagementController ) {

        var options = { } ;

        // initialise the backbone application
        fui.controllers.dataManagementController = new DataManagementController();
        fui.start( options );

        // additional services
        require( 'services/ping-service' ).start();
      });
  }
);
