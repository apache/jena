require.config({
  baseUrl: 'js/lib',
  paths: {
    'app':                  '../app',
    // lib paths
    'bootstrap':            'bootstrap.min',
    'jquery':               'jquery-1.10.2.min',
    'marionette':           'backbone.marionette',
    'sprintf':              'sprintf-0.7-beta1'
  },
  shim: {
    'underscore': {
      exports: '_'
    },
    'backbone': {
      deps: ['underscore', 'jquery'],
      exports: 'Backbone'
    },
    'bootstrap': {
      deps: ['jquery']
    },
    'bootstrap-select.min': {
      deps: ['bootstrap']
    },
    'jquery.xdomainrequest': {
      deps: ['jquery']
    },
    'jquery.dataTables.min': {
      deps: ['jquery']
    },
    'jquery.form': {
      deps: ['jquery']
    },
    'jquery.ui.widget': {
      deps: ['jquery']
    },
    'qonsole': {
      deps: ['addon/fold/brace-fold', 'addon/fold/comment-fold', 'addon/fold/foldgutter', 'addon/fold/xml-fold',
             'mode/javascript/javascript', 'mode/sparql/sparql', 'mode/xml/xml', 'jquery.dataTables.min',
             'remote-sparql-service'],
      exports: 'qonsole'
    },
    'jquery.fileupload': {
      deps: ['jquery.fileupload.local', 'jquery.iframe-transport', 'jquery.ui.widget']
    },
    'jquery.fileupload.local': {
      deps: ['jquery']
    },
    'jquery.iframe-transport': {
      deps: ['jquery']
    },
    'sprintf': {
      exports: 'sprintf'
    },
    'marionette': {
      deps: ['backbone'],
      exports: 'Marionette'
    },
    'lib/codemirror': {
      exports: 'CodeMirror'
    },
    'addon/fold/foldcode': {deps: ['lib/codemirror']},
    'addon/fold/brace-fold': {deps: ['addon/fold/foldcode']},
    'addon/fold/comment-fold': {deps: ['addon/fold/foldcode']},
    'addon/fold/foldgutter': {deps: ['addon/fold/foldcode']},
    'addon/fold/xml-fold': {deps: ['addon/fold/foldcode']},
    'mode/javascript/javascript': {deps: ['lib/codemirror']},
    'mode/sparql/sparql': {deps: ['lib/codemirror']},
    'mode/xml/xml': {deps: ['lib/codemirror']},
    'mode/turtle/turtle': {deps: ['lib/codemirror']}
  }
});
