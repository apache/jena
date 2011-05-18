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

import java.io.IOException;
import java.io.StringReader;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestLARQ_Code extends TestCase
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestLARQ_Code.class) ;
    }
    
    
//    public static TestSuite suite()
//    {
//        TestSuite ts = new TestSuite(TestLARQ1.class) ;
//        ts.setName("LARQ-code") ;
//        return ts ;
//    }
//    // Called every test.
//    public void setUp() {}
//    public void tearDown() {}

    static final String datafile = "src/test/resources/LARQ/data-1.ttl" ;
    
    public void test_ext_1()
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r = model.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.closeWriter() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        assertEquals(1, TestLARQUtils.count(nIter)) ;
        nIter = index.searchModelByIndex("foo") ;
        Resource r2 = (Resource)nIter.nextNode() ;
        assertEquals(r, r2) ;
    }
    
    @Test public void test_ext_2()
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Model model = ModelFactory.createDefaultModel() ;
        Literal lit = model.createLiteral("example") ;
        b.index(lit, "foo") ;
        b.closeWriter() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        assertEquals(1, TestLARQUtils.count(nIter)) ;
        nIter = index.searchModelByIndex("foo") ;
        Literal lit2 = (Literal)nIter.nextNode() ;
        assertEquals(lit, lit2) ;
    }

    @Test public void test_ext_3()
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource bnode = model.createResource() ;
        b.index(bnode, "foo") ;
        b.closeWriter() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        assertEquals(1, TestLARQUtils.count(nIter)) ;
        nIter = index.searchModelByIndex("foo") ;
        Resource bnode2 = (Resource)nIter.nextNode() ;
        assertEquals(bnode, bnode2) ;
        assertTrue(bnode2.isAnon()) ;
    }

    @Test public void test_ext_4()
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r = model.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.closeWriter() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("bah") ;
        assertFalse(nIter.hasNext()) ;
    }
    
    @Test public void test_ext_5()
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r = model.createResource("http://example/r") ;
        StringReader sr = new StringReader("foo") ;
        b.index(r, sr) ;
        b.closeWriter() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        assertEquals(1, TestLARQUtils.count(nIter)) ;
        nIter = index.searchModelByIndex("foo") ;
        Resource r2 = (Resource)nIter.nextNode() ;
        assertEquals(r, r2) ;
    }
    
    // Test what happens when the index is updated after a reader index (LARQIndex) is created
    @Test public void test_ext_6()
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r1 = model.createResource("http://example/r1") ;
        Resource r2 = model.createResource("http://example/r2") ;
        
        StringReader sr = new StringReader("R1") ;
        b.index(r1, sr) ;
        b.closeWriter();
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("R1") ;
        assertEquals(1, TestLARQUtils.count(nIter)) ;
        nIter = index.searchModelByIndex("R2") ;
        assertEquals(0, TestLARQUtils.count(nIter)) ;
        
        // Add r2.
        b = new IndexBuilderNode() ;
        b.index(r2, new StringReader("R2")) ;
        b.closeWriter();

        // Old index - can't see R2
        nIter = index.searchModelByIndex("R2") ;
        assertEquals(0, TestLARQUtils.count(nIter)) ;
        
        // New index - can see R2
        index = b.getIndex() ;
        nIter = index.searchModelByIndex("R2") ;
        assertEquals(1, TestLARQUtils.count(nIter)) ;
    }
    
    @Test public void test_index_literal_1()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString()) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search both DC title and RDFS label
        assertEquals(3,TestLARQUtils.count(nIter)) ;
        index.close() ;
    }

    @Test public void test_index_literal_2()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString(DC.title)) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search just DC title
        assertEquals(2,TestLARQUtils.count(nIter)) ;
    } 
    
    @Test public void test_index_literal_3()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString()) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search both DC title and RDFS label
        for ( ; nIter.hasNext(); )
        {
            RDFNode n = nIter.nextNode() ;
            assertTrue(n instanceof Literal) ;
            assertTrue(model.contains(null, null, n)) ;
            boolean b = model.contains(null, DC.title, n) ||
                        model.contains(null, RDFS.label, n) ;
            assertTrue("DC.title or RDFS.label", b) ;
        }
    }

    @Test public void test_index_literal_4()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString(DC.title)) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search both DC title and RDFS label
        for ( ; nIter.hasNext(); )
        {
            RDFNode n = nIter.nextNode() ;
            assertTrue(n instanceof Literal) ;
            assertTrue(model.contains(null, DC.title, n)) ;
            assertFalse(model.contains(null, RDFS.label, n)) ;
        }
    }
    
    
    @Test public void test_index_subject_1()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderSubject()) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search both DC title and RDFS label
        assertEquals(3,TestLARQUtils.count(nIter)) ;
    }
    
    @Test public void test_index_subject_2()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderSubject(DC.title)) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search both DC title and RDFS label
        assertEquals(2,TestLARQUtils.count(nIter)) ;
    }

    @Test public void test_index_subject_3()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderSubject()) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search both DC title and RDFS label
        for ( ; nIter.hasNext(); )
        {
            RDFNode n = nIter.nextNode() ;
            assertTrue(n instanceof Resource) ;
            assertTrue(model.contains((Resource)n, null, (RDFNode)null)) ;
            boolean b = model.contains((Resource)n, DC.title, (RDFNode)null) ||
                        model.contains((Resource)n, RDFS.label, (RDFNode)null) ;
            assertTrue("subject with DC.title or RDFS.label", b) ;
        }
    }

    @Test public void test_index_subject_4()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderSubject(DC.title)) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        for ( ; nIter.hasNext(); )
        {
            RDFNode n = nIter.nextNode() ;
            assertTrue(n instanceof Resource) ;
            assertTrue(model.contains((Resource)n, null, (RDFNode)null)) ;
            assertTrue(model.contains((Resource)n, DC.title, (RDFNode)null)) ;
        }
    }

    // Negative searches
    @Test public void test_negative_1()
    {
        IndexLARQ index = TestLARQUtils.createIndex(datafile, new IndexBuilderString()) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }

    @Test public void test_negative_2()
    {
        IndexLARQ index = TestLARQUtils.createIndex(datafile, new IndexBuilderString(DC.title)) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }

    @Test public void test_negative_3()
    {
        IndexLARQ index = TestLARQUtils.createIndex(datafile, new IndexBuilderSubject()) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }
    
    @Test public void test_negative_4()
    {
        IndexLARQ index = TestLARQUtils.createIndex(datafile, new IndexBuilderSubject(DC.title)) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }
    
    @Test public void test_textMatches_index_registration_1()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString()) ;
        
        assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
        try {
            LARQ.setDefaultIndex(index) ;
            assertTrue(ARQ.getContext().isDefined(LARQ.indexKey)) ;
            
            QueryExecution qExec = TestLARQUtils.query(model, "{ ?lit pf:textMatch '+document' }") ;
            ResultSet rs = qExec.execSelect() ;
            assertEquals(3, TestLARQUtils.count(rs)) ;
            qExec.close() ;
            index.close() ;
            LARQ.removeDefaultIndex() ;
            assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
        } finally { LARQ.removeDefaultIndex() ; }
    }
    
    @Test public void test_textMatches_index_registration_2()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString()) ;
        
        assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
        QueryExecution qExec = TestLARQUtils.query(model, "{ ?lit pf:textMatch '+document' }") ;
        
        try {
            LARQ.setDefaultIndex(qExec.getContext(), index) ;
            assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
            assertTrue(qExec.getContext().isDefined(LARQ.indexKey)) ;
            
            ResultSet rs = qExec.execSelect() ;
            assertEquals(3, TestLARQUtils.count(rs)) ;
            qExec.close() ;
            index.close() ;
            LARQ.removeDefaultIndex(qExec.getContext()) ;
            assertFalse(qExec.getContext().isDefined(LARQ.indexKey)) ;
            assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
        } finally { LARQ.removeDefaultIndex() ; }
    }

    @Test public void test_remove_1() 
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Resource r = ResourceFactory.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.unindex(r, "foo");
        b.closeWriter();
        
        IndexLARQ index = b.getIndex() ;
        assertFalse(index.searchModelByIndex("foo").hasNext()) ;
    }
    
    @Test public void test_remove_2() throws Exception 
    {
        IndexBuilderString indexBuilder = new IndexBuilderString();
    	Model model = ModelFactory.createDefaultModel();
        model.register(indexBuilder) ;
        FileManager.get().readModel(model, datafile) ;
        model.removeAll(ResourceFactory.createResource("http://example/doc3"), (Property)null, (RDFNode)null);
        indexBuilder.closeWriter() ;
        
        IndexLARQ index = indexBuilder.getIndex() ;
        assertFalse(index.searchModelByIndex("keyword").hasNext()) ;    	
    }

    @Test public void test_remove_3() throws IOException 
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Resource r = ResourceFactory.createResource("http://example/r") ;
        StringReader sr = new StringReader("foo") ;
        b.index(r, sr) ;
        sr = new StringReader("foo") ;
        b.unindex(r, sr);
        b.closeWriter();
        
        IndexLARQ index = b.getIndex() ;
        assertFalse(index.searchModelByIndex("foo").hasNext()) ;
    }
    
    @Test public void test_remove_4() 
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Resource r1 = ResourceFactory.createResource("http://example/r1") ;
        b.index(r1, "foo") ;
        Resource r2 = ResourceFactory.createResource("http://example/r1") ;
        b.index(r2, "foo") ;
        b.unindex(r1, "foo"); // no reference counting without a Jena Model
        b.closeWriter();
        
        IndexLARQ index = b.getIndex() ;
        assertFalse(index.searchModelByIndex("foo").hasNext()) ;
    }

    @Test public void test_remove_5() throws Exception 
    {
        IndexBuilderString indexBuilder = new IndexBuilderString();
    	Model model = ModelFactory.createDefaultModel();
        model.register(indexBuilder) ;
        model.add(model.createResource("http://example/r1"), model.createProperty("http://example/p"), model.createLiteral("foo"));
        model.add(model.createResource("http://example/r2"), model.createProperty("http://example/p"), model.createLiteral("foo"));

        model.removeAll(model.getResource("http://example/r1"), model.getProperty("http://example/p"), model.createLiteral("foo"));        
        IndexLARQ index = indexBuilder.getIndex() ;
        assertTrue(index.searchModelByIndex("foo").hasNext()) ; // the Model is used as reference counting, still one literal "foo" is present in the Lucene index

        model.removeAll(model.getResource("http://example/r2"), model.getProperty("http://example/p"), model.createLiteral("foo"));        
        index = indexBuilder.getIndex() ;
        assertFalse(index.searchModelByIndex("foo").hasNext()) ;
    }
    
    @Test public void test_duplicates_1() 
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Resource r = ResourceFactory.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.index(r, "foo") ;
        b.closeWriter();

        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        if ( b.avoidDuplicates() ) {
            assertEquals(1, TestLARQUtils.count(nIter)) ;
        } else {
            assertEquals(2, TestLARQUtils.count(nIter)) ;
        }
    }
    
    @Test public void test_duplicates_2() throws Exception 
    {
        IndexBuilderString indexBuilder = new IndexBuilderString();
    	Model model = ModelFactory.createDefaultModel();
        model.register(indexBuilder) ;
        model.add(model.createResource("http://example/r"), RDFS.label, "foo");
        model.add(model.createResource("http://example/r"), RDFS.label, "foo");
        indexBuilder.closeWriter() ;

        IndexLARQ index = indexBuilder.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        if ( indexBuilder.avoidDuplicates() ) {
            assertEquals(1, TestLARQUtils.count(nIter)) ;
        } else {
            assertEquals(2, TestLARQUtils.count(nIter)) ;
        }
    }

    @Test public void test_duplicates_3() 
    {
        IndexBuilderNode b = new IndexBuilderNode() ;
        Resource r1 = ResourceFactory.createResource() ;
        Resource r2 = ResourceFactory.createResource() ;
        b.index(r1, "foo") ;
        b.index(r1, "foo") ;
        b.index(r1, "bar") ;
        b.index(r2, "foo") ;
        b.index(r2, "foo") ;
        b.index(r2, "bar") ;
        b.closeWriter();

        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        if ( b.avoidDuplicates() ) {
            assertEquals(2, TestLARQUtils.count(nIter)) ;
        } else {
            assertEquals(4, TestLARQUtils.count(nIter)) ;
        }
        nIter = index.searchModelByIndex("bar") ;
        assertEquals(2, TestLARQUtils.count(nIter)) ;
    }
    
    @Test public void test_duplicates_4() throws Exception 
    {
        IndexBuilderString indexBuilder = new IndexBuilderString();
    	Model model = ModelFactory.createDefaultModel();
        model.register(indexBuilder) ;
        model.add(model.createResource(), RDFS.label, "foo");
        model.add(model.createResource(), RDFS.label, "foo");
        indexBuilder.closeWriter() ;
        
        IndexLARQ index = indexBuilder.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        if ( indexBuilder.avoidDuplicates() ) {
            assertEquals(1, TestLARQUtils.count(nIter)) ;
        } else { 
            assertEquals(2, TestLARQUtils.count(nIter)) ;
        }
    }
    
    @Test public void test_duplicates_5() throws Exception 
    {
        IndexBuilderNode indexBuilder = new IndexBuilderNode();
        
        Resource blank = ResourceFactory.createResource() ;
        indexBuilder.index(blank, "foo");
        indexBuilder.index(blank, "foo");
        indexBuilder.index(blank, "bar");
        indexBuilder.closeWriter() ;
        
        IndexLARQ index = indexBuilder.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        if ( indexBuilder.avoidDuplicates() ) {
            assertEquals(1, TestLARQUtils.count(nIter)) ;
        } else {
            assertEquals(2, TestLARQUtils.count(nIter)) ;
        }
        assertEquals(blank, index.searchModelByIndex("foo").nextNode());
        nIter = index.searchModelByIndex("bar") ;
        assertEquals(1, TestLARQUtils.count(nIter)) ;
        assertEquals(blank, index.searchModelByIndex("bar").nextNode());
    }
    
    @Test public void test_existing_index_not_overridden() throws Exception 
    {
        Directory dir = new RAMDirectory() ;
        IndexWriter indexWriter = IndexWriterFactory.create(dir) ;

        IndexBuilderNode b = new IndexBuilderNode(indexWriter) ;
        Resource r1 = ResourceFactory.createResource() ;
        b.index(r1, "foo") ;
        b.closeWriter();

        indexWriter = IndexWriterFactory.create(dir) ;
        b = new IndexBuilderNode(indexWriter) ;
        Resource r2 = ResourceFactory.createResource() ;
        b.index(r2, "bar") ;

        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        assertEquals(1, TestLARQUtils.count(nIter)) ;
        nIter = index.searchModelByIndex("bar") ;
        assertEquals(1, TestLARQUtils.count(nIter)) ;
    }
    
