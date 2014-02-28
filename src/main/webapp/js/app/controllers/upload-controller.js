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
	    bindEvents() ;
    };

    var _config = {} ;

    var initEndpoints = function(endpoints) {
      var ulEndpoints = $("ul.endpoints");
      ulEndpoints.empty();

      $.each( endpoints, function( key, url ) {
        var html = sprintf( "<li role='presentation'><a role='menuitem' tabindex='-1' href='#'>%s</a></li>", url );
        ulEndpoints.append( html );
      } );

      setCurrentEndpoint( endpoints["default"] );
    } ;

    var bindEvents = function() {
      $(".endpoints").on( "click", "a", function( e ) {
        var elem = $(e.currentTarget);
        setCurrentEndpoint( $.trim( elem.text() ) );
      } );
      $("ul.formats").on( "click", "a", function( e ) {
        var elem = $(e.currentTarget);
        setCurrentFormat( elem.data( "value" ), $.trim( elem.text() ) );
      } );

      $(".run-upload").on( "click", runUpload );
    } ;

     /** Return the current endpoint text */
    var currentEndpoint = function( ) {
      return $("[id=uploadEndpoint]").val();
    };

    /** Set the current endpoint text */
    var setCurrentEndpoint = function( url ) {
      $("[id=uploadEndpoint]").val( url );
    };

    var runUpload = function( e ) {
      console.info("runUpload") ;
      e.preventDefault();

      var url = currentEndpoint();
      console.info(sprintf("runUpload: %s", url)) ;

      var options = {
//        data: {query: query, output: format},
//        success: function( data, xhr ) {
//          onQuerySuccess( data, format );
//        },
        success: onUploadSuccess ,
        error: onUploadFail
      };

      $.ajax( url, options );
    } ;

    var onUploadSuccess = function( data, format ) {    
        console.info("Upload - onUploadSuccess") ;
    } ;

    /** Report query failure */
    var onUploadFail = function( jqXHR, textStatus, errorThrown ) {
      var text = jqXHR.valueOf().responseText || sprintf( "Sorry, that didn't work because: '%s'", jqXHR.valueOf().statusText );
      $("#results").html( sprintf( "<pre class='text-danger'>%s</pre>", _.escape(text) ) );
    };

    // add the behaviours defined on the controller
    _.extend( UploadController.prototype, {
      initEvents: function() {
        _.bindAll( this, "onServerModelReady" );
        fui.vent.on( "models.fuseki-server.ready", this.onServerModelReady );
      },

      /** Set the default endpoint, if it was passed in the URL */
      setDefaultEndpoint: function( fusekiServer, endpoints ) {
        var dsName = pageUtils.queryParam( "ds" );
        if (dsName) {
          var ds = fusekiServer.dataset( dsName );
          endpoints["default"] = ds.uploadURL();
        }
      },	

      /** When the fuseki server is ready, we can list the initial datasets */
      onServerModelReady: function( event ) {
	      var fusekiServer = fui.models.fusekiServer;
        var endpoints = {};
        var datasets = fusekiServer.datasets();

        this.setDefaultEndpoint( fusekiServer, endpoints );  

        initEndpoints(endpoints) ;

        var ddlv = new DatasetsDropdownListView( {model: datasets} );
        ddlv.render();
        ddlv.setCurrentDatasetName( pageUtils.queryParam("ds") );
      }

    } );

      return UploadController;
  }
);
