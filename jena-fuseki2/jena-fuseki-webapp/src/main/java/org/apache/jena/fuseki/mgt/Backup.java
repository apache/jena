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

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.webapp.FusekiWebapp;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.system.Txn;

/** Perform a backup */
public class Backup
{
    public static String chooseFileName(String dsName) {
        // Without the "/" - i.e. a relative name.
        String ds = dsName;
        if ( ds.startsWith("/") )
            ds = ds.substring(1);
        if ( ds.contains("/") ) {
            Fuseki.adminLog.warn("Dataset name: weird format: "+dsName);
            // Some kind of fixup
            ds = ds.replace("/",  "_");
        }

        String timestamp = DateTimeUtils.nowAsString("yyyy-MM-dd_HH-mm-ss");
        String filename = ds + "_" + timestamp;
        filename = FusekiWebapp.dirBackups.resolve(filename).toString();
        return filename;
    }

    // Record of all backups so we don't attempt to backup the
    // same dataset multiple times at the same time.
    private static Set<DatasetGraph> activeBackups = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Perform a backup.
     * <p>
     * A backup is a dump of the dataset in compressed N-Quads, done inside a transaction.
     */
    public static void backup(Transactional transactional, DatasetGraph dsg, String backupfile) {
        if ( transactional == null )
            transactional = new TransactionalNull();
        Txn.executeRead(transactional, ()->backup(dsg, backupfile));
    }

    // This seems to achieve about the same as "gzip -6"
    // It's not too expensive in elapsed time but it's not
    // zero cost. GZip, large buffer.
    private static final boolean USE_GZIP = true;

    /**
     * Perform a backup.
     *
     * @see #backup(Transactional, DatasetGraph, String)
     */

    private static void backup(DatasetGraph dsg, String backupfile) {
        if (dsg == null) {
            throw new FusekiException("No dataset provided to backup");
        }

        // Per backup source lock.
        synchronized(activeBackups) {
            // Atomically check-and-set
            if ( activeBackups.contains(dsg) )
                FmtLog.warn(Fuseki.serverLog, "Backup already in progress");
            activeBackups.add(dsg);
        }

        if ( !backupfile.endsWith(".nq") )
            backupfile = backupfile + ".nq";

        if ( USE_GZIP )
            backupfile = backupfile + ".gz";

        try {
            IOX.safeWrite(Path.of(backupfile), outfile -> {
                OutputStream out = outfile;
                if ( USE_GZIP )
                    out = new GZIPOutputStream(outfile, 8 * 1024);
                try (OutputStream out2 = new BufferedOutputStream(out)) {
                    RDFDataMgr.write(out2, dsg, Lang.NQUADS);
                }
            });
        } finally {
            // Remove lock.
            synchronized(activeBackups) {
                activeBackups.remove(dsg);
            }
        }
    }
}

