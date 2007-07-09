/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

 
package com.hp.hpl.jena.sparql.junit;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.sparql.core.DataFormat;
import com.hp.hpl.jena.sparql.vocabulary.TestManifest;
import com.hp.hpl.jena.sparql.vocabulary.TestManifestX;
import com.hp.hpl.jena.sparql.vocabulary.VocabTestQuery;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.junit.TestUtils;

import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.* ;


/** Wrapper class for individual test items.
 *  Assumes it is a query test item, using both the manifest vocabulary and the
 *  test query vocabulary.
 *  
 *  @author      Andy Seaborne
 *  @version     $Id: TestItem.java,v 1.20 2007/01/09 16:58:07 andy_seaborne Exp $
 */
public class TestItem
{
    private Resource testResource = null ;
    private Resource actionResource = null ;

    private String name ;

    private boolean buildLuceneIndex = false ; 
    private String resultFile ;
    private String comment ;
    private List defaultGraphURIs ;
    private List namedGraphURIs ;
    private Resource testType = null ;
    private String queryFile ;
    private Syntax queryFileSyntax ;
    
    public TestItem(Resource entry, Resource defaultTestType,
                    Syntax defaultQuerySyntax, DataFormat defaultDataSyntax)
    {
        testResource = entry ;
        
        if ( ! entry.hasProperty(TestManifest.name) )
            throw new QueryTestException("TestItem with no name ("+entry+")") ;
        name = _getName() ;

        if ( ! entry.hasProperty(TestManifest.action) )
            throw new QueryTestException("TestItem '"+name+"' with no action") ;
        
        // Assumes one type per test only.
        testType = TestUtils.getResource(entry, RDF.type) ;
        if ( testType == null )
            testType= defaultTestType ;
        
        queryFileSyntax = defaultQuerySyntax ;
        //dataFileSyntax = defaultDataSyntax ;
        
        resultFile = _getResultFile() ;
        comment = _getComment() ;

        defaultGraphURIs = _getDefaultGraphURIs() ;
        namedGraphURIs = _getNamedGraphsURIs() ; 
        
        queryFile = _getQueryFile() ;
        queryFileSyntax = _getSyntax(entry.getModel(), queryFile, defaultQuerySyntax) ;
        if ( queryFileSyntax == null && queryFile != null )
            queryFileSyntax = Syntax.guessQueryFileSyntax(queryFile) ;
        
        buildLuceneIndex = _getTextIndex() ;
    }
        
    public TestItem(String _name,
             String _queryFile,
             String _dataFile,
             String _resultFile)
    {
        name = _name ;
        queryFile = _queryFile ;
        defaultGraphURIs = new ArrayList() ;
        defaultGraphURIs.add(_dataFile) ;
        namedGraphURIs =  new ArrayList() ;
        resultFile = _resultFile ;
        comment = "" ;
        queryFileSyntax = Syntax.guessQueryFileSyntax(_queryFile) ;
    }
             
    
    public Resource getResource() { return testResource ; }
    public Resource getAction()   { return _getAction() ; }
    
    /** @return Returns the testType. */
    public Resource getTestType() { return testType ; }
    public String getQueryFile() { return queryFile ; }
    public Syntax getQueryFileSyntax() { return queryFileSyntax ; }
    public void setQueryFileSyntax(Syntax syntax) { queryFileSyntax = syntax ; }

    public String getResultFile() { return resultFile ; }
    public String getName() { return name ; }
    public String getComment() { return comment ; }
    public List getDefaultGraphURIs() { return defaultGraphURIs ; }
    public List getNamedGraphURIs() { return namedGraphURIs ; }
    
    public boolean requiresTextIndex() { return buildLuceneIndex ; }
    
    private String _getName()
    {
        Statement s = testResource.getProperty(TestManifest.name) ;
        if ( s == null )
            return "<<unset>>" ;
        return s.getString() ;
    }
    
    private Resource _getAction()
    {
        if ( actionResource == null )
            actionResource = testResource.getProperty(TestManifest.action).getResource() ;
        return actionResource ;
    }

    private String _getResultFile()
    {
        return TestUtils.getLiteralOrURI(testResource, TestManifest.result) ;
    }

    private String _getComment()
    {
        Statement s = testResource.getProperty(RDFS.comment) ; 
        if ( s == null )
            return null ;
        return s.getString() ;
    }
    
    // ----------------------------------------------------
    // ---- Query specific properties
    
    /** Get the data file (default graph): maybe unknown if part for the query (FROM) 
     * @return List
     */ 
    
    private List _getDefaultGraphURIs()
    {
        if ( ! _getAction().isAnon() )
            // Action is a URI - data had better be in the query itself.
            return null ;
            
        List l = new ArrayList() ;
        ClosableIterator cIter =  _getAction().listProperties(VocabTestQuery.data) ;
        for ( ; cIter.hasNext() ; )
        {
            Object obj = cIter.next() ;
            String df = ((Statement)obj).getResource().getURI() ;
            l.add(df) ;
        }
        cIter.close() ;
        
        return l ;
    }
    /** Get the named graphs : maybe unknown if part for the query (FROM NAMED) 
     * @return List
     */ 
    
    private List _getNamedGraphsURIs()
    {
        if ( ! _getAction().isAnon() )
            // Action is a URI - data had better be in the query itself.
            return null ;
            
        List l = new ArrayList() ;
        ClosableIterator cIter =  _getAction().listProperties(VocabTestQuery.graphData) ;
        for ( ; cIter.hasNext() ; )
        {
            Object obj = cIter.next() ;
            String df = ((Statement)obj).getResource().getURI() ;
            l.add(df) ;
        }
        cIter.close() ;
        
        return l ;
    }

    /** Get the query file: either it is the action (data in query)
     *  or it is specified within the bNode as a query/data pair. 
     * @return
     */ 
    
    private String _getQueryFile()
    {
        Resource r = _getAction() ;
        
        if ( r.hasProperty(VocabTestQuery.query))
            return TestUtils.getLiteralOrURI(r, VocabTestQuery.query) ;
        
        // No query property - must be this action node
        
        if ( _getAction().isAnon() )
            return "[]" ;
        return _getAction().getURI() ;
    }
        
    private Syntax _getSyntax(Model m, String uri, Syntax def)
    {
        Resource r = m.createResource(uri) ;
        if ( r.hasProperty(RDF.type) )
            return Syntax.make(r.getProperty(RDF.type).getResource().getURI()) ;
        return def ;
    }

    private boolean _getTextIndex()
    {
        Statement s = testResource.getProperty(TestManifestX.textIndex) ;
        if ( s == null )
            return false ;
        return s.getString().equalsIgnoreCase("true") ;
    }
    
    // ----------------------------------------------------
    // Misc
    
    public String toString()
    {
        StringBuffer sbuff = new StringBuffer() ;
        String name = getName() ;
        //String actionStr = FmtUtils.stringForRDFNode(_getAction()) ; 
        
        sbuff.append("Name: "+name) ;
        
        if ( getComment() != null )
            sbuff.append("    Comment: "+getComment()) ;
        return sbuff.toString() ;
    }
}


/*
 *  (c) Copyright 2004, 2005  Hewlett-Packard Development Company, LP
 *  All rights reserved.
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

