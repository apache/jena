
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'fui', 'controllers/index-controller',
       'sprintf', 'bootstrap',
       'models/fuseki-server', 'models/dataset',
       'views/dataset-selection-list',
       'services/ping-service'
      ],
      function( _, $, Backbone, Marionette, fui, IndexController ) {

        // TODO: these variables need to be set dynamically based on the current server config
        var options = {
          serverPort: 3030,
          managementPort: 3131
        };

        // initialise the backbone application
        fui.controllers.indexController = new IndexController();
        fui.start( options );

        // additional services
        require( 'services/ping-service' ).start();
      });
  }
);