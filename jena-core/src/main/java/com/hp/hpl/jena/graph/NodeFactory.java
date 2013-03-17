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

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.datatypes.DatatypeFormatException ;
import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;

public class NodeFactory
{

    public static RDFDatatype getType( String s )
    { return TypeMapper.getInstance().getSafeTypeByName( s ); }

    /** make a blank node with a fresh anon id */ 
    public static Node createAnon()
        { return createAnon( AnonId.create() ); }

    /** make a blank node with the specified label */
    public static Node createAnon( AnonId id )
        { return Node.create( Node.makeAnon, id ); }

    /** make a literal node with the specified literal value */
    public static Node createLiteral( LiteralLabel lit )
        { return Node.create( Node.makeLiteral, lit ); }

    /** make a URI node with the specified URIref string */
    public static Node createURI( String uri )
        { return Node.create( Node.makeURI, uri ); }

    /** make a variable node with a given name */
    public static Node createVariable( String name )
        { return Node.create( Node.makeVariable, Node_Variable.variable( name ) ); }

    public static Node createLiteral( String value )
    { return createLiteral( value, "", false ); }

    /** make a literal with specified language and XMLishness.
        _lit_ must *not* be null.
        @param isXml If true then lit is exclusive canonical XML of type 
            rdf:XMLLiteral, and no checking will be invoked.
    */
    public static Node createLiteral( String lit, String lang, boolean isXml )
        {
        if (lit == null) throw new NullPointerException
            ( "null for literals has been illegal since Jena 2.0" );
        return createLiteral( LiteralLabelFactory.create( lit, lang, isXml ) ); 
        }

    /**
     * Build a literal node from its lexical form. The
     * lexical form will be parsed now and the value stored. If
     * the form is not legal this will throw an exception.
     * 
     * @param lex the lexical form of the literal
     * @param lang the optional language tag
     * @param dtype the type of the literal, null for old style "plain" literals
     * @throws DatatypeFormatException if lex is not a legal form of dtype
     */
    public static Node createLiteral( String lex, String lang, RDFDatatype dtype ) 
        throws DatatypeFormatException 
        { return createLiteral( LiteralLabelFactory.createLiteralLabel( lex, lang, dtype ) ); }

    /**
     * Build a typed literal node from its lexical form. The
     * lexical form will be parsed now and the value stored. If
     * the form is not legal this will throw an exception.
     * 
     * @param lex the lexical form of the literal
     * @param dtype the type of the literal, null for old style "plain" literals
     * @throws DatatypeFormatException if lex is not a legal form of dtype
     */
    public static Node createLiteral( String lex, RDFDatatype dtype ) 
        throws DatatypeFormatException 
        { return createLiteral( LiteralLabelFactory.createLiteralLabel( lex, "", dtype ) ); }

    public static Node createUncachedLiteral( Object value, String lang, RDFDatatype dtype ) 
    throws DatatypeFormatException 
    { return new Node_Literal( LiteralLabelFactory.create( value, lang, dtype ) ); }

    public static Node createUncachedLiteral( Object value, RDFDatatype dtype ) 
    throws DatatypeFormatException 
    { return new Node_Literal( LiteralLabelFactory.create( value, "", dtype ) ); }

}