//    
//    @Test public void test_textMatches_literal_1()
//    {
//        Model model = ModelFactory.createDefaultModel() ;
//        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString()) ;
//        LARQ.setDefaultIndex(index) ;
//        QueryExecution qExec = query(model, "{ ?lit pf:textMatch '+document' }") ;
//        ResultSet rs = qExec.execSelect() ;
//        assertEquals(3, TestLARQUtils.count(rs)) ;
//        qExec.close() ;
//        index.close() ;
//        LARQ.removeDefaultIndex() ;
//    }
//
//    @Test public void test_textMatches_literal_2()
//    {
//        Model model = ModelFactory.createDefaultModel() ;
//        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString(DC.title)) ;
//        LARQ.setDefaultIndex(index) ;
//        QueryExecution qExec = query(model, "{ ?lit pf:textMatch '+document' }") ;
//        ResultSet rs = qExec.execSelect() ;
//        assertEquals(2, TestLARQUtils.count(rs)) ;
//        qExec.close() ;
//        index.close() ;
//        LARQ.removeDefaultIndex() ;
//    }
//    
//    
//
//    @Test public void test_textMatches_literal_3()
//    {
//        Model model = ModelFactory.createDefaultModel() ;
//        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString()) ;
//        LARQ.setDefaultIndex(index) ;
//        QueryExecution qExec = query(model, 
//            "{ ?lit pf:textMatch '+document' }") ;
//        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
//        //ResultSetFormatter.outputAsJSON(rs1) ;
//        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.load(results1)) ;
//        assertTrue(RSCompare.same(rs1, rs2)) ;
//        qExec.close() ;
//        index.close() ;
//        LARQ.removeDefaultIndex() ;
//    }
//
//    @Test public void test_textMatches_literal_4()
//    {
//        Model model = ModelFactory.createDefaultModel() ;
//        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString()) ;
//        LARQ.setDefaultIndex(index) ;
//        QueryExecution qExec = query(model, 
//            "{ ?lit pf:textMatch '+document' . ?lit pf:textMatch '+document'}") ;
//        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
//        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.load(results1)) ;
//        assertTrue(RSCompare.same(rs1, rs2)) ;
//        qExec.close() ;
//        index.close() ;
//        LARQ.removeDefaultIndex() ;
//    }
//
//    @Test public void test_textMatches_literal_5()
//    {
//        Model model = ModelFactory.createDefaultModel() ;
//        IndexLARQ index = TestLARQUtils.createIndex(model, datafile, new IndexBuilderString()) ;
//        LARQ.setDefaultIndex(index) ;
//        QueryExecution qExec = query(model, 
//        "{ ?lit pf:textMatch '+document' . ?doc ?p ?lit }") ;
//        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
//        //ResultSetFormatter.outputAsJSON(rs1) ;
//        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.load(results2)) ;
//        assertTrue(RSCompare.same(rs1, rs2)) ;
//        qExec.close() ;
//        index.close() ;
//        LARQ.removeDefaultIndex() ;
//    }
}
