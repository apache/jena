
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

        var options = { } ;

        // initialise the backbone application
        fui.controllers.queryController = new QueryController();
        fui.start( options );
	  
        // additional services
        require( 'services/ping-service' ).start();
      });
  }
);
