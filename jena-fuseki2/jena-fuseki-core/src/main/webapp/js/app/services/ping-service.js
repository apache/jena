/**
 * The ping service checks the status of the attached server and sets the light in the
 * control bar accordingly.
 */
define( ['jquery', 'underscore', 'sprintf'],
  function( $, _, sprintf ) {

    var PING_URL = "$/ping"
    var DEFAULT_PING_TIME = 500000;  // TODO slowed down during debugging phase
    var _startTime = 0;

    var onBeforeSend = function() {
      _startTime = new Date().getTime();
    };

    var duration = function() {
      return new Date().getTime() - _startTime;
    };

    var onPingSuccess = function( ) {
      setPingStatus( "server-up", sprintf( "Last ping returned OK in %dms", duration() ) );
    };

    var onPingFail = function( jqXHR, msg, errorThrown ) {
      setPingStatus( "server-down", sprintf( "Last ping returned '%s' in %dms", errorThrown || msg, duration() ) );
    };

    var setPingStatus = function( lampClass, statusText ) {
      $( "a#server-status-light span").removeClass()
                                      .addClass( lampClass )
                                      .attr( "title", statusText );
    };

    /** Return a cache-defeating ping URL */
    var ping_url = function() {
      return PING_URL + "?_=" + Math.random();
    };

    var start = function( period ) {
      ping( period || DEFAULT_PING_TIME );
    };

    var ping = function( period ) {
      onBeforeSend();
      $.get( ping_url() ).done( onPingSuccess )
                         .fail( onPingFail );
      setTimeout( function() {ping( period );}, period );
    };

    return {
      start: start
    }
  }
);