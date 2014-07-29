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

package com.hp.hpl.jena.tdb;

import java.io.* ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxn ;

/**
 * Backup a database.
 */

public class TDBBackup
{
    public static void backup(Location location, String backupfile)
    {
        try(OutputStream out = new BufferedOutputStream(new FileOutputStream(backupfile))) {
            backup(location, out) ;
        } 
        catch (FileNotFoundException e)
        {
            Log.warn(TDBBackup.class, "File not found: "+backupfile) ;
            throw new TDBException("File not found: "+backupfile) ;
        } 
        catch (IOException e)
        { IO.exception(e) ; }
        
    }
    
    public static void backup(Location location, OutputStream backupfile)
    {
        Dataset ds = TDBFactory.createDataset(location) ;
        StoreConnection sConn = StoreConnection.make(location) ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.READ, "backup") ;
        RDFDataMgr.write(backupfile, dsg, Lang.NQUADS) ;
        dsg.end();
    }
}

