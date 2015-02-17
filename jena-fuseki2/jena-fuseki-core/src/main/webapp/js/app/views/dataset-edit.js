/** Component for showing detailed information about a dataset */

define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        DatasetEditTpl = require( "plugins/text!app/templates/dataset-edit.tpl" ),
        CodeMirror = require( "lib/codemirror" ),
        CodeMirrorTurtle = require( "mode/turtle/turtle" );

    var MAX_EDITABLE_SIZE = 10000;

    var DatasetEdit = Backbone.Marionette.ItemView.extend( {

      initialize: function() {
        _.bindAll( this, "onCountGraphs", "onModelChanged", "onSelectDataset",
                         "onShownTab", "onShownEditTab", "onGraphContent",
                         "onSaveEdit", "onCancelEdit" );

        this.model.on( "change", this.onModelChanged );
        this._editor = null;

        fui.vent.on( "shown.bs.tab", this.onShownTab );
      },

      template: _.template( DatasetEditTpl ),

      ui: {
        listGraphs: ".action.list-graphs",
        editor: "#graph-editor",
        graphName: "input.graph-name",
        saveButton: "button.save-edit",
        cancelButton: "button.cancel-edit"
      },

      el: "#edit .with-dataset",

      events: {
        "click .list-graphs": "onCountGraphs",
        "click .select-dataset": "onSelectDataset",
        "click .save-edit": "onSaveEdit",
        "click .cancel-edit": "onCancelEdit"
      },

      templateHelpers: {
      },

      serializeData: function() {
        return this.model;
      },

      /** Alias for the model */
      dataset: function() {
        return this.model;
      },

      // event handlers

      onModelChanged: function() {
        if (!this.model.counting) {
          this.render();
        }
      },

      onCountGraphs: function( e ) {
        e.preventDefault();
        this.model.count();
      },

      /** Event that triggers when any tab is shown */
      onShownTab: function( tab ) {
        if (tab.attr("href") === "#edit") {
          this.onShownEditTab();
        }
      },

      /** When the tab is show, ensure the editor element is initialised */
      onShownEditTab: function() {
        this.showEditor();
      },

      /** When rendering, only show the code mirror editor if the tab is visible */
      onRender: function() {
        this.showEditor();
      },

      /** Ensure the code mirror element is visible */
      showEditor: function() {
        if (this.editorElementVisible() && this.editorNotYetInitialised()) {
          this._editor = null;
          this.editorElement();
        }
      },

      /** Return true if the editor container element is visible */
      editorElementVisible: function()  {
        return this.ui.editor.is( ":visible" );
      },

      /** Return true if the CodeMirror element has not yet been initialised */
      editorNotYetInitialised: function() {
        return this.ui.editor.is( ":not(:has(.CodeMirror))" );
      },

      /** User has (attempted to) select a dataset */
      onSelectDataset: function( e ) {
        e.preventDefault();
        var self = this;
        var elem = $(e.currentTarget);
        var graphName = elem.data( "graph-name" );
        var graphSize = parseInt( elem.data( "graph-size" ));

        if (graphSize > MAX_EDITABLE_SIZE) {
          alert( "Sorry, that dataset is too large to load into the editor" );
        }
        else {
          if (this.dirtyCheck()) {
            $(".nav.graphs").find( ".active" ).removeClass( "active" );
            elem.parent().addClass( "active" );

            var gn = this.setGraphName( graphName );
            this.dataset()
                .fetchGraph( gn )
                .done( self.onGraphContent );
          }
        }
      },

      /** Return true if the edit buffer is not dirty, or if the user says OK */
      dirtyCheck: function() {
        return true; // TODO
      },

      /** Return the DOM node representing the query editor */
      editorElement: function() {
        if (!this._editor) {
          this._editor = new CodeMirror( this.ui.editor.get(0), {
            lineNumbers: true,
            mode: "turtle"
          } );
        }
        return this._editor;
      },

      /** Set the graph name, return the actual name used */
      setGraphName: function( name ) {
        var text = (name === "default" || name === "default graph") ? "default" : name;

        this.ui.graphName.val( text );

        return text;
      },

      /** Get the graph name */
      graphName: function() {
        return this.ui.graphName.val();
      },

      /** Server has sent the content of the graph encoded as turtle */
      onGraphContent: function( data ) {
        this.editorElement().setValue( data );
      },

      /** User wants to save changes */
      onSaveEdit: function( e ) {
        e.preventDefault();
        var self = this;

        var turtle = this.editorElement().getValue();
        this.dataset()
            .putGraph( turtle, this.graphName() )
            .done( function( data ) {
              var nq = parseInt( data.quadCount );
              var nt = parseInt( data.tripleCount );
              var typ = (nq > nt) ? "quad" : "triple";
              var s = (nq + nt) === 1 ? "" : "s";
              var msg = sprintf( "Added %d %s%s", nq + nt, typ, s );

              self.showFeedback( msg, "" );
            } )
            .error( function( jqhxr, msg, err ) {
              self.showFeedback( err || msg, "text-warning" );
            } );
      },

      /** User wants to discard changes */
      onCancelEdit: function( e ) {
        e.preventDefault();
        this.ui.graphName.val( "" );
        this.editorElement().setValue( "" );
      },

      /** Show feedback from operations */
      showFeedback: function( msg, cls ) {
        $(".feedback").html( sprintf( "<span class='%s'>%s</span>", cls, msg ) );
      }


    });


    return DatasetEdit;
  }
);
