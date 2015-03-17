require.config({
  baseUrl: 'js/lib',
  paths: {
    'app':                  '../app',
    // lib paths
    'bootstrap':            'bootstrap.min',
    'jquery':               'jquery-1.10.2.min',
    'marionette':           'backbone.marionette',
    'sprintf':              'sprintf-0.7-beta1',
    'datatables':           'jquery.dataTables.min',
    'yasqe':                'yasqe.min',
    'yasr':                 'yasr.min',
    'pivottable':           'pivot.min',
    'jquery-ui':            'jquery-ui.min'
  },
  map: {
      '*': {
          'codemirror': 'lib/codemirror',
          'jquery.dataTables.min' : 'datatables',
          'jquery-ui': 'jquery-ui'
      },
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
      deps: ['yasqe', 'yasr'],
      exports: 'qonsole'
    },
    'yasqe': {
      deps: ['jquery', 'lib/codemirror'],
      exports: 'YASQE'
    },
    'yasr': {
//        deps: ['pivottable', 'jquery', 'lib/codemirror', 'datatables'],
        deps: ['jquery', 'lib/codemirror', 'datatables'],
        exports: 'YASR'
    },
    'pivottable': {
        deps: ['jquery-ui']
    },
    'jquery-ui': {
        deps: ['jquery']
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
