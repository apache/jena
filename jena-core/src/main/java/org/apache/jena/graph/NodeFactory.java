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

package org.apache.jena.graph ;

import java.util.Objects ;

import org.apache.jena.datatypes.DatatypeFormatException ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.TypeMapper ;
import org.apache.jena.graph.impl.LiteralLabel ;
import org.apache.jena.graph.impl.LiteralLabelFactory ;
import org.apache.jena.sys.JenaSystem;

public class NodeFactory {

    static { JenaSystem.init(); }

    public static RDFDatatype getType(String s) {
        if ( s == null )
            return null ;
        return TypeMapper.getInstance().getSafeTypeByName(s) ;
    }

    /** Make a fresh blank node */
    public static Node createBlankNode() {
        return createBlankNode(BlankNodeId.create()) ;
    }

    /** make a blank node with the specified label
     */
    public static Node createBlankNode(BlankNodeId id) {
        Objects.requireNonNull(id, "Argument to NodeFactory.createBlankNode is null") ;
        return new Node_Blank(id) ;
    }

    /** make a blank node with the specified label */
    public static Node createBlankNode(String string) {
        BlankNodeId id = BlankNodeId.create(string) ;
        return new Node_Blank(id) ;
    }

    /** make a blank node with a fresh anon id
     *  @deprecated Use {@link #createBlankNode()}
     */
    @Deprecated
    public static Node createAnon() {
        return createAnon(BlankNodeId.create()) ;
    }

    /** make a blank node with the specified label
     * @deprecated Use {@link #createBlankNode(BlankNodeId)}
     */
    @Deprecated
    public static Node createAnon(BlankNodeId id) {
        Objects.requireNonNull(id, "Argument to NodeFactory.createAnon is null") ;
        return new Node_Blank(id) ;
    }

    /** make a blank node with the specified label
     * @deprecated Use {@link #createBlankNode(String)}
     */
    @Deprecated
    public static Node createAnon(String string) {
        Objects.requireNonNull(string, "Argument to NodeFactory.createAnon is null") ;
        BlankNodeId id = BlankNodeId.create(string) ;
        return new Node_Blank(id) ;
    }

    /** make a literal node with the specified literal value */
    public static Node createLiteral(LiteralLabel lit) {
        Objects.requireNonNull(lit, "Argument to NodeFactory.createLiteral is null") ;
        return new Node_Literal( lit ) ;
    }

    /** make a URI node with the specified URIref string */
    public static Node createURI(String uri) {
        Objects.requireNonNull(uri, "Argument to NodeFactory.createURI is null") ;
        return new Node_URI(uri) ;
    }

    /** make a variable node with a given name */
    public static Node createVariable(String name) {
        Objects.requireNonNull(name, "Argument to NodeFactory.createVariable is null") ;
        return new Node_Variable(name) ;
    }

    public static Node createLiteral(String value) {
        Objects.requireNonNull(value, "Argument to NodeFactory.createLiteral is null") ;
        return createLiteral(value, "", false) ;
    }

    /**
     * make a literal with specified language and XMLishness. lexical form must
     * not be null.
     *
     * @param lex
     * @param lang
     * @param isXml
     *            If true then lit is exclusive canonical XML of type
     *            rdf:XMLLiteral, and no checking will be invoked.
     */
    public static Node createLiteral(String lex, String lang, boolean isXml) {
        if ( lex == null )
            throw new NullPointerException("null lexical form for literal") ;
        return createLiteral(LiteralLabelFactory.create(lex, lang, isXml)) ;
    }

    /**
     * Make a literal with specified language. lexical form must not be null.
     *
     * @param lex
     *            the lexical form of the literal
     * @param lang
     *            the optional language tag
     */
    public static Node createLiteral(String lex, String lang) {
        // Equivalent to create(lex, lang, false) except the XML flag is
        // hidden so client code does not see it unnecesarily.
        if ( lex == null )
            throw new NullPointerException("null lexical form for literal") ;
        return createLiteral(LiteralLabelFactory.create(lex, lang)) ;
    }

