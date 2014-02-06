/**
 * Backbone model denoting statistics on a dataset
 */
define(
  function( require ) {
    "use strict";

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "fui" ),
        sprintf = require( "sprintf" );

    /**
     * This model represents the statistics available on a given named dataset
     */
    var DatasetStats = Backbone.Model.extend( {
      initialize: function( fusekiServer, dsName ) {
        _.bindAll( this, "onLoadDone", "onLoadFail" );
        this.set( {"ds.name": dsName,
                   url: fusekiServer.statsURL( dsName )
                  } );

        this.load();
      },

      load: function() {
        $.getJSON( this.url() )
         .done( this.onLoadDone )
         .fail( this.onLoadFail );
      },

      /** Return the number of datasets we have statistics for */
      size: function() {
        return _.keys( datasets() ).length;
      },

      name: function() {
        return this.get( "ds.name" );
      },

      toJSON: function() {
        return this.table();
      },

      /** Return a table of the statistics we have, one row per dataset */
      table: function() {
        var ds = this.datasets();
        var services = this.collectServices( ds );
        var rows = [];

        _.each( ds, function( d, dsName ) {
          var row = [dsName, d.Requests, d.RequestsGood, d.RequestsBad];
          var s = d.services;

          _.each( services, function( service ) {
            if (s[service]) {
              var servStats = s[service];

              if (servStats.Requests === 0) {
                row.push( "0" );
              }
              else {
                row.push( sprintf( "%d (%d bad)", servStats.Requests, servStats.RequestsBad ))
              }
            }
            else {
              row.push( "" );
            }
          } );

          rows.push( row );
        } );

        return {headings: this.columnHeadings( services ), rows: rows};
      },

      url: function() {
        return this.get( "url" );
      },

      stats: function() {
        return this.get( "stats" );
      },

      datasets: function() {
        return this.stats() ? this.stats().datasets : {};
      },

      onLoadDone: function( data ) {
        this.set( {stats: data} );
        fui.vent.trigger( "model.stats.loaded" );
      },

      onLoadFail: function() {
        fui.vent.trigger( "model.stats.loadFailed" );
      },

      // internal methods

      collectServices: function( ds ) {
        var services = [];
        _.each( ds, function( d ) {
          services = services.concat( _.keys( d.services ) );
        } );
        return _.uniq( services ).sort();
      },

      columnHeadings: function( services ) {
        return ["Service name", "overall", "good", "bad"].concat( services );
      }
    } );

    return DatasetStats;
  }
);