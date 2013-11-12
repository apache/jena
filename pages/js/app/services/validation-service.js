define( ['underscore', 'jquery', 'fui', 'codemirror/codemirror'],
  function( _, $, fui ) {

    var ValidationService = function( editor_el, output_el ) {
      this.editor_el = editor_el;
      this.output_el = output_el;
    };

    _.extend( ValidationService.prototype, {
      init: function() {
        this.editorElement();
        this.outputElement();
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
      outputElement: function() {
        if (!this._output) {
          this._output = new CodeMirror( $(this.output_el).get(0), {
            lineNumbers: true,
            mode: "text",
            readOnly: true
          } );
        }
        return this._output;
      },

      /** Return the current code editor contents */
      editorContent: function() {
        return this.editorElement().getValue();
      },

      /** Perform the given action to validate the current content */
      performValidation: function( optionsModel ) {
        var self = this;
        var content = {};
        content[optionsModel.payloadParam()] = this.editorContent();

        var options = {
            data: _.extend( optionsModel.toJSON(), content ),
            type: "POST"
        };

        $.ajax( optionsModel.validationURL(), options )
         .done( function( data ) {
           self.outputElement().setValue( data );
         } );
      }

    } );


    return ValidationService;
  }
);