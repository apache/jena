/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.larq;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.IndexWriter;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;

import com.hp.hpl.jena.sparql.ARQNotImplemented;

/** 
 * Base class for indexing literals (i.e. index is a literal and the 
 * index returns the literal)
 *
 */

public abstract class IndexBuilderLiteral extends IndexBuilderModel
{
    // Ensure literals ar eindex once only.
    // Expensive to have use a Lucene reader (they see the state of the index when opened)
    // to check so need to manange duplicates in this class.
    private Set<Node> seen = new HashSet<Node>() ;
    
    public IndexBuilderLiteral()
    { super() ; }

    public IndexBuilderLiteral(IndexWriter existingWriter)
    { super(existingWriter) ; }
    
    public IndexBuilderLiteral(File fileDir)
    { super(fileDir) ; }
    
    public IndexBuilderLiteral(String fileDir)
    { super(fileDir) ; }

    /** Test whether to index this literal */
    protected abstract boolean indexThisLiteral(Literal literal) ;
    
    /** Condition to filter statements passed to the indexStatement */
    protected abstract boolean indexThisStatement(Statement stmt) ;

    @Override
    public void unindexStatement(Statement s)
    { throw new ARQNotImplemented("unindexStatement") ; }
    
    @Override
    public void indexStatement(Statement s)
    {
        if ( ! indexThisStatement(s) )
            return ;
        
        try {
            if ( s.getObject().isLiteral() )
            {
                Node node = s.getObject().asNode() ;
                if ( ! seen.contains(node) )
                {
                    if ( indexThisLiteral(s.getLiteral()))
                        index.index(node, node.getLiteralLexicalForm()) ;
                    seen.add(node) ;
                }
            }
        } catch (Exception e)
        { throw new ARQLuceneException("indexStatement", e) ; }
    }
   
    /** Close the index - no more updates possible */
    @Override
    public void closeWriter()
    { 
        super.closeWriter() ;
        seen = null ;
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
