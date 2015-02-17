/**
 * This view presents a control to upload files to the current dataset, and a recently-uploaded
 * log to track what has been done.
 */

define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        sprintf = require( "sprintf" ),
        fui = require( "app/fui" ),
        fileUploadTemplate = require( "plugins/text!app/templates/file-upload.tpl" ),
        UploadableFileView = require( "app/views/uploadable-file" );

    var FileUploadView = Backbone.Marionette.CompositeView.extend( {
      initialize: function(){
        _.bindAll( this,
                   "onUploadAdd", "onRemoveUpload", "onUploadAll",
                   "onProgress", "onUploadDone", "onUploadFail",
                   "onPerformUpload" );

        fui.vent.on( "upload.remove", this.onRemoveUpload );
        fui.vent.on( "upload.perform", this.onPerformUpload );
      },

      template: _.template( fileUploadTemplate ),

      el: "#file-upload",

      itemViewContainer: "ul",
      itemView: UploadableFileView,
      collection: new Backbone.Collection(),

      ui: {
        fileUpload: '#fileuploadForm',
        graphLabel: '.graph-label input'
      },

      events: {
        "click .action-upload-all": "onUploadAll"
      },

      templateHelpers: {
      },

      onRender: function() {
        // initialise the file upload widget
        this.ui.fileUpload.fileupload( {
          dataType: 'json',
          add: this.onUploadAdd,
          progress: this.onProgress
        } );
      },

      /** User has added a file */
      onUploadAdd: function( e, data ) {
        var collection = this.collection;
        var self = this;

        _.each( data.files, function( file ) {
          file.readableFileSize = self.readableFileSize( file );
          collection.add( new Backbone.Model( {file: file} ) );
        } );

        this.enableUploadAll( true );
      },

      /** Return file size in bytes in a human-readable form */
      readableFileSize: function( file ) {
        var k = 1024;
        var m = k * k;

        if (file.size >= m) {
          return sprintf( "%.1fmb", file.size / m );
        }
        else if (file.size >= k) {
          return sprintf( "%.1fkb", file.size / k );
        }
        else {
          return sprintf( "%d bytes", file.size );
        }
      },

      /** User has requested to remove a selected upload */
      onRemoveUpload: function( file ) {
        this.collection.remove( file );
        this.enableUploadAll( this.collection.size() > 0 )
      },

      /** User has requested to perform a selected upload */
      onPerformUpload: function( model ) {
        this.loadAll = false;
        this.uploadFileFromModel( model );
      },

      /** Return the list of active files waiting for upload */
      activeFiles: function() {
        var activeModels = _.filter( this.collection.models, function( m ) {
            return !m.completed;
          } );

        return _.map( activeModels,  function( m ) {
            return m.get( "file" );
          } );
      },

      /** User action to upload all active files */
      onUploadAll: function( e ) {
        if (e) {
          e.preventDefault();
        }

        this.$el.find( ".file-description .action" ).attr( 'disabled', 'disabled' );
        this.loadNextAvailableFile( true );
      },

      /** Load the next file in the sequence */
      loadNextAvailableFile: function( all ) {
        this.loadAll = all;
        var files = this.activeFiles();

        if (files.length > 0) {
          this.uploadFile( files.shift() );
        }
        else {
          this.enableUploadAll( false );
        }
      },

      /** Upload the given file to the server */
      uploadFile: function( file ) {
        this.uploadFileFromModel( this.collection.findWhere( {file: file} ) );
      },

      /** Upload the file attached to a given model */
      uploadFileFromModel: function( model ) {
        this.cacheModel( model );

        var file = model.get( "file" );
        var ds = fui.models.fusekiServer.selectedDataset();
        var url = ds.uploadURL( this.destinationGraphName() );

        this.ui.fileUpload.fileupload( 'send', {
          files: [file],
          url: url
        })
          .success( this.onUploadDone )
          .fail( this.onUploadFail );
      },

      /** Return the selected graph name, or 'default' */
      destinationGraphName: function() {
        var gName = this.ui.graphLabel.val();
        return (gName && gName !== "") ? gName : 'default';
      },

      /** Callback on progress against an upload */
      onProgress: function( e, data ) {
        var complete = Math.round( 100.0 * data.loaded/ data.total);
        $(this.activeView.el).find( ".progress-bar" )
                             .attr( 'aria-valuenow', complete )
                             .css( 'width', sprintf( "%s%%", complete ));
      },

      /** Callback on successful completion */
      onUploadDone: function( data, response ) {
        var label = "Data file was empty.";
        if ((data.count) > 0) {
          var s = (data.count === 1) ? "" : "s";
          label = sprintf( "%d %s%s", data.count, ((data.tripleCount > 0) ? "triple" : "quad"), s );
        }

        this.displayUploadResult( sprintf( "<p><small>Result: <strong>success</strong>. %s</small></p>", label ), "" );

        if (this.loadAll) {
          this.loadNextAvailableFile( true );
        }
      },

      /** Callback on error */
      onUploadFail: function( jqxhr, error, msg ) {
        $(this.activeView.el).find( ".progress-bar" )
                             .removeClass( "progress-bar-success" )
                             .addClass( "progress-bar-warning" );
        this.displayUploadResult( sprintf( "<p><small>Result: <strong>failed</strong> with message &quot;%s&quot;</small></p>", msg ), "text-danger" );

        if (this.loadAll) {
          this.loadNextAvailableFile( true );
        }
      },

      /** Show the result of uploading a file */
      displayUploadResult: function( html, cls ) {
        var el = $(this.activeView.el);
        this.activeModel.completed = true;
        el.find( ".action" ).hide();

        el.find( ".result" )
          .addClass( cls )
          .append( html );
      },

      /** Cache the currently active model so that we can attach actions to the corresponding view */
      cacheModel: function( model ) {
        this.activeModel = model;
        this.activeView = this.children.findByModel( model );
      },

      /** Enable or disable the upload all button */
      enableUploadAll: function( enabled ) {
        if (enabled) {
          $(".action-upload-all").removeAttr( 'disabled' );
        }
        else {
          $(".action-upload-all").attr( 'disabled', 'disabled' );
        }
      }


    });


    return FileUploadView;
  }
);
