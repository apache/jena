/** Controller for the admin/data-management.html page */
define(
  function( require ) {
    var Marionette = require( "marionette" ),
    Backbone = require( "backbone" ),
    _ = require( "underscore" ),
    fui = require( "fui" ),
    pageUtils = require( "util/page-utils" ),
    DatasetsDropdownListView = require( "views/datasets-dropdown-list" );

    var UploadController = function() {
      this.initEvents();
    };

    // add the behaviours defined on the controller
    _.extend( UploadController.prototype, {
      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      /** Return a hashmap of datasets */
      datasetsConfig: function( endpoints, datasets ) {
        _.each( datasets, function( ds ) {
          var uploadURL = ds.uploadURL();
          if (uploadURL) {
            endpoints[ds.name()] = uploadURL;

            if (!endpoints["default"]) {
              endpoints["default"] = uploadURL;
            }
          }
        } );

        return endpoints;
      },

      /** Set the default endpoint, if it was passed in the URL */
      setDefaultEndpoint: function( fusekiServer, endpoints ) {
        var dsName = pageUtils.queryParam( "ds" );
        if (dsName) {
          var ds = fusekiServer.dataset( dsName );
          endpoints["default"] = ds.uploadURL();
        }
        $
      },  

      /** Return a hashmap in a form we can pass to initUploader (c.f. a qconsole config)  */
      datasetsConfig: function( endpoints, datasets ) {
        _.each( datasets, function( ds ) {
          var uploadURL = ds.uploadURL();
          if (uploadURL) {
            endpoints[ds.name()] = uploadURL;

            if (!endpoints["default"]) {
              endpoints["default"] = uploadURL;
            }
          }
        } );

        var x = {} ;
        x.endpoints = endpoints ;
        return x ;
      },

      /** When the fuseki server is ready, we can list the initial datasets */
      onServerModelReady: function( event ) {
        var fusekiServer = fui.models.fusekiServer;
        var endpoints = {};
        var datasets = fusekiServer.datasets();

        this.setDefaultEndpoint( fusekiServer, endpoints );  

        var config = this.datasetsConfig(endpoints, datasets) ;
        initUploader(config) ; ;

        var ddlv = new DatasetsDropdownListView( {model: datasets} );
        ddlv.render();
        ddlv.setCurrentDatasetName( pageUtils.queryParam("ds") );
      }

    } );

    // Like qconsole initialization.
    var initUploader = function( config ) {
      initEndpoints(config) ;
      bindEvents();
    };

    /** Bind events that we want to manage */
    var bindEvents = function() {
      $(".endpoints").on( "click", "a", function( e ) {
        var elem = $(e.currentTarget);
        setCurrentEndpoint( $.trim( elem.text() ) );
      } );

      // TODO Conflicts with the JQuery uploader?
      $(".run-upload").on( "click", runUpload );
    };

    /** Set up the drop-down list of end-points */
    var initEndpoints = function( config ) {
      var endpoints = $("ul.endpoints");
      endpoints.empty();

      $.each( config.endpoints, function( key, url ) {
        var html = sprintf( "<li role='presentation'><a role='menuitem' tabindex='-1' href='#'>%s</a></li>",
          url );
        endpoints.append( html );
      } );

      setCurrentEndpoint( config.endpoints["default"] );
    };

    /** Set the current endpoint text */
    var setCurrentEndpoint = function( url ) {
      $("[id=uploadEndpoint]").val( url );
      $('#fileuploadForm').attr('action', url) ;
    };

    /** Return the current endpoint text */
    var currentEndpoint = function( url ) {
      return $("[id=uploadEndpoint]").val();
    };

    var runUpload = function( e ) {
      e.preventDefault();
      resetResults() ;

      console.info("runUpload") ;
      var url = currentEndpoint();
      console.info(sprintf("runUpload: %s", url)) ;

      // ?????
      } ;
    return UploadController;
  }
);
