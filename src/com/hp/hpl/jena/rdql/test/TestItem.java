/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

 
package com.hp.hpl.jena.rdql.test;

import com.hp.hpl.jena.rdf.model.* ;

import com.hp.hpl.jena.vocabulary.* ;
import com.hp.hpl.jena.rdql.QueryPrintUtils;


/** Wrapper class for individual test items.
 *  Assumes it is a query test item, using both the manifest vocabulary and the
 *  test query vocabulary.
 *  
 *  @author      Andy Seaborne
 *  @version     $Id: TestItem.java,v 1.1 2004-08-31 10:01:31 andy_seaborne Exp $
 */
class TestItem
{
    Resource testResource = null ;
    
    Resource actionResource ;
    String name ;
    public String getName() { return name ; }
    
    String resultFile ;
    public String getResultFile() { return resultFile ; }
    
    String comment ;
    public String getComment() { return comment ; }
    
    String dataFile ;
    public String getDataFile() { return dataFile ; }
    
    String queryFile ;
    public String getQueryFile() { return queryFile ; }
    
    Resource queryForm ;
    public Resource getQueryForm() { return queryForm ; }
    
    Resource resultForm ;
    public Resource getResultForm() { return resultForm ; }
    
    TestItem(Resource r)
    {
        testResource = r ;
        if ( ! r.hasProperty(TestManifest.action) )
            throw new QueryTestException("TestItem with no action") ;
        if ( ! r.hasProperty(TestManifest.name) )
            throw new QueryTestException("TestItem with no name") ;
        
        name = _getName() ;
        resultFile = _getResultFile() ;
        comment = _getComment() ;
        dataFile = _getDataFile() ;
        queryFile = _getQueryFile() ;
        queryForm = _getQueryForm() ; 
        resultForm = _getResultForm() ;
    }
        
    TestItem(String _name,
             String _queryFile,
             String _dataFile,
             String _resultFile)
    {
        name = _name ;
        queryFile = _queryFile ;
        dataFile = _dataFile ;
        resultFile = _resultFile ;
        comment = "" ;
        queryForm = null ; 
        resultForm = null ;
    }
             
    
    public Resource getResource() { return testResource ; }
    
    // ----------------------------------------------------
    // ---- Action properties
    
    String _getName()
    {
        Statement s = testResource.getProperty(TestManifest.name) ;
        if ( s == null )
            return "<<unset>>" ;
        return s.getString() ;
    }
    
    Resource _getAction()
    {
        if ( actionResource == null )
            actionResource = testResource.getProperty(TestManifest.action).getResource() ;
        return actionResource ;
    }

    String _getResultFile()
    {
        return getLiteralOrURI(testResource, TestManifest.result) ;
    }

    String _getComment()
    {
        Statement s = testResource.getProperty(RDFS.comment) ; 
        if ( s == null )
            return null ;
        return s.getString() ;
    }
    
    // ----------------------------------------------------
    // ---- Query specific properties
    
    /** Get the data file: maybve unknown if part for the query (FROM) 
     * @return
     */ 
    
    String _getDataFile()
    {
        if ( _getAction().isAnon() )
            return getLiteralOrURI(_getAction(), TestQuery.data) ;
        return null ;
    }

    /** Get the query file: either it is the action (data in query)
     *  or it is specified within the bNode as a query/data pair. 
     * @return
     */ 
    
    String _getQueryFile()
    {
        Resource r = _getAction() ;
        if ( r.hasProperty(TestQuery.query))
            return getLiteralOrURI(_getAction(), TestQuery.query) ;
        
        // No query property - must be this action node
        
        if ( _getAction().isAnon() )
            return "[]" ;
        return _getAction().getURI() ;
    }
        
    Resource _getQueryForm()
    {
        //return _getAction().getProperty(TestQuery.queryForm).getResource() ;
        return null ;
    }
    
    Resource _getResultForm()
    {
        //return _getAction().getProperty(TestQuery.resultForm).getResource() ;
        return null ;
    }

    // ----------------------------------------------------
    // Misc
    
    public String toString()
    {
        StringBuffer sbuff = new StringBuffer() ;
        String name = getName() ;
        String comment = getComment() ;
                
        String actionStr = QueryPrintUtils.stringForRDFNode(_getAction()) ; 
        
        sbuff.append("Name: "+name) ;
        
        if ( getComment() != null )
            sbuff.append("    Comment: "+getComment()) ;
        return sbuff.toString() ;
    }
    
    // ----------------------------------------------------
    // Workers
    
    private String getLiteralOrURI(Resource r, Property p)
    {
        if ( r == null )
            return null ;
        
        if ( ! r.hasProperty(p) )
            return null ;
        
        RDFNode n = r.getProperty(p).getObject() ;
        if ( n instanceof Literal )
            return ((Literal)n).getString() ;
        
        if ( n instanceof Resource )
        {
            Resource r2 = (Resource)n ; 
            if ( ! r2.isAnon() )
                return r2.getURI() ;
        }
        
        throw new QueryTestException("Manifest problem: "+
                                     QueryPrintUtils.stringForRDFNode(n)+" => "+
                                     QueryPrintUtils.stringForRDFNode(p)
                                     ) ;
    }
}


/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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

