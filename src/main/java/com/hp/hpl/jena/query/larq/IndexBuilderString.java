/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.larq;


import java.io.File;

import org.apache.lucene.index.IndexWriter;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;

/** Index literals which are plain strings, string with a language tag or 
 *  which have a datatype of XSD string.  
 * 
 * @author Andy Seaborne
 */
public class IndexBuilderString extends IndexBuilderLiteral
{
    private Property property = null ;

    /** Create an index builder in-memory that indexes string literals */
    public IndexBuilderString() { super() ; }
    
    /** Use an existing IndexWriter to indexes string literal */
    public IndexBuilderString(IndexWriter existingWriter)
    { super(existingWriter) ; }
    
    /** Create an index builder, storing the Lucene files in a directory,
     *  that indexes string literals */
    public IndexBuilderString(File fileDir)
    { super(fileDir) ; }
    
    /** Create an index builder, storing the Lucene files in a directory,
     *  that indexes string literals */
    public IndexBuilderString(String fileDir)
    { super(fileDir) ; }

    /** Create an index builder in-memory that indexes string literals,
     * restricted to statements with a given property
     */
    public IndexBuilderString(Property property)
    { super() ; setProperty(property) ; }
    
    /** Use an existing IndexWriter to indexes string literal,
     * restricted to statements with a given property
     */
    public IndexBuilderString(Property property, IndexWriter existingWriter)
    { super(existingWriter) ; setProperty(property) ; }
    
    /** Create an index builder, storing the Lucene files in a directory,
     *  that indexes string literals, where the indexed statements are 
     *  restricted to statements with a given property
     */
    public IndexBuilderString(Property property, File fileDir)
    { super(fileDir) ; setProperty(property) ; }
    
    /** Create an index builder, storing the Lucene files in a directory,
     *  that indexes string literals, where the indexed statements are 
     *  restricted to statements with a given property
     */
    public IndexBuilderString(Property property, String fileDir)
    { super(fileDir) ; setProperty(property) ; }

    /** Condition to filter statements passed to the indexStatement.
     * indexThisLiteral also applies
     */
    @Override
    protected boolean indexThisStatement(Statement stmt)
    { 
        if ( property == null )
            return true ;
        return stmt.getPredicate().equals(property) ;
    }

    private void setProperty(Property p)
    { property = p ; }
    
    /** Index literal if it is a plain string,
     * a plain string with lanuage tag or an xsd string.
     * {@link #indexThisStatement(Statement) indexThisStatement} also applies.
     */
    @Override
    protected boolean indexThisLiteral(Literal literal)
    { return LARQ.isString(literal) ; }


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