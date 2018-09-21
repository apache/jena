/** Utilities for managing HTML pages */

define(
  function( require ) {
    "use strict";

    var _ = require( "underscore" );

    /** Return true if a given query parameter is defined, otherwise null */
    var hasQueryParam = function( param ) {
      return !!queryParam( param );
    };

    /** Return the value of a query parameter, or null */
    var queryParam = function( param ) {
      var p = param && queryParams()[param];
      return p ? p : null;
    };

    /** Return the current query params as a map */
    var queryParams = function() {
      return _.chain( document.location.search.slice(1).split('&') )
              .invoke('split', '=')
              .object()
              .value();
    };

    return {
      hasQueryParam: hasQueryParam,
      queryParam: queryParam
    };
  }
);