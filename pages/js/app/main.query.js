
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'fui', 'controllers/query-controller',
       'sprintf', 'bootstrap',
       'models/fuseki-server', 'models/dataset',
       'views/dataset-selection-list',
       'services/ping-service',
       'lib/jquery.xdomainrequest', 'lib/qonsole'
      ],
      function( _, $, Backbone, Marionette, fui, QueryController ) {

        // TODO: these variables need to be set dynamically based on the current server config
        var options = {
          serverPort: 3030,
          managementPort: 3131
        };

        // initialise the backbone application
        fui.controllers.queryController = new QueryController();
        fui.start( options );

        // additional services
        require( 'services/ping-service' ).start();
      });
  }
);
