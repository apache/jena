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

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
    A Node has five subtypes: Node_Blank, Node_Anon, Node_URI,  
    Node_Variable, and Node_ANY.
    Nodes are only constructed by the node factory methods, and they will
    attempt to re-use existing nodes with the same label if they are recent
    enough.    
*/

public abstract class Node {
    
    final protected Object label;
    static final int THRESHOLD = 10000;
    
    /**
        The canonical instance of Node_ANY. No other instances are required.
    */       
    public static final Node ANY = new Node_ANY();
       
    static final String RDFprefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
            
    /**
     * @deprecated Use {@link NodeFactory#getType(String)} instead
     */
    @Deprecated
    public static RDFDatatype getType( String s )
        {
            return NodeFactory.getType(s) ;
        }
    
    /** make a blank node with a fresh anon id 
     * @deprecated Use {@link NodeFactory#createAnon()} instead*/ 
    @Deprecated
    public static Node createAnon()
        {
            return NodeFactory.createAnon() ;
        }
    
    /** make a blank node with the specified label 
     * @deprecated Use {@link NodeFactory#createAnon(AnonId)} instead*/
    @Deprecated
    public static Node createAnon( AnonId id )
        {
            return NodeFactory.createAnon(id) ;
        }
        
    /** make a literal node with the specified literal value 
     * @deprecated Use {@link NodeFactory#createLiteral(LiteralLabel)} instead*/
    @Deprecated
    public static Node createLiteral( LiteralLabel lit )
        {
            return NodeFactory.createLiteral(lit) ;
        }
        
    /** make a URI node with the specified URIref string 
     * @deprecated Use {@link NodeFactory#createURI(String)} instead*/
    @Deprecated
    public static Node createURI( String uri )
        {
            return NodeFactory.createURI(uri) ;
        }
    
    /** make a variable node with a given name 
     * @deprecated Use {@link NodeFactory#createVariable(String)} instead*/
    @Deprecated
    public static Node createVariable( String name )
        {
            return NodeFactory.createVariable(name) ;
        }
        
    /**
     * @deprecated Use {@link NodeFactory#createLiteral(String)} instead
     */
    @Deprecated
    public static Node createLiteral( String value )
        {
            return NodeFactory.createLiteral(value) ;
        }
    
