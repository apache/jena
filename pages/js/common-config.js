require.config({
  baseUrl: 'js/app',
  paths: {
    'backbone':             '../lib/backbone',
    'backbone.babysitter':  '../lib/backbone.babysitter',
    'backbone.wreqr':       '../lib/backbone.wreqr',
    'jquery':               '../lib/jquery-1.10.2.min',
    'marionette':           '../lib/backbone.marionette',
    'underscore':           '../lib/underscore'
  },
  shim: {
    'underscore': {
      exports: '_'
    },
    'backbone': {
      depends: ['underscore', 'jquery']
    },
    'backbone.babysitter': {
      depends: ['backbone', 'marionette']
    },
    'backbone.wreqr': {
      depends: ['backbone', 'marionette']
    },
    'marionette': {
      depends: ['backbone'],
      exports: 'Marionette'
    }

  }
});

// define the modules as dependencies here, so that we avoid the dread
// 'module name X has not been loaded yet for context _'
define( ['underscore', 'backbone', 'jquery', 'marionette', 'backbone.wreqr'],
        function() {} );