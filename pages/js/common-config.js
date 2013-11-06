require.config({
  baseUrl: 'js/app',
  paths: {
    'backbone':             '../lib/backbone',
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
    'sprintf': {
      exports: 'sprintf'
    },
    'marionette': {
      deps: ['backbone'],
      exports: 'Marionette'
    }

  }
});
