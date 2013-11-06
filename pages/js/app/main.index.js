
define( ['require', '../common-config'],
  function( require ) {
    require(
      ['underscore', 'jquery', 'backbone', 'marionette', 'fui',
       'sprintf',
       'models/fuseki-server', 'models/dataset',
       'views/dataset-selection-list'
      ],
      function( _, $, Backbone, Marionette, fui ) {

        // TODO: these variables need to be set dynamically based on the current server config
        var options = {
          serverPort: 3030,
          managementPort: 3131
        };

        // initialise the backbone application
        fui.start( options );
      });
  }
);