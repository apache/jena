/**
 * Reusable component that encapsulates managing a collection of sub-views as
 * tabs, with the active tab being selected via the URL query param `tab`.
 **/

define(
  function( require ) {
    "use strict";

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        sprintf = require( "sprintf" ),
        PageUtils = require( "app/util/page-utils" );

    var TabbedViewManagerView = Backbone.View.extend( {

      initialize: function(){
        this._tab = PageUtils.queryParam( "tab" );
        this._firstRender = false;
      },

      render: function() {
        if (!this._firstRender) {
          this._firstRender = true;
          this.activateCurrentTab();

          $(".nav-tabs").on( "shown.bs.tab", function( e ) {
            fui.vent.trigger( "shown.bs.tab", $(e.target) );
          } );
        }
      },

      /**
       * Make the tab named as the current tab active. If no named tab, make
       * the first tab active by default.
       */
      activateCurrentTab: function() {
        var tabs = $(".nav-tabs");
        var tab = tabs.children().first();

        if (this._tab) {
          tab = tabs.find( sprintf( "a[href=#%s]", this._tab ) )
                    .parent();
        }

        if (!tab.is(".active")) {
          tabs.children( "li" ).removeClass( "active" );
          tabs.parent().children(".tab-pane").removeClass("active");

          tab.addClass( "active" );
          $( tab.children( "a" ).attr( "href" ) ).addClass("active");
        }
      }


    });


    return TabbedViewManagerView;
  }
);
