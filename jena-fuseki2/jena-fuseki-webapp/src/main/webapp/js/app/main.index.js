
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette',
       'app/fui', 'app/controllers/index-controller',
       'sprintf', 'bootstrap',
       'app/models/fuseki-server',
       'app/models/dataset',
       'app/views/dataset-selection-list',
       'app/services/ping-service'
      ],
      function( _, $, Backbone, Marionette, fui, IndexController ) {
        var options = { };

        // initialise the backbone application
        fui.controllers.indexController = new IndexController();
        fui.start( options );

        // additional services
        require( 'app/services/ping-service' ).start();
      });
  }
);