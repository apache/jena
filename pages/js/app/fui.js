/**
 * Top-level application code module for Fuseki UI
 */

define( ['require', 'backbone', 'marionette'],
  function( require, Backbone, Marionette ) {
    // define the application object, and add it to the global namespace
    var fui = new Marionette.Application();

    // define some Marionette modules, because they have a lifecycle component
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

    fui.on('initialize:after', function( options ) {
      // Backbone.history.start();
      this.initialized = true;

      // view association
      // TODO temp: this will migrate into the layout component in due course
      var DatasetSelectionList = require( "views/dataset-selection-list" );
      var dsl = new DatasetSelectionList( {model: fui.models.fusekiServer} );
      var dslRegion = new Marionette.Region({
        el: '#datasetSelectionList'
      });
      dslRegion.show(dsl);
    });


    return fui;
  }
);
