/**
 * A long-running task, which periodically pings the server for its task status
 */

define(
  function( require ) {
    "use strict";

    var Marionette = require( "marionette" ),
        Backbone = require( "backbone" ),
        _ = require( "underscore" ),
        fui = require( "app/fui" ),
        sprintf = require( "sprintf" );

    /* Constants */

    var MS = 1000;
    var MAX_DELAY = 10 * MS;

    /**
     * This model represents a long running task process
     */
    var Task = function( ds, operationType, taskDescription ) {
      this.taskDescription = taskDescription;
      this.ds = ds;
      this.operationType = operationType;
      this.delay = 500;

      _.bindAll( this, "checkTaskStatus", "onCurrentTaskStatusFail", "onCurrentTaskStatus" );

      this.checkTaskStatus();
    };

    _.extend( Task.prototype, {
      /** Return the unique ID (on this server) of the task */
      taskId: function() {
        return this.taskDescription.taskId;
      },

      /** Return the URL for the task's API */
      taskURL: function() {
        return sprintf( "%s/$/tasks/%s", this.ds.baseURL(), this.taskId() );
      },

      /** Test the current status of the task */
      checkTaskStatus: function() {
        $.getJSON( this.taskURL() )
         .done( this.onCurrentTaskStatus )
         .fail( this.onCurrentTaskStatusFail )
      },

      /** Successful result from checking the task */
      onCurrentTaskStatus: function( taskDescription ) {
        this.taskDescription = taskDescription;

        var status = {
            task: this,
            dsId: this.ds.name(),
            finished: this.taskFinished()
        };

        fui.vent.trigger( "task.status", status );

        this.queueTaskStatusCheck();
      },

      /** Failed to check the task */
      onCurrentTaskStatusFail: function( jqxhr, msg, err ) {
        var status = {
            task: this,
            dsId: this.ds.name(),
            errorMessage: err || msg
        };

        fui.vent.trigger( "task.failed", status );
      },

      /** Re-queue the status check if the task is not yet complete */
      queueTaskStatusCheck: function() {
        if (!this.taskFinished()) {
          _.delay( this.checkTaskStatus, this.statusDelay() );
        }
      },

      /** Return the completion time if the task has been fid, otherwise null */
      taskFinished: function() {
        return this.taskDescription.finished;
      },

      /** Return the delay in ms until the next status check is due. */
      statusDelay: function() {
        var t = this.delay;

        if (t < MAX_DELAY) {
          this.delay = t * 2;
        }

        return t;
      }
    } );

    return Task;
  }
);

