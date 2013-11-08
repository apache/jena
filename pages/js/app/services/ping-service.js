/**
 * The ping service checks the status of the attached server and sets the light in the
 * control bar accordingly.
 */
define( ['jquery', 'underscore', 'sprintf'],
  function( $, _, sprintf ) {

    var DEFAULT_PING_TIME = 5000;
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

    var start = function( period ) {
      ping( period || DEFAULT_PING_TIME );
    };

    var ping = function( period ) {
      onBeforeSend();
      $.getJSON( "/ping.txt" ).done( onPingSuccess )
                          .fail( onPingFail );
      setTimeout( function() {ping( period );}, period );
    };

    return {
      start: start
    }
  }
);