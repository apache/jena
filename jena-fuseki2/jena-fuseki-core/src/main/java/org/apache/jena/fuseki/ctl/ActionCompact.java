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

package org.apache.jena.fuseki.ctl;

import static java.lang.String.format;

import java.util.function.Predicate;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.slf4j.Logger;

public class ActionCompact extends ActionAsyncTask
{
    public ActionCompact() { super("Compact"); }

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

        action.log.info(format("[%d] Compact dataset %s", action.id, name));

        CompactTask task = new CompactTask(action);
        if ( task.dataset == null ) {
            ServletOps.errorBadRequest("Dataset not found");
            return null;
        }

        DatasetGraph dsg = getTDB2(task.dataset);

        if ( dsg == null ) {
            ServletOps.errorBadRequest("Not a TDB2 dataset: Compact only applies to TDB2");
            return null;
        }
        return task;
    }

    // Unwrapping until the top of TDBS2, DatasetGraphSwitchable, is found.
    // This include a DatasetGraphText.

    /** Safety condition that stops further unwrapping */
    private static Predicate<DatasetGraph> notTDB2 =
        (dsg) -> org.apache.jena.tdb.sys.TDBInternal.isTDB1(dsg);

    private static DatasetGraph getTDB2(DatasetGraph dsg) {
        return unwrap(dsg, x -> TDBInternal.isTDB2(x), notTDB2);
    }

    private static DatasetGraph unwrap(DatasetGraph dsg, Predicate<DatasetGraph> predicate, Predicate<DatasetGraph> failPredicate) {
        for ( ;; ) {
            if ( failPredicate.test(dsg) )
                return null;
            if ( predicate.test(dsg) )
                return dsg;
            if ( ! ( dsg instanceof DatasetGraphWrapper dsgw ) )
                return null;
            dsg = dsgw.getWrapped();
        }
    }

    static class CompactTask extends TaskBase {
        static private final Logger log = Fuseki.compactLog;

        private final boolean shouldDeleteOld;

        public CompactTask(HttpAction action) {
            super(action);

            String deleteOldParam = action.getRequestParameter("deleteOld");

            this.shouldDeleteOld = ( deleteOldParam != null
                                     && ( deleteOldParam.isEmpty() || deleteOldParam.equalsIgnoreCase("true") ) );
        }

        @Override
        public void run() {
            try {
                DatasetGraph dsg = getTDB2(dataset);
                log.info(format("[%d] >>>> Start compact %s", actionId, datasetName));
                DatabaseMgr.compact(dsg, this.shouldDeleteOld);
                log.info(format("[%d] <<<< Finish compact %s", actionId, datasetName));
            } catch (Throwable ex) {
                log.warn(format("[%d] **** Exception in compact", actionId), ex);
                // Pass on - the async task tracking infrastructure will record this.
                throw ex;
            }
        }
    }
}
