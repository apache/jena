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
      depends: ['underscore', 'jquery'],
      exports: 'Backbone'
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
