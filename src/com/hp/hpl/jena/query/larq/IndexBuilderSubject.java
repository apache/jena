/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.larq;

import java.io.File;

import org.apache.lucene.index.IndexWriter;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;

import com.hp.hpl.jena.sparql.ARQNotImplemented;

/** 
 * Class for indexing by subject (i.e. index is a literal and the 
 * index returns the subject).  Often the application can provide an
 * additional property to further restrict what gets indexed.
 *    
 * @author Andy Seaborne
 */

public class IndexBuilderSubject extends IndexBuilderModel
{
    Property property ;
    
    public IndexBuilderSubject()
    { super() ; }

    public IndexBuilderSubject(IndexWriter existingWriter)
    { super(existingWriter) ; }
    
    
    public IndexBuilderSubject(File fileDir)
    { super(fileDir) ; }
    
    public IndexBuilderSubject(String fileDir)
    { super(fileDir) ; }

    public IndexBuilderSubject(Property p)
    {
        this() ;
        property = p ;
    }

    public IndexBuilderSubject(Property p, IndexWriter existingWriter)
    { 
        super(existingWriter) ;
        property = p ;
    }
    
    public IndexBuilderSubject(Property p, File fileDir)
    {
        this(fileDir) ;
        property = p ;
    }
    
    public IndexBuilderSubject(Property p, String fileDir)
    {
        this(fileDir) ;
        property = p ;
    }
    
    @Override
    public void unindexStatement(Statement s)
    { throw new ARQNotImplemented("unindexStatement") ; }
    
    @Override
    public void indexStatement(Statement s)
    {
        if ( ! indexThisStatement(s) )
            return ;
        
        try {
            Node subject = s.getSubject().asNode() ;

            if ( ! s.getObject().isLiteral() ||
                 ! LARQ.isString(s.getLiteral()) )
                return ;
            
            Node object  = s.getObject().asNode() ;
            
            // Note: if a subject occurs twice with an indexable string,
            // there will be two hits later.
            index.index(subject, object.getLiteralLexicalForm()) ;
        } catch (Exception e)
        { throw new ARQLuceneException("indexStatement", e) ; }
    }

    protected boolean indexThisStatement(Statement s)
    {  
        if ( property == null ) 
            return true ;
        return s.getPredicate().equals(property) ;
    }
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