/**
 * Top-level application code module for Fuseki UI
 */

define(
  function( require ) {
    var Backbone = require( 'backbone' ),
        Marionette = require( 'marionette' );

    // define the application object, and add it to the global namespace
    var fui = new Marionette.Application();

    // define someMarionette modules, because they have a lifecycle component
    // see https://github.com/marionettejs/backbone.marionette/wiki/AMD-Modules-vs-Marionette%27s-Modules
    fui.module( "models" );
    fui.module( "views" );
    fui.module( "apis" );
    fui.module( "layouts" );
    fui.module( "controllers" );

    // define top-level regions where our layouts will go
    fui.addRegions({
    });

    fui.on('initialize:before', function( options ) {
      // attach API instances
//      fui.apis.qb = new QB( options );
    });

    fui.on('initialize:after', function() {
      // Backbone.history.start();
      this.initialized = true;
    });

    return fui;
  }
);
