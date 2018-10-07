define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" );

    var DatasetDropDownListView = Backbone.Marionette.ItemView.extend( {
      initialize: function(){
      },

      template:"",

      el: "ul.dropdown-menu.dataset-list",

      ui: {
      },

      events: {
      },

      render: function() {
        var e = $(this.el).empty();
        _.each( this.model, function( ds ) {
          e.append( sprintf( "<li><a class='action select-dataset'href='?ds=%s'>%s</a></li>", ds.name(), ds.name() ));
        } );
      },

      /** Change the currently selected dataset name. If required, notify other units via an event */
      setCurrentDatasetName: function( dsName, notify ) {
        if (dsName) {
          $(".current-dataset").text( dsName );
        }

        if (notify) {
          fui.vent.trigger( "views.datasets-dropdown-list.dataset-changed", dsName )
        }
      }
    });


    return DatasetDropDownListView;
  }
);
