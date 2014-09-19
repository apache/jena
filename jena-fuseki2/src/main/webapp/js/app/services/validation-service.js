define( ['underscore', 'jquery', 'fui',
         'lib/codemirror', 'mode/javascript/javascript', 'mode/sparql/sparql'],
  function( _, $, fui, CodeMirror ) {

    var ValidationService = function( editor_el, output_el ) {
      this.editor_el = editor_el;
      this.output_el = output_el;
    };

    _.extend( ValidationService.prototype, {
      init: function() {
        _.bindAll( this, "handleValidationOutput", "handleJsonValidationOutput" );
        this.editorElement();
      },

      /** Return the DOM node representing the query editor */
      editorElement: function() {
        if (!this._editor) {
          this._editor = new CodeMirror( $(this.editor_el).get(0), {
            lineNumbers: true,
            mode: "text"
          } );
        }
        return this._editor;
      },

      /** Return the DOM node representing the output editor */
      outputElement: function( mode, lineNumbers, data ) {
        $(this.output_el).empty();

        var cm = new CodeMirror( $(this.output_el).get(0), {
          lineNumbers: lineNumbers,
          mode: mode || "text",
          readOnly: true,
          value: data
        } );

        return cm;
      },

      /** Return the current code editor contents */
      editorContent: function() {
        return this.editorElement().getValue();
      },

      /** Perform the given action to validate the current content */
      performValidation: function( optionsModel ) {
        var context = {optionsModel: optionsModel};
        var self = this;

        var content = {};
        content[optionsModel.payloadParam()] = this.editorContent();

        var options = {
            data: _.extend( optionsModel.toJSON(), content ),
            type: "POST"
        };

        $.ajax( optionsModel.validationURL(), options )
         .done( function( data, status, xhr ) {
           self.handleValidationOutput( data, status, xhr, context );
         } );
      },

      /** Respond to validation output from the server */
      handleValidationOutput: function( data, status, xhr, context ) {
        var ct = xhr.getResponseHeader("content-type") || "";
        if (ct.match( /json/ )) {
          this.handleJsonValidationOutput( data, context );
        }
        else {
          // in HTML output, we look for a .error div
          var errors = $(data).find( "div.error" ).text();
          this.outputElement( "text", true, errors || "No warnings or errors reported." );
        }
      },

      handleJsonValidationOutput: function( json, context ) {
        var outputFormat = context.optionsModel.outputFormat();
        console.log( "output format = " + outputFormat );
        var jsonString = null;

        if (outputFormat && json[outputFormat]) {
          jsonString = json[outputFormat];
        }
        else {
          jsonString = JSON.stringify( json, null, '  ' );
        }

        this.outputElement( "application/json", false, jsonString );
      }

    } );


    return ValidationService;
  }
);