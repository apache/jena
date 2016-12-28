/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package dev;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.seaborne.dboe.base.file.Location;
import org.seaborne.dboe.jenax.Txn;
import org.seaborne.tdb3.setup.TDB3Builder;
import org.seaborne.tdb3.storage.StorageTDB;
import projects.dsg2.DatasetGraphStorage;

public class DevTDB3 {
    //static { LogCtl.setLog4j() ; }
    static { LogCtl.setJavaLogging();}
    
    // Prefixes
    // Updates to DatasetGraphStorage etc. 
    
    public static void main(String[] args) {
        StorageTDB storage = TDB3Builder.build(Location.mem());
        DatasetGraph dsg = new DatasetGraphStorage(storage, storage);
        Dataset ds = DatasetFactory.wrap(dsg);
        Txn.executeWrite(ds, ()->{
            RDFDataMgr.read(ds, "D.trig");
        });
        
        // Prefixes.
        Txn.executeRead(ds, ()->{
            RDFDataMgr.write(System.out, ds, Lang.TRIG);
        });
        
    }

}