    /** make a literal with specified language and XMLishness.
        _lit_ must *not* be null.
        @param isXml If true then lit is exclusive canonical XML of type 
            rdf:XMLLiteral, and no checking will be invoked.
     * @deprecated Use {@link NodeFactory#createLiteral(String,String,boolean)} instead
    */
    @Deprecated
    public static Node createLiteral( String lit, String lang, boolean isXml )
        {
            return NodeFactory.createLiteral(lit, lang, isXml) ;
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
     * @deprecated Use {@link NodeFactory#createLiteral(String,String,RDFDatatype)} instead
     */
    @Deprecated
    public static Node createLiteral( String lex, String lang, RDFDatatype dtype ) 
        throws DatatypeFormatException 
        {
            return NodeFactory.createLiteral(lex, lang, dtype) ;
        }
    
    /**
     * Build a typed literal node from its lexical form. The
     * lexical form will be parsed now and the value stored. If
     * the form is not legal this will throw an exception.
     * 
     * @param lex the lexical form of the literal
     * @param dtype the type of the literal, null for old style "plain" literals
     * @throws DatatypeFormatException if lex is not a legal form of dtype
     * @deprecated Use {@link NodeFactory#createLiteral(String,RDFDatatype)} instead
     */
    @Deprecated
    public static Node createLiteral( String lex, RDFDatatype dtype ) 
        throws DatatypeFormatException 
        {
            return NodeFactory.createLiteral(lex, dtype) ;
        }
    
    /**
     * @deprecated Use {@link NodeFactory#createUncachedLiteral(Object,String,RDFDatatype)} instead
     */
    @Deprecated
    public static Node createUncachedLiteral( Object value, String lang, RDFDatatype dtype ) 
        throws DatatypeFormatException 
        {
            return NodeFactory.createUncachedLiteral(value, lang, dtype) ;
        }
    
    /**
     * @deprecated Use {@link NodeFactory#createUncachedLiteral(Object,RDFDatatype)} instead
     */
    @Deprecated
    public static Node createUncachedLiteral( Object value, RDFDatatype dtype ) 
        throws DatatypeFormatException 
        {
            return NodeFactory.createUncachedLiteral(value, dtype) ;
        }
                                                   
    /**
        Visit a Node and dispatch on it to the appropriate method from the 
        NodeVisitor <code>v</code>.
        
    	@param v the visitor to apply to the node
    	@return the value returned by the applied method
     */
    public abstract Object visitWith( NodeVisitor v );
     
    /**
        Answer true iff this node is concrete, ie not variable, ie URI, blank, or literal.
    */                     
    public abstract boolean isConcrete();
        
    /** 
         Answer true iff this node is a literal node [subclasses override]
    */
    public boolean isLiteral() 
        { return false; }
    
    /** 
        Answer true iff this node is a blank node [subclasses override]
    */
    public boolean isBlank()
        { return false; }
    
    /** 
         Answer true iff this node is a URI node [subclasses override]
    */
    public boolean isURI()
        { return false; }
        
    /** 
        Answer true iff this node is a variable node - subclasses override
    */
    public boolean isVariable()
        { return false; }

    /** get the blank node id if the node is blank, otherwise die horribly */    
    public AnonId getBlankNodeId() 
        { throw new UnsupportedOperationException( this + " is not a blank node" ); }

    /**
        Answer the label of this blank node or throw an UnsupportedOperationException
        if it's not blank.
    */
    public String getBlankNodeLabel()
        { return getBlankNodeId().getLabelString(); }    
    
    /** 
         Answer the literal value of a literal node, or throw an UnsupportedOperationException
         if it's not a literal node 
     */
    public LiteralLabel getLiteral()
        { throw new UnsupportedOperationException( this + " is not a literal node" ); }

    /**
        Answer the value of this node's literal value, if it is a literal;
        otherwise die horribly. 
    */
    public Object getLiteralValue()
        { throw new NotLiteral( this ); }
    
    /**
        Answer the lexical form of this node's literal value, if it is a literal;
        otherwise die horribly.
    */
    public String getLiteralLexicalForm()
        { throw new NotLiteral( this ); }
    
    /**
        Answer the language of this node's literal value, if it is a literal;
        otherwise die horribly. 
    */
    public String getLiteralLanguage()
        { throw new NotLiteral( this ); }
    
    /**
        Answer the data-type URI of this node's literal value, if it is a 
        literal; otherwise die horribly. 
    */
    public String getLiteralDatatypeURI()
        { throw new NotLiteral( this ); }
    
    /**
        Answer the RDF datatype object of this node's literal value, if it is 
        a literal; otherwise die horribly. 
    */
    public RDFDatatype getLiteralDatatype()
        { throw new NotLiteral( this ); }
    
    public boolean getLiteralIsXML()
        { throw new NotLiteral( this ); }
    
    /**
        Exception thrown if a literal-access operation is attemted on a
        non-literal node.
    */
    public static class NotLiteral extends JenaException
        {
        public NotLiteral( Node it )
            { super( it + " is not a literal node" ); }
        }
    
    /**
        Answer the object which is the index value for this Node. The default
        is this Node itself; overridden in Node_Literal for literal indexing
        purposes. Only concrete nodes should use this method.
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
    
    /** answer true iff this node is a URI node with the given URI */
    public boolean hasURI( String uri )
        { return false; }
        
    /** an abstraction to allow code sharing */
    static abstract class NodeMaker { abstract Node construct( Object x ); }

    static final NodeMaker makeAnon = new NodeMaker()
        { @Override
        Node construct( Object x ) { return new Node_Blank( x ); } };
        
    static final NodeMaker makeLiteral = new NodeMaker()
        { @Override
        Node construct( Object x ) { return new Node_Literal( x ); } };
        
    static final NodeMaker makeURI = new NodeMaker()
        { @Override
        Node construct( Object x ) { return new Node_URI( x ); } };
        
    static final NodeMaker makeVariable = new NodeMaker()
        { @Override
        Node construct( Object x ) { return new Node_Variable( x ); } };
        
    /**
        The canonical NULL. It appears here so that revised definitions [eg as a bnode]
        that require the cache-and-maker system will work; the NodeMaker constants
        should be non-null at this point.
    */       
    @Deprecated
    public static final Node NULL = new Node_NULL(); 
    
    /* package visibility only */ Node( Object label ) 
        { this.label = label; }
        
    /**
        We object strongly to null labels: for example, they make .equals flaky.
    */
    public static Node create( NodeMaker maker, Object label )
        {
        if (label == null) throw new JenaException( "Node.make: null label" );
        return maker.construct( label ) ;
        }

    /**
		Nodes only equal other Nodes that have equal labels.
	*/	
    @Override
    public abstract boolean equals(Object o);
    
    /**
     * Test that two nodes are semantically equivalent.
     * In some cases this may be the same as equals, in others
     * equals is stricter. For example, two xsd:int literals with
     * the same value but different language tag are semantically
     * equivalent but distinguished by the java equality function
     * in order to support round-tripping.
     * <p>Default implementation is to use equals, subclasses should
     * override this.</p>
     */
    public boolean sameValueAs(Object o) 
        { return equals( o ); }

    @Override
    public int hashCode() 
        { return label.hashCode() * 31; }
    
    /**
        Answer true iff this node accepts the other one as a match.
        The default is an equality test; it is over-ridden in subclasses to
        provide the appropriate semantics for literals, ANY, and variables.
        
        @param other a node to test for matching
        @return true iff this node accepts the other as a match
    */
    public boolean matches( Node other )
        { return equals( other ); }

    /** 
        Answer a human-readable representation of this Node. It will not compress URIs, 
        nor quote literals (because at the moment too many places use toString() for 
        something machine-oriented).
    */   
    @Override
    public String toString()
    	{ return toString( null ); }
    
    /**
         Answer a human-readable representation of this Node where literals are
         quoted according to <code>quoting</code> but URIs are not compressed.
    */
    public String toString( boolean quoting )
        { return toString( null, quoting ); }
    
    /**
        Answer a human-readable representation of the Node, quoting literals and
        compressing URIs.
    */
    public String toString( PrefixMapping pm )
        { return toString( pm, true ); }
        
    /**
        Answer a human readable representation of this Node, quoting literals if specified,
        and compressing URIs using the prefix mapping supplied.
    */
    public String toString( PrefixMapping pm, boolean quoting )
        { return label.toString(); }
    }
