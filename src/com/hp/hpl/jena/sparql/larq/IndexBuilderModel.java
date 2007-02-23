/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.larq;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/** Root class for index creation from a graph or model.  This class
 *  a Model listener interface to index while loading data.  It also
 *  provides the ability to index from a StmtIterator.
 *  Once completed, the index builder should be closed for writing,
 *  then the getIndex() called.
 *  To update the index once closed, the application should create a new index builder.
 *  Any index readers (e.g. IndexLARQ objects)
 *  need to be recreated and registered.  
 *        
 * @author Andy Seaborne
 * @version $Id: IndexBuilderModel.java,v 1.3 2007/01/14 20:00:23 andy_seaborne Exp $
 */

public abstract class IndexBuilderModel extends StatementListener
{
    private Directory dir = null ;

    // Multiple inheritance would be nice .
    private IndexBuilderBase base ;
    
    /** Create an in-memory index */
    public IndexBuilderModel()
    { base = new IndexBuilderBase() ; }
    
    /** Manage a Lucene index that has already been created */
    public IndexBuilderModel(IndexWriter existingWriter)
    { base = new IndexBuilderBase(existingWriter) ; }

    /** Create an on-disk index */
    public IndexBuilderModel(File fileDir)
    { base = new IndexBuilderBase(fileDir) ; }
    
    /** Create an on-disk index */
    public IndexBuilderModel(String fileDir)
    { base = new IndexBuilderBase(fileDir) ; }

    protected IndexWriter getIndexWriter() { return base.getIndexWriter() ; }
    protected IndexReader getIndexReader() { return base.getIndexReader() ; }
    
    /** ModelListener interface : statement taken out of the model */
    public void removedStatement(Statement s)
    { unindexStatement(s) ; }

    /** Remove index information */
    public void unindexStatement(Statement s)
    { throw new java.lang.UnsupportedOperationException("unindexStatement") ; }
    
    /** ModelListener interface : statement added to the model */
    public void addedStatement(Statement s)
    { indexStatement(s) ; }
    
    /** Index all the statements from a StmtIterator */
    public void indexStatements(StmtIterator sIter)
    {
        for ( ; sIter.hasNext() ; )
            indexStatement(sIter.nextStatement()) ;
    }
    
    /** Update index based on one statement */
    public abstract void indexStatement(Statement s) ;
   
    /** Finish indexing */
    public void closeForWriting()
    { base.closeForWriting() ; }

    /** Get a search index used by LARQ.
     * Automatically close the index for update
     * */ 
    public IndexLARQ getIndex()
    { return base.getIndex() ; }
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