/*
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

package org.apache.jena.graph;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.function.Function;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.impl.LiteralLabel ;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sys.Serializer;

/**
 * A Node has subtypes:
 * <ul>
 * <li>{@link Node_Blank}, {@link Node_URI}, {@link Node_Literal},
 *     {@link Node_Triple} for RDF terms.
 * </li>
 * <li>{@link Node_Variable}, {@link Node_ANY}, for variables and wildcard.
 *     ARQs {@code Var} extends Node_Variable.
 * </li>
 * <li>{@link Node_Ext}(ension), and {@link Node_Graph} outside RDF.</li>
 * </ul>
 * <p>
 * Nodes should be constructed by the {@code NodeFactory} methods.
 */

public abstract class Node implements Serializable {

    /**
     * The canonical instance of Node_ANY. No other instances are required.
     */
    public static final Node ANY = Node_ANY.nodeANY;

    /**
     * The string used when a literal does not have a language tag.
     * Accessing the language of a non-literal will throw an exception.
     * @see Util#hasLang(Node)
     */
    public static final String noLangTag = "";

    /**
     * The TextDirection used when a literal does not have an initial text direction setting.
     * Accessing the initial text direction of a non-literal will throw an exception.
     * @see Util#hasDirection(Node)
     */
    public static final TextDirection noTextDirection = null;

    // Constants to separate hashes.
    // e.g. label is string so we perturb the hash code.
    protected static final int hashURI          = 30;
    protected static final int hashVariable     = 29;
    protected static final int hashANY          = 28;
    protected static final int hashNodeTriple   = 27;
    protected static final int hashExt          = 26;
    protected static final int hashBNode        = 25;

    /**
     * Visit a Node and dispatch on it to the appropriate method from the NodeVisitor
     * <code>v</code>.
     *
     * @param v the visitor to apply to the node
     * @return the value returned by the applied method
     */
    public abstract Object visitWith(NodeVisitor v);

    /**
     * Answer true iff this node is concrete, meaning a node that is data in an RDF
     * Graph.
     */
    public abstract boolean isConcrete();

    /**
     * Answer true iff this node is a literal node [subclasses override]
     */
    public boolean isLiteral()
    { return false; }

    /**
     * Answer true iff this node is a blank node [subclasses override]
     */
    public boolean isBlank()
    { return false; }

    /**
     * Answer true iff this node is a URI node [subclasses override]
     */
    public boolean isURI()
    { return false; }

        /**
         * Answer true iff this node is a variable node - subclasses override
         */
    public boolean isVariable()
    { return false; }

    /**
     * Answer true iff this node is an "triple node" (RDF-star)
     */
    public boolean isNodeTriple()
    { return false; }

    /**
     * Answer true iff this node is an "graph node" (N3 formula). This is not related
     * to named graphs.
     */
    public boolean isNodeGraph()
    { return false; }

    /** Extension node. Typically used in data structures based on triples.*/
    public boolean isExt()
    { return false; }

    /**
     * Answer the label of this blank node or throw an UnsupportedOperationException
     * if it's not blank.
     */
    public String getBlankNodeLabel()
    { throw new UnsupportedOperationException( this + " is not a blank node" ); }

    /**
     * Answer the literal value of a literal node, or throw an
     * UnsupportedOperationException if it's not a literal node
     */
    public LiteralLabel getLiteral()
    { throw new UnsupportedOperationException( this + " is not a literal node" ); }

    /**
     * Answer the value of this node's literal value, if it is a literal; otherwise
     * die horribly.
     */
    public Object getLiteralValue()
    { throw new NotLiteral( this ); }

    /**
     * Answer the lexical form of this node's literal value, if it is a literal;
     * otherwise die horribly.
     */
    public String getLiteralLexicalForm()
    { throw new NotLiteral( this ); }

    /**
     * Answer the language of this node's literal value, if it is a literal;
     * otherwise die horribly.
     */
    public String getLiteralLanguage()
    { throw new NotLiteral( this ); }

    /** Return the initial text direction for an rdf:dirLangString literal.
     * Does not return null if the literal is a rdf:dirLangString literal.
     * Returns null if the text direction is not set (and the datatype won't be rdf:dirLangString).
     * Otherwise die horribly.
     */
    public TextDirection getLiteralTextDirection()
    { throw new NotLiteral( this ); }

