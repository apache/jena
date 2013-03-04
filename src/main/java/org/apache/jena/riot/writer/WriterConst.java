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

package org.apache.jena.riot.writer;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class WriterConst
{
    // See INDENT_PREDICATE
//    public static final int MIN_SUBJECT     = 4 ;
    
    /** Minimum width of the predicate columns. */
    public static final int MIN_PREDICATE   = 4 ;

    /** Subjects longer than this have a NL after them. */ 
    public static final int LONG_SUBJECT    = 20 ;
    /** Predicates longer than this have a NL after them. */
    public static final int LONG_PREDICATE  = 30 ;
    
    /** The IRI column in a prefix. */
    public static final int PREFIX_IRI      = 15;
    
    // Pretty writers - do object lists?
    // The block writers do not do object lists. 
    public static boolean OBJECT_LISTS      = true ;
    
//    // Fixed column widths (unused).
//    public static int COLW_SUBJECT          = 6 ;
//    public static int COLW_PREDICATE        = 10 ;
    
    /** Column for start of predicate */  
    public static final int INDENT_PREDICATE = 8 ;
    
    /** Column for start of object */
    public static final int INDENT_OBJECT   = 8 ;

    /** Indent for triples in default graph blocks */
    public static final int INDENT_GDFT          = 2 ;
    /** Indent for trinples in named graph blocks */
    public static final int INDENT_GNMD          = 4 ;

    
    /** Minimum gap from S to P and from P to O */
    public static final int MIN_GAP         = 2 ;

    /** Minimum gap from S to P */
    public static final int GAP_S_P         = MIN_GAP ;         
    /** Minimum gap from P to O */
    public static final int GAP_P_O         = MIN_GAP ;  
    
    /** Whether to put in a newline after the opening { of a default graph block */   
    public static final boolean NL_GDFT_START    =  false ;
    /** Whether to put in a newline after the opening { of a named graph block */   
    public static final boolean NL_GNMD_START    =  true ;
    
    /** Whether to put the closing } of a default graph block on a newline */
    public static final boolean NL_GDFT_END      =  true ;
    /** Whether to put the closing } of a named graph block on a newline */
    public static final boolean NL_GNMD_END      =  true ;

    
    // Constants.
    public static final String rdfNS        = RDF.getURI() ;
    public static final Node RDF_type       = RDF.Nodes.type ;
    public static final Node RDF_First      = RDF.Nodes.first ;
    public static final Node RDF_Rest       = RDF.Nodes.rest ;
    public static final Node RDF_Nil        = RDF.Nodes.nil ;
    

}

