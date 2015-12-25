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

package org.seaborne.tdb2.junit;

import java.util.function.Consumer ;

import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.ConfigTest ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.sys.StoreConnection ;
import org.seaborne.tdb2.sys.SystemTDB ;

/** Execute a test with a fresh dataset in a write transaction */
public class TL {
    
    public static void exec(Consumer<Dataset> action) {
        Dataset dataset = createTestDataset() ;
        action.accept(dataset);
        releaseDataset(dataset) ;
        StoreConnection.reset() ;
    }
    
    public static void execMem(Consumer<Dataset> action) {
        Dataset dataset = createTestDatasetMem() ;
        action.accept(dataset);
        releaseDataset(dataset) ;
        StoreConnection.reset() ;
    }

    // Or use these for @Before, @After style.
    
    public static Location cleanLocation() {
        // To avoid the problems on MS Windows where memory mapped files
        // can't be deleted from a running JVM, we use a different, cleaned 
        // directory each time.
        String dirname = ConfigTest.getCleanDir() ;
        Location location = Location.create(dirname) ;
        return location ;
    }
    
    public static void releaseDataset(Dataset dataset) {
        dataset.abort() ;
        StoreConnection.reset() ;
    }

    public static Dataset createTestDataset() {
        Location location = cleanLocation() ;
        Dataset dataset = TDBFactory.connectDataset(location) ;
        // Non-transactional tests
        dataset = SystemTDB.setNonTransactional(dataset) ;
        return dataset ;
    }
    
    public static Dataset createTestDatasetMem() {
        Dataset dataset = TDBFactory.createDataset() ;
        // Non-transactional tests
        dataset = SystemTDB.setNonTransactional(dataset) ;
        return dataset ;
    }

    public static DatasetGraph createTestDatasetGraphMem() {
        DatasetGraph dataset = TDBFactory.createDatasetGraph() ;
        // Non-transactional tests
        dataset = SystemTDB.setNonTransactional(dataset) ;
        return dataset ;
    }
}

