/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.test.suites;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Iterator;

import arq.examples.* ;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.larq.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.resultset.RSCompare;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.NiceIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestLARQ extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestLARQ.class) ;
        ts.setName("LARQ") ;
        return ts ;
    }
//    // Called every test.
//    public void setUp() {}
//    public void tearDown() {}

    static final String datafile = "testing/LARQ/data-1.ttl" ;
    static final String results1 = "testing/LARQ/results-1.srj" ;
    static final String results2 = "testing/LARQ/results-2.srj" ;
    static final String results3 = "testing/LARQ/results-3.srj" ;
    
    public void test_ext_1()
    {
        IndexBuilderExt b = new IndexBuilderExt() ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r = model.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.closeForWriting() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        assertEquals(1, count(nIter)) ;
        nIter = index.searchModelByIndex("foo") ;
        Resource r2 = (Resource)nIter.nextNode() ;
        assertEquals(r, r2) ;
    }
    
    public void test_ext_2()
    {
        IndexBuilderExt b = new IndexBuilderExt() ;
        Model model = ModelFactory.createDefaultModel() ;
        Literal lit = model.createLiteral("example") ;
        b.index(lit, "foo") ;
        b.closeForWriting() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        assertEquals(1, count(nIter)) ;
        nIter = index.searchModelByIndex("foo") ;
        Literal lit2 = (Literal)nIter.nextNode() ;
        assertEquals(lit, lit2) ;
    }

    public void test_ext_3()
    {
        IndexBuilderExt b = new IndexBuilderExt() ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource bnode = model.createResource() ;
        b.index(bnode, "foo") ;
        b.closeForWriting() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        assertEquals(1, count(nIter)) ;
        nIter = index.searchModelByIndex("foo") ;
        Resource bnode2 = (Resource)nIter.nextNode() ;
        assertEquals(bnode, bnode2) ;
        assertTrue(bnode2.isAnon()) ;
    }

    public void test_ext_4()
    {
        IndexBuilderExt b = new IndexBuilderExt() ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r = model.createResource("http://example/r") ;
        b.index(r, "foo") ;
        b.closeForWriting() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("bah") ;
        assertFalse(nIter.hasNext()) ;
    }
    
    public void test_ext_5()
    {
        IndexBuilderExt b = new IndexBuilderExt() ;
        Model model = ModelFactory.createDefaultModel() ;
        Resource r = model.createResource("http://example/r") ;
        StringReader sr = new StringReader("foo") ;
        b.index(r, sr) ;
        b.closeForWriting() ;
        
        IndexLARQ index = b.getIndex() ;
        NodeIterator nIter = index.searchModelByIndex("foo") ;
        assertEquals(1, count(nIter)) ;
        nIter = index.searchModelByIndex("foo") ;
        Resource r2 = (Resource)nIter.nextNode() ;
        assertEquals(r, r2) ;
    }
    
    public void test_index_literal_1()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString()) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search both DC title and RDFS label
        assertEquals(3,count(nIter)) ;
        index.close() ;
    }

    public void test_index_literal_2()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString(DC.title)) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search just DC title
        assertEquals(2,count(nIter)) ;
    } 
    
    public void test_index_literal_3()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString()) ; 
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

    public void test_index_literal_4()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString(DC.title)) ; 
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
    
    
    public void test_index_subject_1()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderSubject()) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search both DC title and RDFS label
        assertEquals(3,count(nIter)) ;
    }
    
    public void test_index_subject_2()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderSubject(DC.title)) ; 
        NodeIterator nIter = index.searchModelByIndex(model, "+document") ;
        // Search both DC title and RDFS label
        assertEquals(2,count(nIter)) ;
    }

    public void test_index_subject_3()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderSubject()) ; 
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

    public void test_index_subject_4()
    { 
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderSubject(DC.title)) ; 
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
    public void test_negative_1()
    {
        IndexLARQ index = createIndex(datafile, new IndexBuilderString()) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }

    public void test_negative_2()
    {
        IndexLARQ index = createIndex(datafile, new IndexBuilderString(DC.title)) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }

    public void test_negative_3()
    {
        IndexLARQ index = createIndex(datafile, new IndexBuilderSubject()) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }
    
    public void test_negative_4()
    {
        IndexLARQ index = createIndex(datafile, new IndexBuilderSubject(DC.title)) ;
        assertFalse(index.hasMatch("+iceberg")) ;
    }
    
    public void test_textMatches_index_registration_1()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString()) ;
        
        assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
        try {
            LARQ.setDefaultIndex(index) ;
            assertTrue(ARQ.getContext().isDefined(LARQ.indexKey)) ;
            
            QueryExecution qExec = query(model, "{ ?lit pf:textMatch '+document' }") ;
            
            ResultSet rs = qExec.execSelect() ;
            assertEquals(3, count(rs)) ;
            qExec.close() ;
            index.close() ;
            LARQ.removeDefaultIndex() ;
            assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
        } finally { LARQ.removeDefaultIndex() ; }
    }
    
    public void test_textMatches_index_registration_2()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString()) ;
        
        assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
        QueryExecution qExec = query(model, "{ ?lit pf:textMatch '+document' }") ;
        
        try {
            LARQ.setDefaultIndex(qExec.getContext(), index) ;
            assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
            assertTrue(qExec.getContext().isDefined(LARQ.indexKey)) ;
            
            ResultSet rs = qExec.execSelect() ;
            assertEquals(3, count(rs)) ;
            qExec.close() ;
            index.close() ;
            LARQ.removeDefaultIndex(qExec.getContext()) ;
            assertFalse(qExec.getContext().isDefined(LARQ.indexKey)) ;
            assertFalse(ARQ.getContext().isDefined(LARQ.indexKey)) ;
        } finally { LARQ.removeDefaultIndex() ; }
    }
    
    public void test_textMatches_literal_1()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString()) ;
        LARQ.setDefaultIndex(index) ;
        QueryExecution qExec = query(model, "{ ?lit pf:textMatch '+document' }") ;
        ResultSet rs = qExec.execSelect() ;
        assertEquals(3, count(rs)) ;
        qExec.close() ;
        index.close() ;
        LARQ.removeDefaultIndex() ;
    }

    public void test_textMatches_literal_2()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString(DC.title)) ;
        LARQ.setDefaultIndex(index) ;
        QueryExecution qExec = query(model, "{ ?lit pf:textMatch '+document' }") ;
        ResultSet rs = qExec.execSelect() ;
        assertEquals(2, count(rs)) ;
        qExec.close() ;
        index.close() ;
        LARQ.removeDefaultIndex() ;
    }

    public void test_textMatches_literal_3()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString()) ;
        LARQ.setDefaultIndex(index) ;
        QueryExecution qExec = query(model, 
            "{ ?lit pf:textMatch '+document' }") ;
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
        //ResultSetFormatter.outputAsJSON(rs1) ;
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.load(results1)) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
        qExec.close() ;
        index.close() ;
        LARQ.removeDefaultIndex() ;
    }

    public void test_textMatches_literal_4()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString()) ;
        LARQ.setDefaultIndex(index) ;
        QueryExecution qExec = query(model, 
            "{ ?lit pf:textMatch '+document' . ?lit pf:textMatch '+document'}") ;
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.load(results1)) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
        qExec.close() ;
        index.close() ;
        LARQ.removeDefaultIndex() ;
    }

    public void test_textMatches_literal_5()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderString()) ;
        LARQ.setDefaultIndex(index) ;
        QueryExecution qExec = query(model, 
        "{ ?lit pf:textMatch '+document' . ?doc ?p ?lit }") ;
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
        //ResultSetFormatter.outputAsJSON(rs1) ;
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.load(results2)) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
        qExec.close() ;
        index.close() ;
        LARQ.removeDefaultIndex() ;
    }

    public void test_textMatches_subject_1()
    {
        Model model = ModelFactory.createDefaultModel() ;
        IndexLARQ index = createIndex(model, datafile, new IndexBuilderSubject(DC.title)) ;
        LARQ.setDefaultIndex(index) ;
        QueryExecution qExec = query(model, 
        "{ ?subj pf:textMatch '+document' }") ;
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
        //ResultSetFormatter.outputAsJSON(rs1) ;
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.load(results3)) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
        qExec.close() ;
        index.close() ;
        LARQ.removeDefaultIndex() ;
    }

    public void test_larq_example_1() throws Exception
    { 
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene1.main(null) ;
        } finally { System.setOut(pOut) ; }
    }

    public void test_larq_example_2() throws Exception
    {
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene2.main(null) ;
        } finally { System.setOut(pOut) ; }
    }

    public void test_larq_example_3() throws Exception
    {
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene3.main(null) ;
        } finally { System.setOut(pOut) ; }
    }

    public void test_larq_example_4() throws Exception
    {
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene4.main(null) ;
        } finally { System.setOut(pOut) ; }
    }
    public void test_larq_example_5() throws Exception
    {
        PrintStream pOut = System.out ;
        PrintStream pNull = new PrintStream(new ByteArrayOutputStream()) ;
        System.setOut(pNull) ;
        try {
            ExLucene5.main(null) ;
        } finally { System.setOut(pOut) ; }
    }
    
    private QueryExecution query(Model model, String pattern)
    { return query(model, pattern, null) ; }

    private QueryExecution query(Model model, String pattern, IndexLARQ index)
    {
        String queryString = StringUtils.join("\n", new String[]{
            "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>" ,
            "PREFIX :       <http://example/>" ,
            "PREFIX pf:     <http://jena.hpl.hp.com/ARQ/property#>",
            "PREFIX  dc:    <http://purl.org/dc/elements/1.1/>",
            "SELECT *",
            pattern,
        }) ;
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
        if ( index != null )
            LARQ.setDefaultIndex(qExec.getContext(), index) ;
        return qExec ;
    }
    
    private int count(ResultSet rs)
    {
        return ResultSetFormatter.consume(rs) ;
    }
    
    private int count(Iterator iter)
    {
        int count = 0 ; 
        for ( ; iter.hasNext() ; )
        {
            iter.next();
            count++ ;
        }
        NiceIterator.close(iter) ;
        return count ;
    }
    
    private static IndexLARQ createIndex(String datafile, IndexBuilderModel indexBuilder)
    { return createIndex(ModelFactory.createDefaultModel(), datafile, indexBuilder) ; }
    
    private static IndexLARQ createIndex(Model model, String datafile, IndexBuilderModel indexBuilder)
    {
        model.register(indexBuilder) ;
        FileManager.get().readModel(model, datafile) ;
        model.unregister(indexBuilder) ;
        indexBuilder.closeForWriting() ;
        return indexBuilder.getIndex() ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */