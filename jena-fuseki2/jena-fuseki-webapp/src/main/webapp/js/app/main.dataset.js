/** RequireJS dependency configuration for dataset.html page */

define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'app/fui', 'app/controllers/dataset-controller',
       'sprintf',
       'bootstrap-select.min',
       'app/controllers/query-controller',
       'app/controllers/upload-controller',
       'app/models/fuseki-server',
       'app/models/dataset',
       'app/views/dataset-selector',
       'app/views/tabbed-view-manager',
       'app/services/ping-service',
       'jquery.xdomainrequest',
       'jquery.form',
       'jquery.fileupload'
      ],
      function( _, $, Backbone, Marionette, fui, DatasetController ) {
          var options = { };

        // initialise the backbone application
        fui.controllers.datasetController = new DatasetController();
        fui.start( options );

        // additional services
        require( 'app/services/ping-service' ).start();
      });
  }
);
