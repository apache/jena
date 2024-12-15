/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.fuseki.mgt;

import static java.lang.String.format;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.ctl.ActionAsyncTask;
import org.apache.jena.fuseki.ctl.TaskBase;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.slf4j.Logger;

public class ActionBackup extends ActionAsyncTask
{
    public ActionBackup() { super("Backup"); }

    @Override
    public void validate(HttpAction action) {}

    @Override
    protected Runnable createRunnable(HttpAction action) {
        String name = getItemName(action);
        if ( name == null ) {
            action.log.error("Null for dataset name in item request");
            ServletOps.errorOccurred("Null for dataset name in item request");
            return null;
        }

        action.log.info(format("[%d] Backup dataset %s", action.id, name));
        // ** Error changing in TaskBase
        BackupTask task = new BackupTask(action);
        if ( task.dataset == null ) {
            ServletOps.errorBadRequest("Dataset not found");
            return null;
        }
        return task;
    }

    static class BackupTask extends TaskBase {
        static private Logger log = Fuseki.backupLog;

        public BackupTask(HttpAction action) {
            super(action);
        }

        @Override
        public void run() {
            try {
                String backupFilename = Backup.chooseFileName(datasetName);
                log.info(format("[%d] >>>> Start backup %s -> %s", actionId, datasetName, backupFilename));
                Backup.backup(transactional, dataset, backupFilename);
                log.info(format("[%d] <<<< Finish backup %s -> %s", actionId, datasetName, backupFilename));
            } catch (Throwable ex) {
                log.warn(format("[%d] **** Exception in backup", actionId), ex);
                // Pass on - the async task tracking infrastructure will record this.
                throw ex;
            }
        }
    }
}
