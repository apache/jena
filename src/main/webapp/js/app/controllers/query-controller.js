/** Controller for the main index.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        qonsole = require( "lib/qonsole" ),
        pageUtils = require( "util/page-utils" ),
        DatasetsDropdownListView = require( "views/datasets-dropdown-list" );

    var QueryController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( QueryController.prototype, {
      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      /** Return a hashmap of datasets, in a form we can pass to qonsole */
      datasetsConfig: function( endpoints, datasets ) {
        _.each( datasets, function( ds ) {
          var queryURL = ds.queryURL();
          if (queryURL) {
            endpoints[ds.name()] = queryURL;

            if (!endpoints["default"]) {
              endpoints["default"] = queryURL;
            }
          }
        } );

        return endpoints;
      },

      /** Initialise the qonsole component */
      initQonsole: function( datasetsConfig ) {
        var qonfig = require( "qonsole-config" );
        qonfig.endpoints = datasetsConfig;
        qonsole.init( qonfig );
      },

      /** Set the default endpoint, if it was passed in the URL */
      setDefaultEndpoint: function( fusekiServer, endpoints ) {
        var dsName = pageUtils.queryParam( "ds" );
        if (dsName) {
          var ds = fusekiServer.dataset( dsName );
          endpoints["default"] = ds.queryURL();
        }
      },

      /** When the fuseki server is ready, we can init the qonsole */
      onServerModelReady: function( event ) {
        var fusekiServer = fui.models.fusekiServer;
        var endpoints = {};
        var datasets = fusekiServer.datasets();

        this.setDefaultEndpoint( fusekiServer, endpoints );
        this.initQonsole( this.datasetsConfig( endpoints, datasets ) );

        var ddlv = new DatasetsDropdownListView( {model: datasets} );
        ddlv.render();
        ddlv.setCurrentDatasetName( pageUtils.queryParam("ds") );
      }

    } );

    return QueryController;
  }
);
