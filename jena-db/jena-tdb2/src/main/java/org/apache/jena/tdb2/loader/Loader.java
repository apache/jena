/*
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

package org.apache.jena.tdb2.loader;

import org.apache.jena.atlas.lib.ProgressMonitor ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.apache.jena.system.Txn;
import org.apache.jena.query.Dataset ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.ProgressStreamRDF ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class Loader {
    
//    private static final int BATCH_SIZE = 100 ;
//    
//    // XXX StreamRDFBatchSplit and parallel index update.
    private static Logger LOG = LoggerFactory.getLogger("Loader") ;
    
    public static void bulkLoad(Dataset ds, String ... files) {
        DatasetGraphTDB dsg = TDBInternal.getDatasetGraphTDB(ds);
        StreamRDF s1 = StreamRDFLib.dataset(dsg) ;
        ProgressMonitor plog = ProgressMonitor.create(LOG, "Triples", 100000, 10) ;
        ProgressStreamRDF sMonitor = new ProgressStreamRDF(s1, plog) ;
        StreamRDF s3 = sMonitor ;

        plog.start(); 
        Txn.executeWrite(ds, () -> {
            for ( String fn : files ) {
                if ( files.length > 1 )
                    FmtLog.info(LOG, "File: %s",fn);
                RDFDataMgr.parse(s3, fn) ;
            }
        }) ;
        plog.finish();
        plog.finishMessage();
    }
    
//    public static void bulkLoadBatching(Dataset ds, String ... files) {
//        DatasetGraphTDB dsg = TDBInternal.getDatasetGraphTDB(ds);
//        StreamRDFBatchSplit s1 = new StreamRDFBatchSplit(dsg, 10) ;
//        ProgressMonitor plog = ProgressMonitor.create(LOG, "Triples", 100000, BATCH_SIZE) ;
//        // Want the monitor on the outside to capture transaction wrapper costs.
//        StreamRDF s3 = new ProgressStreamRDF(s1, plog) ;
//
//        plog.start(); 
//        Txn.executeWrite(ds, () -> {
//            for ( String fn : files ) {
//                if ( files.length > 1 )
//                    FmtLog.info(LOG, "File: %s",fn);
//                RDFDataMgr.parse(s3, fn) ;
//            }
//        }) ;
//        plog.finish();  
//        plog.finishMessage();
//    }
}