    /**
     * Answer the data-type URI of this node's literal value, if it is a literal;
     * otherwise die horribly.
     */
    public String getLiteralDatatypeURI()
    { throw new NotLiteral( this ); }

    /**
     * Answer the RDF datatype object of this node's literal value, if it is a
     * literal; otherwise die horribly.
     */
    public RDFDatatype getLiteralDatatype()
    { throw new NotLiteral( this ); }

    /**
     * Exception thrown if a literal-access operation is attempted on a non-literal
     * node.
     */
    public static class NotLiteral extends JenaException {
        public NotLiteral(Node it) {
            super(it + " is not a literal node");
        }
    }

    /**
     * Answer the object which is the index value for this Node. The default is this
     * Node itself; overridden in Node_Literal for literal indexing purposes. Only
     * concrete nodes should use this method.
     */
    public Object getIndexingValue()
    { return this; }

    /** get the URI of this node if it has one, else die horribly */
    public String getURI()
    { throw new UnsupportedOperationException( this + " is not a URI node" ); }

    /** get the namespace part of this node if it's a URI node, else die horribly */
    public String getNameSpace()
    { throw new UnsupportedOperationException( this + " is not a URI node" ); }

    /** get the localname part of this node if it's a URI node, else die horribly */
    public String getLocalName()
    { throw new UnsupportedOperationException( this + " is not a URI node" ); }

    /** get a variable nodes name, otherwise die horribly */
    public String getName()
    { throw new UnsupportedOperationException( "this (" + this.getClass() + ") is not a variable node" ); }

    /** Get the triple for a triple term (embedded triple), otherwise die horribly */
    public Triple getTriple()
    { throw new UnsupportedOperationException( "this (" + this.getClass() + ") is not a embedded triple node" ); }

    /** Get the graph for a graph term (N3 formula), otherwise die horribly */
    public Graph getGraph()
    { throw new UnsupportedOperationException( "this (" + this.getClass() + ") is not a graph-valued node" ); }

    /** answer true iff this node is a URI node with the given URI */
    public boolean hasURI( String uri )
    { return false; }

    /** See {@link Node_Ext} for custom Nodes */
    /*package*/ Node( ) {}

    /**
     * Java rules for equals. See also {#sameTermAs} and {#sameValueAs} Nodes only equal
     * other Nodes that have equal labels.
     */
    @Override
    public abstract boolean equals(Object o);

    public boolean sameTermAs(Object o)
    { return equals( o ); }

    /**
     * Test that two nodes are semantically equivalent.
     * In some cases this may be the same as equals, in others
     * equals is stricter. For example, two xsd:int literals with
     * the same value but different lexical form are semantically
     * equivalent but distinguished by the java equals function.
     * <p>Default implementation is to use equals, subclasses should
     * override this.</p>
     */
    public boolean sameValueAs(Object o)
    { return equals( o ); }

    /** Answer a human-readable representation of this Node. */
    @Override
    public abstract String toString();

    /**
     * Answer a human-readable representation of the Node.
     * For URIs, abbreviate URI.
     * For literals, quoting literals and abbreviating datatype URI.
     */
    public abstract String toString( PrefixMapping pmap );

    @Override
    public abstract int hashCode();

    /**
     * Answer true iff this node accepts the other one as a match. The default is an
     * equality test; it is over-ridden in subclasses to provide the appropriate
     * semantics for literals, ANY, and variables.
     *
     * @param other a node to test for matching
     * @return true iff this node accepts the other as a match
     */
    public boolean matches( Node other )
    { return equals( other ); }

    // ---- Serializable
    // Must be "protected", not "private".
    protected Object writeReplace() throws ObjectStreamException {
        Function<Node, Object> function =  Serializer.getNodeSerializer() ;
        if ( function == null )
            throw new IllegalStateException("Function for Node.writeReplace not set") ;
        return function.apply(this);
    }
    // Any attempt to serialize without replacement is an error.
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        throw new IllegalStateException();
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new IllegalStateException();
    }
    // ---- Serializable
}
