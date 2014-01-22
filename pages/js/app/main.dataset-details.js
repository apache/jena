
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'fui', 'controllers/dataset-details-controller',
       'sprintf', 'bootstrap',
       'models/fuseki-server', 'models/dataset',
       'views/dataset-details',
       'services/ping-service',
       'lib/jquery.xdomainrequest',
       'lib/jquery.form'
      ],
      function( _, $, Backbone, Marionette, fui, DatasetDetailsController ) {
        var options = {
        };

        // initialise the backbone application
        fui.controllers.datasetDetailsController = new DatasetDetailsController();
        fui.start( options );

        // additional services
        require( 'services/ping-service' ).start();
      });
  }
);
