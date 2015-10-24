/**
 * Backbone model denoting the remote Fuseki server.
 */
define(
  function( require ) {
    "use strict";

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        sprintf = require( "sprintf" ),
        Task = require( "app/models/task" );

    /**
     * This model represents the core representation of the remote Fuseki
     * server. Individual datasets have their own model.
     */
    var Dataset = Backbone.Model.extend( {
      initialize: function( datasetDescription, baseURL, mgmtURL ) {
        this.set( datasetDescription );
        this.set( {
                    baseURL: baseURL,
                    mgmtURL: mgmtURL,
                    counts: {},
                    countPerformed: false,
                    counting: false,
                    statistics: false
                  } );
      },

      baseURL: function() {
        return this.get( "baseURL" );
      },

      mgmtURL: function() {
        return this.get( "mgmtURL" );
      },

      mgmtActionURL: function() {
        return this.get( "mgmtURL" ) + this.name();
      },

      statisticsURL: function() {
        return sprintf( "%s/$/stats%s", this.baseURL(), this.name() );
      },

      name: function() {
        return this.get( "ds.name" );
      },

      services: function() {
        return this.get( "ds.services" );
      },

      countPerformed: function() {
        return this.get( "countPerformed" );
      },

      counts: function() {
        return this.get( "counts" );
      },

      serviceTypes: function() {
        return _.map( this.services(), function( s ) {return s["srv.type"];} );
      },

      /** Return a descriptive data-structure listing all this datasets services */
      servicesDescription: function() {
        var description = [];
        var self = this;

        _.each( this.services(), function( s ) {
          _.each( s["srv.endpoints"], function( e ) {
            description.push( {label: s["srv.description"],
                               url: self.datasetEndpointURL( e )
                              } );
          } );
        } );

        description.sort( function( d0, d1 ) {
          return (d0.label < d1.label) ? -1 : (d0.label > d1.label ? 1 : 0);
        } );

        return description;
      },

      /** Return the first service that has the given type */
      serviceOfType: function( serviceType ) {
        return _.find( this.services(), function( s ) {
          return s["srv.type"] === serviceType;
        } );
      },

      /** Return the first endpoint of the first service that has the given type */
      endpointOfType: function( serviceType ) {
        var service = this.serviceOfType( serviceType );
        return service && _.first( service["srv.endpoints"] );
      },

      /* Return URL for a service of a given type or null, if no such service */
      endpointURL: function( serviceType ) {
        var endpoint = this.endpointOfType( serviceType );
        return endpoint ? this.datasetEndpointURL( endpoint ) : null;
      },

      /** Return the URL for the given endpoint */
      datasetEndpointURL: function( endpoint ) {
        return sprintf( "%s%s/%s", this.baseURL(), this.name(), endpoint );
      },

      /** Return the sparql query URL for this dataset, if it has one, or null */
      queryURL: function() {
        return this.endpointURL( "Query" ) ;
      },

      /** Return the sparql query URL for this dataset, if it has one, or null */
      quadsURL: function() {
        return this.endpointURL( "Quads" ) ;
      },

      /** Return the sparql update URL for this dataset, if it has one, or null */
      updateURL: function() {
        return this.endpointURL( "Update" ) ;
      },

      /** Return the GSP write URL for this dataset, if it has one, or null */
      graphStoreProtocolURL: function() {
        if ( this.endpointURL( "GSP" ) )
            // Old name
            return this.endpointURL( "GSP" ) ;
        return this.endpointURL( "GSP_RW" ) ;
      },

      /** Return the GSP read URL for this dataset, if it has one, or null */
      graphStoreProtocolReadURL: function() {
        return this.endpointURL( "GSP_R" ) ;
      },

      /** Return the upload URL for this dataset, if it has one, or null */
      uploadURL: function( graphName ) {
        if (this.graphStoreProtocolURL() !== null) {
          return sprintf( "%s%s", this.graphStoreProtocolURL(), (graphName === "default" ? "" : ("?graph=" + graphName) ));
        }
        else {
          return null;
        }
      },

      /** Perform the action to delete the dataset. Returns the Ajax deferred object */
      deleteDataset: function() {
        return $.ajax( {
          url: this.mgmtActionURL(),
          type: 'DELETE'
        } );
      },

      /** Perform the action of taking a backup of this dataset */
      backupDataset: function() {
        var backupURL = sprintf( "%s/$/backup%s", this.baseURL(), this.name() );
        var ds = this;

        return $.ajax( {
          url: backupURL,
          type: 'POST'
        } )
          .done( function( taskDescription ) {
            new Task( ds, "backup", taskDescription );
          } );
      },

      /**
       * Request the statistics for this dataset, and return the promise object for the callback.
       * @param keep If truthy, and we have existing statistics, re-use the existing stats
       * */
      statistics: function( keep ) {
        var self = this;
        var currentStats = this.get( "statistics" );

        if (currentStats && keep) {
          return $.Deferred().resolveWith( null, currentStats );
        }
        else {
          return $.getJSON( this.statisticsURL() )
                  .then( function( data ) {
                    self.set( "statistics", data );
                    return data;
                  } );
        }
      },

      /** Perform a count query to determine the size of the dataset. Changes the size property when done,
       * but also returns the JQuery promise object used to monitor the query response */
      count: function() {
        var self = this;
        var query1 = sprintf( "select (count(*) as ?count) {?s ?p ?o}" );
        var query2 = sprintf( "select ?g (count(*) as ?count) {graph ?g {?s ?p ?o}} group by ?g" );

        self.set( "counting", true );

        var updateCount = function( model, result, graph ) {
          var n = parseInt( result.count.value );
          var counts = _.extend( {}, model.get( "counts" ) );
          counts[graph] = n;
          model.set( "counts", counts );
        };

        $.getJSON( sprintf( "%s?query=%s", self.queryURL(), query1 ) )
         .done( function( data ) {
           updateCount( self, data.results.bindings[0], "default graph" );

           $.getJSON( sprintf( "%s?query=%s", self.queryURL(), query2 ) )
            .done( function( data ) {
              _.each( data.results.bindings, function( binding ) {
                if (binding.g) {
                  updateCount( self, binding, binding.g.value );
                }
              } );
            } );

           self.set( {countPerformed: true, counting: false} );
         } );
      },

      /**
       * Fetch the content of the given graph as Turtle. Return the jQuery promise
       * object for the Ajax call.
       */
      fetchGraph: function( graphName ) {
        return $.ajax( this.graphStoreProtocolReadURL(),
                       {method: "get",
                        headers: {Accept : "text/turtle; charset=utf-8"},
                        dataType: "html",
                        data: {graph: graphName}
                       } );
      },

      /**
       * Put the given Turtle content back to the server using the given graph name
       */
      putGraph: function( turtle, graphName ) {
        return $.ajax( sprintf( "%s?graph=%s", this.graphStoreProtocolURL(), graphName ),
                       {method: "put",
                        dataType: "json",
                        data: turtle,
                        contentType: "text/turtle; charset=uft-8"
                       } );
      }

    } );

    return Dataset;
  }
);
