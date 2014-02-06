
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'fui', 'controllers/dataset-stats-controller',
       'sprintf', 'bootstrap',
       'models/fuseki-server', 'models/dataset-stats',
       'views/dataset-stats',
       'services/ping-service',
       'lib/jquery.xdomainrequest',
       'lib/jquery.form'
      ],
      function( _, $, Backbone, Marionette, fui, DatasetStatsController ) {
        var options = {
        };

        // initialise the backbone application
        fui.controllers.datasetStatsController = new DatasetStatsController();
        fui.start( options );

        // additional services
        require( 'services/ping-service' ).start();
      });
  }
);
