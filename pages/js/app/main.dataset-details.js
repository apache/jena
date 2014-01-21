
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'fui', 'controllers/dataset-details-controller',
       'sprintf', 'bootstrap',
       'models/fuseki-server', 'models/dataset',
       'views/dataset-details',
       'services/ping-service',
       'lib/jquery.xdomainrequest'
      ],
      function( _, $, Backbone, Marionette, fui, DatasetDetailsController ) {

        // TODO: these variables need to be set dynamically based on the current server config
        var options = {
          serverPort: 3030,
          managementPort: 3131
        };

        // initialise the backbone application
        fui.controllers.datasetDetailsController = new DatasetDetailsController();
        fui.start( options );

        // additional services
        require( 'services/ping-service' ).start();
      });
  }
);
