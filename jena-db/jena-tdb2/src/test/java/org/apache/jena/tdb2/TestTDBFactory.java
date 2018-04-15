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

package org.apache.jena.tdb2;

import static org.junit.Assert.*;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.sys.Txn;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdf.model.ResourceFactory ;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.Test ;

/** Test of TDB2Factory - the Dataset level API to TDB2 **/
public class TestTDBFactory
{
    static Resource s1 = ResourceFactory.createResource("http://example/s2");
    static Resource s2 = ResourceFactory.createResource();
    static Property p  =  ResourceFactory.createProperty("http://example/ns#", "p");
    static Literal  o1 =  ResourceFactory.createPlainLiteral("object");
    static Literal  o2 =  ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger);
    
    @Test
    public void testTDBFactory1() {
        TDBInternal.reset() ;
        Dataset ds = TDB2Factory.connectDataset(Location.mem()) ;
        test(ds);
    }
    
    @Test
    public void testTDBFactory2() {
        TDBInternal.reset() ;
        Dataset ds = TDB2Factory.connectDataset(Location.mem("MEMORY")) ;
        test(ds);
    }
    
    @Test
    public void testTDBFactory3() {
        TDBInternal.reset() ;
        // Only do disk things for tests that need them (disk takes time!).
        String DIRx = ConfigTest.getCleanDir() ;
        Location DIR = Location.create(DIRx);
        try { 
            FileOps.clearDirectory(DIRx) ;
            Dataset ds = TDB2Factory.connectDataset(DIR) ;
            test(ds);
        } finally { FileOps.clearDirectory(DIRx) ; }
    }
    
    @Test
    public void testTDBFactory2DS_1() {
        TDBInternal.reset() ;
        Dataset ds1 = TDB2Factory.connectDataset(Location.mem("FOO")) ;
        Dataset ds2 = TDB2Factory.connectDataset(Location.mem("FOO")) ;
        Txn.executeWrite(ds1, ()->{
            ds1.getDefaultModel().add(s1, p, o1);
        });
        Txn.executeRead(ds2, ()->{
            assertTrue(ds2.getDefaultModel().contains(s1, p, o1));
        });
    }
    
    @Test
    public void testTDBFactory2DS_2() {
        TDBInternal.reset() ;
        // The unnamed location is unique each time.
        Dataset ds1 = TDB2Factory.connectDataset(Location.mem()) ;
        Dataset ds2 = TDB2Factory.connectDataset(Location.mem()) ;
        Txn.executeWrite(ds1, ()->{
            ds1.getDefaultModel().add(s1, p, o1);
        });
        Txn.executeRead(ds2, ()->{
            assertFalse(ds2.getDefaultModel().contains(s1, p, o1));
        });
    }
    
    @Test
    public void testTDBFactory2DS_3() {
        TDBInternal.reset() ;
        TDBInternal.reset() ;
        String DIRx = ConfigTest.getCleanDir() ;
        Location DIR = Location.create(DIRx);
        try {
            Dataset ds1 = TDB2Factory.connectDataset(DIR) ;
            Dataset ds2 = TDB2Factory.connectDataset(DIR) ;
            Txn.executeWrite(ds1, ()->{
                ds1.getDefaultModel().add(s1, p, o1);
            });
            Txn.executeRead(ds2, ()->{
                assertTrue(ds2.getDefaultModel().contains(s1, p, o1));
            });
        } finally { FileOps.clearDirectory(DIRx) ; }
    }

    private static void test(Dataset ds) {
        Txn.executeWrite(ds, ()->{
            ds.getDefaultModel().add(s1, p, o1);
        });
        Txn.executeRead(ds, ()->{
            assertTrue(ds.getDefaultModel().contains(s1, p, o1));
        });
        Txn.executeWrite(ds, ()->{
            ds.getDefaultModel().remove(s1, p, o1);
            ds.getDefaultModel().add(s2, p, o2);
        });
        Txn.executeRead(ds, ()->{
            assertFalse(ds.getDefaultModel().contains(s1, p, o1));
            assertTrue(ds.getDefaultModel().contains(s2, p, o2));
        });
    }
}
