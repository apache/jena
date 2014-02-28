define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'fui', 'controllers/upload-controller',
       'sprintf', 'bootstrap',
       'models/fuseki-server', 'models/dataset',
       'views/dataset-selection-list',
       'services/ping-service',
       'lib/jquery.xdomainrequest'
      ],
      function( _, $, Backbone, Marionette, fui, UploadController ) {
          var options = { } ;

        // initialise the backbone application
        fui.controllers.uploadController = new UploadController();
        fui.start( options );

        // additional services
        require( 'services/ping-service' ).start();
      });
  }
);
