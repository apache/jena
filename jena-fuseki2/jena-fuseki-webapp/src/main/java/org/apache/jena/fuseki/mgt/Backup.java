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

import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.webapp.FusekiWebapp;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;

/** Perform a backup */
public class Backup
{
    public static String chooseFileName(String dsName) {
        // Without the "/" - ie. a relative name.
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

    /** Perform a backup.
     *  A backup is a dump of the dataset in compressed N-Quads, done inside a transaction.
     */
    public static void backup(Transactional transactional, DatasetGraph dsg, String backupfile) {
        if ( transactional == null )
            transactional = new TransactionalNull();
        transactional.begin(ReadWrite.READ);
        try {
            Backup.backup(dsg, backupfile);
        } catch (Exception ex) {
            Log.warn(Fuseki.serverLog, "Exception in backup", ex);
        }
        finally {
            transactional.end();
        }
    }

    /** Perform a backup.
     *
     * @see #backup(Transactional, DatasetGraph, String)
     */
    private static void backup(DatasetGraph dsg, String backupfile) {
        if (dsg == null) {
            throw new FusekiException("No dataset provided to backup");
        }
        
        if ( !backupfile.endsWith(".nq") )
            backupfile = backupfile + ".nq";

        // Per backup source lock.
        synchronized(activeBackups) {
            // Atomically check-and-set
            if ( activeBackups.contains(dsg) )
                FmtLog.warn(Fuseki.serverLog, "Backup already in progress");
            activeBackups.add(dsg);
        }

        OutputStream out = null;
        try {

            if ( true ) {
                // This seems to achive about the same as "gzip -6"
                // It's not too expensive in elapsed time but it's not
                // zero cost. GZip, large buffer.
                out = new FileOutputStream(backupfile + ".gz");
                out = new GZIPOutputStream(out, 8 * 1024);
                out = new BufferedOutputStream(out);
            } else {
                out = new FileOutputStream(backupfile);
                out = new BufferedOutputStream(out);
            }
            RDFDataMgr.write(out, dsg, Lang.NQUADS);
            out.close();
            out = null;
        } catch (FileNotFoundException e) {
            FmtLog.warn(Fuseki.serverLog, "File not found: %s", backupfile);
            throw new FusekiException("File not found: " + backupfile);
        } catch (IOException e) {
            IO.exception(e);
        } finally {
            try {
                if ( out != null )
                    out.close();
            } catch (IOException e) { /* ignore */}
            // Remove lock.
            synchronized(activeBackups) {
                activeBackups.remove(dsg);
            }
        }
    }
}

