require.config({
    baseUrl: 'js/app',
    paths: {
      'backbone':             '../lib/backbone-min',
      'backbone.babysitter':  '../lib/backbone.babysitter',
      'backbone.wreqr':       '../lib/backbone.wreqr',
      'jquery':               '../lib/jquery-1.10.2.min',
      'marionette':           '../lib/backbone.marionette',
      'underscore':           '../lib/underscore'
    },
    shim: {
      'underscore': {
        exports: '_'
      }
    }
  },
});

// define the modules as dependencies here, so that we avoid the dread
// 'module name X has not been loaded yet for context _'
define( ['backbone', 'jquery', 'marionette', 'underscore'],
        function() {} );