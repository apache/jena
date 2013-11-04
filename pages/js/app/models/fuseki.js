/**
 * Backbone model denoting the remote Fuseki server.
 */

define(
  function( require ) {
    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" );

    var Fuseki = Backbone.Model.extend( {} );

    return Fuseki;
  }
);