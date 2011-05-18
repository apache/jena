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


import java.io.File;

import org.apache.lucene.index.IndexWriter;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;

/** Index literals which are plain strings, string with a language tag or 
 *  which have a datatype of XSD string. */
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
