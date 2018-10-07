define(
  function( require ) {
    var Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        datasetManagementViewTpl = require( "plugins/text!app/templates/dataset-management.tpl" );

    var DataManagementView = Backbone.Marionette.ItemView.extend( {

      initialize: function(){
        _.bindAll( this, "onRemoveDataset", "onConfirmAction",
                         "onDatasetRemoveSuccess", "onDatasetRemoveFail",
                         "onTaskStatus", "onTaskFailed", "cleanup" );

        this.listenTo( this.model, "change", this.onModelChange, this );

        fui.vent.on( "action.delete.confirm", this.onConfirmRemoveDataset, this );
        fui.vent.on( "action.backup.confirm", this.onConfirmBackupDataset, this );

        fui.vent.on( "task.status", this.onTaskStatus, this );
        fui.vent.on( "task.failed", this.onTaskFailed, this );
      },

      template: _.template( datasetManagementViewTpl ),

      ui: {
        actionConfirmModal: "#actionConfirmModal"
      },

      el: "#dataset-management",

      events: {
        "click .action.remove": "onRemoveDataset",
        "click .action.confirm": "onConfirmAction",
        "click .action.backup": "onBackupDataset"
      },

      templateHelpers: {
      },

      cleanup: function() {
        this.unbind();
        this.undelegateEvents();
        this.model.unbind( 'change', this.onModelChange, this ); 
        fui.vent.unbind( 'action.delete.confirm', this.onConfirmRemoveDataset, this );
      },

      /** If the model changes, update the summary */
      onModelChange: function( event ) {
         this.cleanup();
         this.render();
      },

      /** User has requested a dataset be removed */
      onRemoveDataset: function( e ) {
        e.preventDefault();
        var elem = $(e.currentTarget);
        var dsId = elem.data( "ds-id" );
        var msg = sprintf( "Are you sure you want to delete dataset <code>%s</code>? This action cannot be reversed.", dsId );

        this.showConfirmationModal( msg, dsId, "action.delete.confirm" );
      },

      /** User has requested a dataset be backed up */
      onBackupDataset: function( e ) {
        e.preventDefault();
        var elem = $(e.currentTarget);
        var dsId = elem.data( "ds-id" );
        var msg = sprintf( "Are you sure you want to create a backup of dataset <code>%s</code>? This action may take some time to complete", dsId );

        this.showConfirmationModal( msg, dsId, "action.backup.confirm" );
      },

      /** Show a generic modal confirmation */
      showConfirmationModal: function( msg, dsId, eventId ) {
        this.ui.actionConfirmModal
               .find( ".modal-body p" )
               .html( msg );

        this.ui.actionConfirmModal
               .find( ".action.confirm" )
               .data( "ds-id", dsId )
               .data( "event-id", eventId );

        this.clearFeedback();
        this.ui.actionConfirmModal.modal( 'show' );
      },

      /** Generic response to confirming the current modal dialogue */
      onConfirmAction: function( e ) {
        e.preventDefault();
        var elem = $(e.currentTarget);
        var dsId = elem.data( "ds-id" );
        var eventId = elem.data( "event-id" );

        //this.ui.actionConfirmModal.modal( 'hide' );
        $('.modal.in').modal('hide');
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();
        _.delay( function() {
          fui.vent.trigger( eventId, dsId );
        }, 100 );
      },

      /** User has confirmed that the dataset can be deleted */
      onConfirmRemoveDataset: function( dsId ) {
        var self = this;

        fui.models
           .fusekiServer
           .dataset( dsId )
           .deleteDataset()
           .done( function( data ) {self.onDatasetRemoveSuccess( data, dsId );} )
           .error( function( jqxhr, msg, err ) {self.onDatasetRemoveFail( jqxhr, msg, err, dsId );} );
      },

      /** Callback after successfully removing a dataset */
      onDatasetRemoveSuccess: function( data, dsId ) {
        this.model.loadServerDescription();
      },

      /** Removing the dataset did not work: notify the user */
      onDatasetRemoveFail: function( jqxhr, msg, err, dsId ) {
        this.feedbackArea( dsId )
            .html( sprintf( "<p class='text-warning'>Sorry, removing dataset %s did not work, because: '%s'</p>", dsId, err || msg ) );
      },

      /** User has confirmed backing up the dataset */
      onConfirmBackupDataset: function( dsId ) {
        var self = this;

        fui.models
           .fusekiServer
           .dataset( dsId )
           .backupDataset();
      },

      /** Remove any current feedback content */
      clearFeedback: function() {
        $(".action.feedback").empty();
      },

      /** Long running task has updated status */
      onTaskStatus: function( status ) {
        var task = status.task;
        var msg = sprintf( "<p>Task <strong>%s</strong> started at %s%s</p>",
                           task.operationType,
                           task.taskDescription.started,
                           status.finished ? sprintf( ", finished at %s", status.finished ) : " &ndash; ongoing" );

        this.feedbackArea( status.dsId )
            .html( msg );
      },

      /** Long running task has failed to start */
      onTaskFailed: function( status ) {
        this.feedbackArea( status.dsId )
            .html( sprintf( "<p class='text-danger'>Task %s failed: '%s'</p>", task.operationType, task.errorMessage ));
      },

      /** Find the feedback area for a particular dataset */
      feedbackArea: function( dsId ) {
        return $(sprintf( ".action[data-ds-id='%s']", dsId ) )
               .parent()
               .siblings(".action.feedback");
      }

    });


    return DataManagementView;
  }
);
