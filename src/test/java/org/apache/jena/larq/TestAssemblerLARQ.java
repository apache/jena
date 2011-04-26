package org.apache.jena.larq;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openjena.atlas.lib.FileOps;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.tdb.assembler.VocabTDB;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;

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
        assertTrue(((Dataset)thing).asDatasetGraph() instanceof DatasetGraphTDB) ;
        
        assertNotNull(LARQ.getDefaultIndex()) ;
        
        ds.close() ;
    }
    
}
