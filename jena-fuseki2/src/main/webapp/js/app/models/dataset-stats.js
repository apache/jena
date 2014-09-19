/**
 * Backbone model denoting statistics on a dataset
 */
define(
  function( require ) {
    "use strict";

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        sprintf = require( "sprintf" );

    /**
     * This model represents the statistics available on a one or more datasets
     */
    var DatasetStats = Backbone.Model.extend( {
      initialize: function( dataset, stats ) {
        this.set( {dataset: dataset, stats: stats} );
      },

      /** Return the number of datasets we have statistics for */
      size: function() {
        return _.keys( datasets() ).length;
      },

      toJSON: function() {
        return this.asTable();
      },

      /** Return a table of the statistics we have, one row per dataset */
      asTable: function() {
        var ds = this.datasets();
        var endpoints = this.collectEndpoints( ds );
        var rows = [];

        _.each( ds, function( d, dsName ) {
          var row = [dsName, d.Requests, d.RequestsGood, d.RequestsBad];
          var es = d.endpoints;

          _.each( endpoints, function( e ) {
            if (es[e.key]) {
              var servStats = es[e.key];

              if (servStats.Requests === 0) {
                row.push( "0" );
              }
              else {
                row.push( sprintf( "%d (%d bad)", servStats.Requests, servStats.RequestsBad ))
              }
            }
            else {
              row.push( "&ndash;" );
            }
          } );

          rows.push( row );
        } );

        return {headings: this.columnHeadings( endpoints ), rows: rows};
      },

      stats: function() {
        return this.get( "stats" );
      },

      datasets: function() {
        return this.stats().datasets;
      },

      /** Reload the numbers from the server */
      refresh: function() {
        var self = this;

        this.get( "dataset" )
            .statistics()
            .done( function( data ) {
              self.set( "stats", data );
            } );
      },

      // internal methods

      collectEndpoints: function( ds ) {
        var endpoints = [];
        _.each( ds, function( d ) {
          var ep = _.each( d.endpoints, function( v, k ) {
            endpoints.push( {key: k, label: v.description} );
          } );
        } );

        return _.uniq( endpoints ).sort();
      },

      columnHeadings: function( services ) {
        return ["Name", "Overall", "Overall good", "Overall bad"].concat( _.pluck( services, 'label' ) );
      }
    } );

    return DatasetStats;
  }
);