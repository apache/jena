require.config({
  baseUrl: 'js/app',
  paths: {
    'plugins':              '../lib/plugins',
    'lib':                  '../lib',
    'codemirror':           '../lib/codemirror',
    // lib paths
    'backbone':             '../lib/backbone',
    'bootstrap':            '../lib/bootstrap.min',
    'jquery':               '../lib/jquery-1.10.2.min',
    'marionette':           '../lib/backbone.marionette',
    'sprintf':              '../lib/sprintf-0.7-beta1',
    'underscore':           '../lib/underscore'
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
    'lib/jquery.xdomainrequest': {
      deps: ['jquery']
    },
    'lib/jquery.dataTables.min': {
      deps: ['jquery']
    },
    'lib/qonsole': {
      deps: ['codemirror/brace-fold', 'codemirror/comment-fold', 'codemirror/foldgutter', 'codemirror/xml-fold',
             'codemirror/javascript', 'codemirror/sparql', 'codemirror/xml', 'lib/jquery.dataTables.min' ],
      exports: 'qonsole'
    },
    'sprintf': {
      exports: 'sprintf'
    },
    'marionette': {
      deps: ['backbone'],
      exports: 'Marionette'
    },
    'codemirror/codemirror': {
      exports: 'CodeMirror'
    },
    'codemirror/foldcode': {deps: ['codemirror/codemirror']},
    'codemirror/brace-fold': {deps: ['codemirror/foldcode']},
    'codemirror/comment-fold': {deps: ['codemirror/foldcode']},
    'codemirror/foldgutter': {deps: ['codemirror/foldcode']},
    'codemirror/xml-fold': {deps: ['codemirror/foldcode']},
    'codemirror/javascript': {deps: ['codemirror/codemirror']},
    'codemirror/sparql': {deps: ['codemirror/codemirror']},
    'codemirror/xml': {deps: ['codemirror/codemirror']},
  }
});
