require.config({
    baseUrl: 'js/app',
    paths: {
        jquery: '../lib/jquery-1.10.2.min'
    }
});

// declare the dependencies here, so that we avoid the dread
// 'module name X has not been loaded yet for context _'
define( ['jquery'],
        function() {} );