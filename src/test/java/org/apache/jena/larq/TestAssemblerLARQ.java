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

package org.apache.jena.larq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.larq.assembler.AssemblerLARQ;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openjena.atlas.lib.FileOps;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.assembler.VocabTDB;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction;

public class TestAssemblerLARQ {

    private static String testDir = "src/test/resources" ;
    private static String tmpDir = "target/data" ;
    
    @BeforeClass static public void beforeClass()
    {
        FileOps.ensureDir(tmpDir) ;
        FileOps.ensureDir(tmpDir + "/tdb") ;
        FileOps.ensureDir(tmpDir + "/lucene") ;
    }
    
    @Before public void before()
    {
        FileOps.clearDirectory(tmpDir + "/tdb") ;
        FileOps.clearDirectory(tmpDir + "/lucene") ;
        LARQ.setDefaultIndex(null) ;
    }
    
    @AfterClass static public void afterClass()
    {
        FileOps.clearDirectory(tmpDir + "/tdb") ;
        FileOps.clearDirectory(tmpDir + "/lucene") ;
    }
    
    @Test public void testCreate()
    {
        assertNull(LARQ.getDefaultIndex()) ;

        String f = testDir + "/tdb.ttl" ;
        Object thing = AssemblerUtils.build( f, VocabTDB.tDatasetTDB) ;
        assertTrue(thing instanceof Dataset) ;
        Dataset ds = (Dataset)thing ;
        ds.asDatasetGraph() ;
        assertTrue(((Dataset)thing).asDatasetGraph() instanceof DatasetGraphTransaction) ;
        
        assertNotNull(LARQ.getDefaultIndex()) ;
        
        ds.close() ;
        
        IndexLARQ indexLARQ = LARQ.getDefaultIndex();
        indexLARQ.close();
    }
    
    @Test public void testMake1() throws CorruptIndexException, IOException {
        IndexLARQ indexLARQ = null;
        try {
            Dataset ds = TDBFactory.createDataset() ;
            indexLARQ = AssemblerLARQ.make(ds, tmpDir + "/lucene") ;
            Directory directory = indexLARQ.getLuceneReader().directory() ;
            assertTrue ( directory instanceof FSDirectory );
        } finally {
            if ( indexLARQ != null ) indexLARQ.close();            
        }
    }
    
    @Test public void testMake2() throws CorruptIndexException, IOException {
        Dataset ds = TDBFactory.createDataset() ;
        IndexLARQ indexLARQ = AssemblerLARQ.make(ds, null) ;
        Directory directory = indexLARQ.getLuceneReader().directory() ;
        assertTrue ( directory instanceof RAMDirectory );
    }
    
    @Test public void testMake3() throws CorruptIndexException, IOException {
        IndexLARQ indexLARQ1 = null ;
        IndexLARQ indexLARQ2 = null ;
        try {
            Dataset ds = TDBFactory.createDataset() ;
            indexLARQ1 = AssemblerLARQ.make(ds, tmpDir + "/lucene") ;
            indexLARQ1.writer.commit() ;
            indexLARQ2 = AssemblerLARQ.make(ds, tmpDir + "/lucene") ;
            Directory directory = indexLARQ2.getLuceneReader().directory() ;
            assertTrue ( directory instanceof FSDirectory );
        } finally {
            if ( indexLARQ1 != null ) indexLARQ1.close() ;
            if ( indexLARQ2 != null ) indexLARQ2.close() ;
        }
    }

    // if an index directory does not exist, LARQ will create it and build the appropriate Lucene index
    @Test public void testMake4() throws CorruptIndexException, IOException {
        File path = new File(tmpDir + File.separator + "lucene") ;
        IndexLARQ indexLARQ = null;
        try {
            FileOps.delete(path.getAbsolutePath()) ;
            assertFalse(path.exists()) ;
            Dataset ds = TDBFactory.createDataset() ;
            ds.getDefaultModel().add(ResourceFactory.createResource(), ResourceFactory.createProperty("foo:p"), ResourceFactory.createPlainLiteral("bar")) ;
            indexLARQ = AssemblerLARQ.make(ds, path.getAbsolutePath()) ;
            Iterator<HitLARQ> iter = indexLARQ.search("bar") ;
            assertTrue (iter.hasNext()) ;
            assertEquals("bar", iter.next().getNode().getLiteral().getValue()) ;
        } finally {
            FileOps.ensureDir(path.getAbsolutePath()) ;
            if ( indexLARQ != null ) indexLARQ.close() ;
        }
        
        

    }
    
}