    /**
     * Build a literal node from its lexical form. The lexical form will be
     * parsed now and the value stored. If the form is not legal this will throw
     * an exception.
     *
     * @param lex
     *            the lexical form of the literal
     * @param lang
     *            the optional language tag
     * @param dtype
     *            the type of the literal
     * @throws DatatypeFormatException
     *             if lex is not a legal form of dtype
     */
    public static Node createLiteral(String lex, String lang, RDFDatatype dtype) throws DatatypeFormatException {
        return createLiteral(LiteralLabelFactory.createLiteralLabel(lex, lang, dtype)) ;
    }

    /**
     * Build a typed literal node from its lexical form. The lexical form will
     * be parsed now and the value stored. If the form is not legal this will
     * throw an exception.
     *
     * @param lex
     *            the lexical form of the literal
     * @param dtype
     *            the type of the literal
     * @throws DatatypeFormatException
     *             if lex is not a legal form of dtype
     */
    public static Node createLiteral(String lex, RDFDatatype dtype) throws DatatypeFormatException {
        return createLiteral(LiteralLabelFactory.create(lex, dtype)) ;
    }

    /** Create a Node based on the value
     * If the value is a string we
     * assume this is intended to be a lexical form after all.
     * @param value
     *          The value, mapped according to registered types.
     * @param dtype
     *          RDF Datatype.
     * @return Node
     * @throws DatatypeFormatException
     */
    public static Node createLiteralByValue(Object value, RDFDatatype dtype) throws DatatypeFormatException {
        Objects.requireNonNull(value, "Argument 'value' to NodeFactory.createLiteralByValue is null") ;
        return new Node_Literal(LiteralLabelFactory.createByValue(value, "", dtype)) ;
    }

    /** Create a Node based on the value
     * If the value is a string we
     * assume this is intended to be a lexical form after all.
     * @param value
     *          The value, mapped according to registered types.
     * @param lang
     *          (optional) Language tag, if a string.
     * @param dtype
     *          RDF Datatype.
     * @return Node
     * @throws DatatypeFormatException
     */
    public static Node createLiteralByValue(Object value, String lang, RDFDatatype dtype) throws DatatypeFormatException {
        Objects.requireNonNull(value, "Argument 'value' to NodeFactory.createLiteralByValue is null") ;
        return new Node_Literal(LiteralLabelFactory.createByValue(value, lang, dtype)) ;
    }

    /** @deprecated To be removed: Use {@link #createLiteralByValue(Object, RDFDatatype)} */
    @Deprecated
    public static Node createUncachedLiteral(Object value, RDFDatatype dtype) throws DatatypeFormatException {
        Objects.requireNonNull(value, "Argument 'value' to NodeFactory.createLiteralByValue is null") ;
        return createLiteralByValue(value, dtype) ;
    }

    /** @deprecated To be removed: Use {@link #createLiteralByValue(Object, String, RDFDatatype)} */
    @Deprecated
    public static Node createUncachedLiteral(Object value, String lang, RDFDatatype dtype) throws DatatypeFormatException {
        Objects.requireNonNull(value, "Argument 'value' to NodeFactory.createLiteralByValue is null") ;
        return createLiteralByValue(value, lang, dtype) ;
    }

    /** Create a triple node (RDF*) */
    public static Node createTripleNode(Node s, Node p, Node o) {
        Triple triple = Triple.create(s, p, o);
        return createTripleNode(triple);
    }

    /** Create a triple node (RDF*) */
    public static Node createTripleNode(Triple triple) {
        return new Node_Triple(triple);
    }

    /** Create a graph node. This is an N3-formula; it is not a named graph (see "quad") */
    public static Node createGraphNode(Graph graph) {
        return new Node_Graph(graph);
    }
}
