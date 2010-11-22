/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.larq;

import java.io.File;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/** Root class for index creation from a graph or model.  This class
 *  can be used as a Model listener to index while loading data.  It also
 *  provides the ability to index from a StmtIterator.
 *  Once completed, the index builder should be closed for writing,
 *  then the getIndex() called.
 *  To update the index once closed, the application should create a new index builder.
 *  Any index readers (e.g. IndexLARQ objects)
 *  need to be recreated and registered.  
 *        
 * @author Andy Seaborne
 */

public abstract class IndexBuilderModel extends StatementListener
{
    private Directory dir = null ;

    // Multiple inheritance would be nice .
    protected IndexBuilderNode index ;
    
    /** Create an in-memory index */
    public IndexBuilderModel()
    { index = new IndexBuilderNode() ; }
    
    /** Manage a Lucene index that has already been created */
    public IndexBuilderModel(IndexWriter existingWriter)
    { index = new IndexBuilderNode(existingWriter) ; }

    /** Create an on-disk index */
    public IndexBuilderModel(File fileDir)
    { index = new IndexBuilderNode(fileDir) ; }
    
    /** Create an on-disk index */
    public IndexBuilderModel(String fileDir)
    { index = new IndexBuilderNode(fileDir) ; }

//    protected IndexWriter getIndexWriter() { return index.getIndexWriter() ; }
//    protected IndexReader getIndexReader() { return index.getIndexReader() ; }
    
    /** ModelListener interface : statement taken out of the model */
    @Override
    public void removedStatement(Statement s)
    { unindexStatement(s) ; }

    /** Remove index information */
    public void unindexStatement(Statement s)
    { throw new java.lang.UnsupportedOperationException("unindexStatement") ; }
    
    /** ModelListener interface : statement added to the model */
    @Override
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
   
    
    /** Flush the index, optimizing it, to allow a reader to be created */
    public void flushWriter() { index.flushWriter() ; }
    
    /** Close the index - no more updates possible */
    public void closeWriter() { index.closeWriter() ; }

    /** Get a search index used by LARQ. */
    public IndexLARQ getIndex()
    { return index.getIndex() ; }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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