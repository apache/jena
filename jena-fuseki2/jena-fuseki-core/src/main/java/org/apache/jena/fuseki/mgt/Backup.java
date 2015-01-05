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

import java.io.* ;
import java.util.HashSet ;
import java.util.Set ;
import java.util.zip.GZIPOutputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Perform a backup */ 
public class Backup
{
    public static final String BackupArea = "backups" ;

    public static String chooseFileName(String dsName) {
        FileOps.ensureDir(BackupArea) ;
        final String ds = dsName.startsWith("/") ? dsName : "/" + dsName ;
    
        String timestamp = Utils.nowAsString("yyyy-MM-dd_HH-mm-ss") ;
        final String filename = BackupArea + ds + "_" + timestamp ;
        return filename ;
    }
    
    // Rcord of all backups so we don't attempt to backup the
    // same dataset multiple times at the same time. 
    private static Set<DatasetGraph> activeBackups = new HashSet<>() ;
    
    public static void backup(DatasetGraph dsg, String backupfile) {
        if ( !backupfile.endsWith(".nq") )
            backupfile = backupfile + ".nq" ;

        // Per backup source lock. 
        synchronized(activeBackups) {
            // Atomically check-and-set
            if ( activeBackups.contains(backupfile) )
                Log.warn(Fuseki.serverLog, "Backup already in progress") ;
            activeBackups.add(dsg) ;
        }

        OutputStream out = null ;
        try {
            
            if ( true ) {
                // This seems to achive about the same as "gzip -6"
                // It's not too expensive in elapsed time but it's not
                // zero cost. GZip, large buffer.
                out = new FileOutputStream(backupfile + ".gz") ;
                out = new GZIPOutputStream(out, 8 * 1024) ;
                out = new BufferedOutputStream(out) ;
            } else {
                out = new FileOutputStream(backupfile) ;
                out = new BufferedOutputStream(out) ;
            }

            RDFDataMgr.write(out, dsg, Lang.NQUADS) ;
            out.close() ;
            out = null ;
        } catch (FileNotFoundException e) {
            Log.warn(Fuseki.serverLog, "File not found: " + backupfile) ;
            throw new FusekiException("File not found: " + backupfile) ;
        } catch (IOException e) {
            IO.exception(e) ;
        } finally {
            try {
                if ( out != null )
                    out.close() ;
            } catch (IOException e) { /* ignore */}
            // Remove lock.
            synchronized(activeBackups) {
                activeBackups.remove(dsg) ;
            }
        }
    }
}

