<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<template>
  <component
    :is="tag"
    ref="editor"
    class="vue-codemirror font-fixed-width"
  >
    <aside
      v-if="$slots.default"
      style="display: none;"
      aria-hidden
    >
      <slot name="default" />
    </aside>
  </component>
</template>

<script>
// CodeMirror
import { basicSetup, minimalSetup } from 'codemirror';
import { EditorSelection, EditorState, StateEffect } from '@codemirror/state';
import { EditorView, keymap, lineNumbers, showPanel } from "@codemirror/view";
import { indentWithTab, history } from '@codemirror/commands';
import { forceLinting, linter, lintGutter } from '@codemirror/lint';
import { nextTick } from 'vue';

//import useAppTheme from '@/lib/stateTheme';

// Load Theme Manager...
//const { isThemeDark } = useAppTheme();

export default {
  name: 'CodeMirror',

  props: {
    strValue: {
      type: String,
      required: true,
    },
    bAllowUpdate: {
      type: Boolean,
      required: true,
    },
    cmExtender: {
      type: Object,
      required: true,
      /*
        () => {
          return {
            theme: {},              // Object
            dark: false,            // Boolean
            basic: false,           // Boolean
            minimal: false,         // Boolean
            wrap: false,            // Boolean
            tab: false,             // Boolean
            tabSize: null,          // Number
            lineSeparator: null,    // String
            readonly: false,        // Boolean
            disabled: false,        // Boolean
            extensions: [],         // Array of Extensions
            lang: undefined,        // Object
            linter: undefined,      // Function
            linterConfig: {},       // Object
            lineNumbers: false,     // Boolean
            lineNumbersConfig: {},  // Object
            history: false,         // Boolean
            historyConfig: null,    // Object
            gutter: false,          // Boolean
            gutterConfig: {},       // Object
            panel: false,           // Boolean
            panelFunc: null,        // Function
          };
        },
    */
    },
    tag: {
      type: String,
      default: 'div',
    },
  },

  //emits: ['ready', 'update-value', 'update-view', 'focus', 'change', 'destroy'],
  emits: ['ready', 'update-value', 'destroy'],

  data() {
    return {
      isLoaded: false,
      editor: this.$refs.editor,
      // CodeMirror Editor View
      view: null,
      cmExtenderDefaults: {
        theme: {},              // Object
        dark: false, //isThemeDark(),    // Boolean
        basic: false,           // Boolean
        minimal: false,         // Boolean
        wrap: false,            // Boolean
        tab: false,             // Boolean
        tabSize: null,          // Number
        lineSeparator: null,    // String
        readonly: false,        // Boolean
        disabled: false,        // Boolean
        lang: null,             // Object
        linter: null,           // Function
        linterConfig: null,     // Object
        lineNumbers: false,     // Boolean
        lineNumbersConfig: null,// Object
        history: false,         // Boolean
        historyConfig: null,    // Object
        gutter: false,          // Boolean
        gutterConfig: null,     // Object
        panel: false,           // Boolean
        panelFunc: null,        // Function
        extensions: [],         // Array of Extensions
      },
      cmExtenderLocal: { ...this.cmExtenderDefaults, ...this.cmExtender },
    }
  },

  computed: {
    // Editor Selection
    selection: {
      get() { return (this.view?.state?.selection || null); },
      set(value) { this.view.dispatch( { selection: value } ); }
    },

    // Editor State
    state: {
      get() { return (this.view?.state || null); },
      set(value) { this.view.setState(value); }
    },

    // Cursor Position
    cursor: {
      get() { return (this.view?.state?.selection?.main?.head || 0); },
      set(value) { this.view.dispatch( { selection: { anchor: value } } ); }
    },

    // Focus
    focus: {
      get() { return (this.view?.hasFocus || false) },
      set(bFocus) {
        if (bFocus) {
          this.view.focus();
        }
      }
    },

    // Text length
    length() { return (this.view?.state?.doc?.length || 0); },

    // Get CodeMirror Extensions...
    extAll() {
      let exts = [];
      if (this.cmExtender) {
        let objExt = this.cmExtenderLocal;
        /*
        console.debug("CM: { " +
          "basic: " + objExt.basic + ", " +
          "dark: " + objExt.dark + ", " +
          "wrap: " + objExt.wrap + ", " +
          "tab: " + objExt.tab + ", " +
          "tabSize: " + objExt.tabSize + ", " +
          "lang: " + objExt.lang + ", " +
          "linter: " + objExt.linter + ", " +
          "lineNumbers: " + objExt.lineNumbers + ", " +
          "gutter: " + objExt.gutter + ", " +
          "panel: " + objExt.panel + ", " +
          "panelFunc: " + objExt.panelFunc );
        */
        /**
         * Extensions from props...
         */
        // Toggle basic setup...
        if (objExt.basic) exts.push(basicSetup);
        // Toggle minimal setup...
        else if (objExt.minimal) exts.push(minimalSetup);
        //// Set ViewUpdate event listener...
        //exts.push(
        //  EditorView.updateListener.of(
        //    (viewUpdate) => this.$emit('update-view', viewUpdate)
        //  )
        //);
        // Toggle light / dark mode...
        exts.push(
          EditorView.theme( objExt.theme, { dark: objExt.dark } ) );
        // Toggle line wrapping...
        if (objExt.wrap) exts.push(EditorView.lineWrapping);
        // Set Indent with tab...
        if (objExt.tab) exts.push( keymap.of([indentWithTab]) );
        // Set Indent tab size...
        if (objExt.tabSize) exts.push( EditorState.tabSize.of(objExt.tabSize) );
        // Set Readonly option...
        exts.push( EditorState.readOnly.of(objExt.readonly) );
        // Set Editable option...
        exts.push( EditorView.editable.of(!objExt.disabled) );
        // Set Line Break char...
        if (objExt.lineSeparator) exts.push( EditorState.lineSeparator.of(objExt.lineSeparator) );
        // Set Language...
        if (objExt.lang) exts.push(objExt.lang);
        // Set Line Numbers Gutter...
        if (objExt.lineNumbers) {
          if (objExt.lineNumbersConfig) exts.push( lineNumbers(objExt.lineNumbersConfig) );
          else exts.push( lineNumbers() );
        }
        if (objExt.history) {
          if (objExt.historyConfig) exts.push( history(objExt.historyConfig) );
          else exts.push( history() );
        }
        // Set Linter settings...
        if (objExt.linter) {
          if (objExt.linterConfig) exts.push( linter( objExt.linter(), objExt.linterConfig ) );
          else exts.push( linter( objExt.linter() ) );
        }
        // Show 🔴 to error line when linter enabled...
        if (objExt.linter && objExt.gutter) {
          if (objExt.gutterConfig) exts.push( lintGutter(objExt.gutterConfig) );
          else exts.push( lintGutter() );
        }
        // Set Panel Function...
        if (objExt.panel) exts.push( showPanel.of(objExt.panelFunc) );

        // Append other extensions...
        // TODO: Ignore previous extension was not changed. Requires a diff.
        if (  objExt.extensions && Array.isArray(objExt.extensions) && objExt.extensions.length > 0 )
          exts.push( ...objExt.extensions );
      }
      return exts;
    }
  },

  watch: {
    strValue(valueNew, valueOld) {
      if ( ! this.bAllowUpdate ) return;
      if ( this.view.composing ) return;
      if ( valueNew === valueOld ) return;

      let trans = {
        changes: [
            { from: 0, to: this.view.state.doc.length },
            { from: 0, insert: valueNew },
          ],
          selection: { anchor: 0 }, //this.view.state.selection
          scrollIntoView: true
      }
      this.view.dispatch(trans);
    },

    cmExtender: {
      deep: true,
      handler(objCMExtender) {
        this.cmExtenderLocal = { ...this.cmExtenderDefaults, ...this.cmExtender };
        if ( ! objCMExtender ) return;
        if (this.isLoaded) {
          this.isLoaded = false;
          this.view.dispatch( { effects: StateEffect.reconfigure.of(this.extAll) } );
          this.isLoaded = true;
        }
        else {
          this.initialize();
        }
      }
    },

    //focus(isFocus) {
    //  this.$emit('focus', isFocus);
    //},
  },

  async mounted() {
    if ( ! this.cmExtender ) return;
    this.editor = this.$refs.editor;
    await this.initialize();
  },

  unmounted() {
    this.removeView();
  },

  methods: {
    async initialize(){
      if ( ! this.cmExtender ) return;
      this.isLoaded = false;

      let strValueText = this.strValue;
      if (this.editor && this.editor.childNodes[0] && this.editor.childNodes[0].innerText) {
        // Overwrite given value when an existing display value is present...
        let strInner = String( this.editor.childNodes[0].innerText ).trim();
        if (strInner) {
          strValueText = strInner;
        }
      }

      // Register CodeMirror...
      this.removeView();
      this.view = new EditorView(
        { doc: strValueText,
          selection: EditorSelection.cursor(0),
          extensions: this.extAll,
          parent: this.editor,
          dispatchTransactions: this.dispatcher,
          scrollIntoView: true,
        }
      );

      await nextTick();
      this.$emit('ready',
        { view: this.view,
          //state: this.view.state, // ...unused
          //container: this.editor, // ...unused
        }
      );

      this.isLoaded = true;
    },

    dispatcher(aTransacts, view) {
      //this.view.disp.update([objTransact]);
      //this.view.dispatch(objTransact);
      view.update(aTransacts);

      let bTest = aTransacts.some(
        (objTransact) => {
          // TODO: Emit lint error event
          // console.debug("CodeMirror : dispatcher() : Transaction: ", objTransact);
          return ( objTransact?.changes && objTransact.changes );
        }
      );
      if (bTest) {
          // console.debug( "CodeMirror : dispatcher() : Doc Text: ", view.state.doc.toString() );
          // Pass text up to parent...
          this.$emit( 'update-value', view.state.doc.toString() );
          //this.$emit('change', view.state);
      }
    },

    removeView() {
      if (this.view) {
        this.view.destroy();
        this.$emit('destroy');
      }
      this.view = null;
    },

    // Forces any linters configured to run when the view is idle to run right away.
    lint() {
      if (this.cmExtenderLocal.linter) {
        forceLinting(this.view);
      }
    },

    // Force Reconfigure Extension
    forceReconfigure() {
      // Deconfigure all Extensions...
      this.view.dispatch( { effects: StateEffect.reconfigure.of([]) } );
      // Register extensions...
      this.view.dispatch( { effects: StateEffect.appendConfig.of(this.extAll) } );
    },

    /**
     * Get the text between the given points in the view
     *
     * @param from - start line number
     * @param to - end line number
     */
    getRange(from, to) {
      return this.view.state.sliceDoc(from, to);
    },
    /**
     * Get the content of line
     *
     * @param number - line number
     */
    getLine(number) {
      return this.view.state.doc.line(number + 1).text;
    },
    /**
     * Get the number of lines in the view
     */
    lineCount() {
      return this.view.state.doc.lines;
    },
    /**
     * Get the cursor position.
     */
     getCursor() {
      return this.cursor;
    },
    /**
     * Retrieves a list of all current selections
     */
    listSelections() {
      return this.view.state.selection.ranges;
    },
    /**
     * Get the currently selected text
     */
    getSelection() {
      return this.view.state.sliceDoc(
        this.view.state.selection.main.from,
        this.view.state.selection.main.to
      );
    },
    /**
     * The length of the given array should be the same as the number of active selections.
     * Replaces the content of the selections with the strings in the array.
     */
    getSelections() {
      return this.view.state.selection.ranges.map(
        (range) => { return this.view.state.sliceDoc(range.from, range.to); }
      );
    },
    /**
     * Return true if any text is selected
     */
    isSelected() {
      return this.view.state.selection.ranges.some(
        (range) => { return !range.empty; }
      );
    },

    /**
     * Replace the part of the document between from and to with the given string.
     *
     * @param replacement - replacement text
     * @param from - start string at position
     * @param to -  insert the string at position
     */
    replaceRange(replacement, from, to) {
      this.view.dispatch(
        { changes: { from, to, insert: replacement } }
      );
    },
    /**
     * Replace the selection(s) with the given string.
     * By default, the new selection ends up after the inserted text.
     *
     * @param replacement - replacement text
     */
    replaceSelection(replacement) {
      this.view.dispatch( this.view.state.replaceSelection(replacement) );
    },
    /**
     * Set the cursor position.
     *
     * @param position - position.
     */
    setCursor(position) {
      this.cursor = position;
      return this.cursor;
    },
    /**
     * Set a single selection range.
     *
     * @param from - start position
     * @param to - end position
     */
    setSelection(from, to) {
      this.view.dispatch( { selection: { from, to } } );
    },
    /**
     * Sets a new set of selections. There must be at least one selection in the given array.
     *
     * @param ranges - Selection range
     * @param primary - Primary selection
     */
    setSelections(ranges, primary = undefined ) {
      this.view.dispatch(
        { selection: EditorSelection.create(ranges, primary) }
      );
    },
    /**
     * Applies the given function to all existing selections, and calls extendSelections on the result.
     *
     * @param func - Function
     */
    extendSelectionsBy(/** @type { Function } */ func) {
      this.view.dispatch(
        { selection:
            EditorSelection.create(
              this.selection.ranges.map(
                (range) => range.extend( func(range) )
              )
            )
        }
      );
    },
  },

  /**
   * Render the template:
   *
   * <template>
   *   <sometag ref="editor" class="vue-codemirror">
   *     <aside
   *       v-if="this.$slots.default"
   *       style='display: none;'
   *       aria-hidden
   *     >
   *       <slot />
   *     </aside>
   *   </sometag>
   * </template>
   *
   * where <sometag> is dynamically determined from the parent;
   *     defaults to <div>,
   *//*
   render() {
    // function h ( Tag, Children ) | ( Tag, Data, Children )
    let tagData = { ref: 'editor', class: 'vue-codemirror' };
    let tagChild = undefined;
    if ( this.$slots.default ) {
      let asideData = { style: 'display: none;', 'aria-hidden': true };
      let asideChild =
        ( typeof this.$slots.default == 'function' )
        ? this.$slots.default()
        : this.$slots.default;
      tagChild = h( 'aside', asideData, asideChild );
      h()
    }
    return h( this.tag, tagData, tagChild );
  },
  */

  /**
   * Export component properties
   */
  expose: [
    // props...
    'editor',
    // computed...
    'selection',
    'state',
    'cursor',
    'focus',
    'length',
    // methods...
    'lint',
    'forceReconfigure',
    'getRange',
    'getLine',
    'lineCount',
    'getCursor',
    'listSelections',
    'getSelection',
    'getSelections',
    'isSelected',
    'replaceRange',
    'replaceSelection',
    'setCursor',
    'setSelection',
    'setSelections',
    'extendSelectionsBy',
  ]
};
</script>