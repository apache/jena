/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Node.java,v 1.9 2003-05-19 19:37:35 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.shared.*;

import org.apache.log4j.*;

import java.util.*;

/**
    @author Jeremy Carroll and Chris Dollin
<br>
    A Node has five subtypes: Node_Blank, Node_Anon, Node_URI,  
    Node_Variable, and Node_ANY.
    Nodes are only constructed by the node factory methods, and they will
    attempt to re-use existing nodes with the same label if they are recent
    enough.
*/

public abstract class Node {
    
    final protected Object label;
    static final int THRESHOLD = 1000;
    static final HashMap present = new HashMap( THRESHOLD * 2 );

    static final Logger log = Logger.getLogger( Node.class );

    /**
        The canonical instance of Node_ANY; no-one else need use the
        constructor.
    */       
    public static final Node ANY = new Node_ANY();
        
    static final String RDFprefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    /**
        Returns a Node described by the string, primarily for testing purposes.
        The string represents a URI, a numeric literal, a string literal, a bnode label,
        or a variable.        
        <ul>
        <li> "some text" :: a string literal with that text
        <li> digits :: a literal [OF WHAT TYPE] with that [numeric] value
        <li> _XXX :: a bnode with an AnonId built from _XXX
        <li> ?VVV :: a variable with name VVV
        <li> &PPP :: to be done
        <li> name:stuff :: the URI; name may be expanded 
        </ul>
        @param x the string describing the node
        @return a node of the appropriate type with the appropriate label
    */
    public static Node create( String x )
        {
        if (x.equals( "" ))
            throw new JenaException( "GraphTestBase::node does not accept an empty string as argument" );
        char first = x.charAt( 0 );
        if (first == '\'')
            return Node.createLiteral( new LiteralLabel(  x, "en-UK", false ) );
        if (Character.isDigit( first )) 
            return Node.createLiteral( new LiteralLabel( x, "nn-NN", false ) );
        if (first == '_')
            return Node.createAnon( new AnonId( x ) );
        if (first == '?')
            return Node.createVariable( x.substring( 1 ) );
        if (first == '&')
            return Node.createURI( "q:" + x.substring( 1 ) );        
        int colon = x.indexOf( ':' );
        if (colon < 0)
            return Node.createURI( "eh:" + x );
        String prefix = x.substring( 0, colon );
        if (prefix.equals( "rdf") )
            return Node.createURI( RDFprefix + x.substring( colon + 1 ) );
        return Node.createURI( x );
        }
        
    /** make a blank node with the specified label */
    public static Node createAnon( AnonId id )
        { return create( makeAnon, id ); }
        
    /** make a literal node with the specified literal value */
    public static Node createLiteral( LiteralLabel lit )
        { return create( makeLiteral, lit ); }
        
    /** make a URI node with the specified URIref string */
    public static Node createURI( String uri )
        { return create( makeURI, uri ); }
       
    private static String checkURI( String uri )
        { // must start "letters:"
        if (uri.length() == 0) return uri; // surely not
        if (Character.isLetterOrDigit( uri.charAt(0))) return uri;
        if (uri.charAt(0) == '#' || uri.charAt(0) == '_') return uri;
        for (int i = 0; i < uri.length(); i += 1)
            {
            char ch = uri.charAt( i );
            if (uri.charAt( i ) == ':') return uri;
            if (Character.isLetterOrDigit( ch ) || ch == '_') {} else break;
            }
        System.err.println( "| BAD URI: " + uri );
        RuntimeException th = new RuntimeException( "BAD " + uri );
        th.printStackTrace( System.err );
        throw th;
        }    
   
    /** make a blank node with a fresh anon id */ 
    public static Node createAnon()
        { return createAnon( new AnonId() ); }
        
    /** make a variable node with a given name */
    public static Node createVariable( String name )
        { return create( makeVariable, "?" + name ); }
        
    /** make a literal with specified language and XMLishness.
        _list_ must *not* be null. This intermediate implementation logs
        a warning to allow users moving over to Jena2 to correct their
        code. When they've had the opportunity, arrange to throw an
        exception, and delete _nullLiteralsGenerateWarnings_ and
        update the regression tests as directed. 
    */
    public static Node createLiteral( String lit, String lang, boolean isXml )
        {
        if (lit == null) 
            {
            // throw new SomeSuitableException( "null in createLiteral" );
            log.warn( "null treated as empty string in createLiteral: this will become illegal." );
            lit = "";
            } 
        return createLiteral( new LiteralLabel( lit, lang, isXml ) ); 
        }    
        
    public static void nullLiteralsGenerateWarnings()
        {}  
        
    /**
     * Build a typed literal node from its lexical form. The
     * lexical form will be parsed now and the value stored. If
     * the form is not legal this will throw an exception.
     * 
     * @param lex the lexical form of the literal
     * @param lang the optional language tag
     * @param dtype the type of the literal, null for old style "plain" literals
     * @throws DatatypeFormatException if lex is not a legal form of dtype
     */
    public static Node createLiteral(String lex, String lang, RDFDatatype dtype) 
                                            throws DatatypeFormatException {        
        return createLiteral( new LiteralLabel(lex, lang, dtype) );
    }
                                                   
    /**
        Visit a Node and dispatch on it to the appropriate method from the 
        NodeVisitor <code>v</code>.
        
    	@param v the visitor to apply to the node
    	@return the value returned by the applied method
     */
    public abstract Object visitWith( NodeVisitor v );
                             
    /** is this a literal node - overridden in Node_Literal */
    public boolean isLiteral() 
        { return false; }
    
    /** is this a blank node - overridden in Node_Blank */
    public boolean isBlank()
        { return false; }
    
    /** is this a URI node - overridden in Node_URI */
    public boolean isURI()
        { return false; }
        
    /** is this a variable node - overridden in Node_Variable */
    public boolean isVariable()
        { return false; }

    /** get the blank node id if the node is blank, otherwise die horribly */    
    public AnonId getBlankNodeId() 
        { throw new UnsupportedOperationException( "this is not a blank node" ); }
    
    /** get the literal value of a literal node, otherwise die horribly */
    public LiteralLabel getLiteral()
        { throw new UnsupportedOperationException( "this is not a literal node" ); }
    
    /** get the URI of this node if it has one, else die horribly */
    public String getURI()
        { throw new UnsupportedOperationException( "this is not a URI node" ); }
        
    /**
        get the triple associated with this node; if no direct association, use the
        triple-getter _r_. 
    */
    public Triple getTriple( GetTriple r )
        { return r.getTriple( this ); }
        
    /** get a variable nodes name, otherwise die horribly */
    public String getName()
        { throw new UnsupportedOperationException( "this (" + this.getClass() + ") is not a variable node" ); }
        
    /** an abstraction to allow code sharing */
    static abstract class NodeMaker { abstract Node construct( Object x ); }

    static final NodeMaker makeAnon = new NodeMaker()
        { Node construct( Object x ) { return new Node_Blank( x ); } };
        
    static final NodeMaker makeLiteral = new NodeMaker()
        { Node construct( Object x ) { return new Node_Literal( x ); } };
        
    static final NodeMaker makeURI = new NodeMaker()
        { Node construct( Object x ) { return new Node_URI( x ); } };
        
    static final NodeMaker makeVariable = new NodeMaker()
        { Node construct( Object x ) { return new Node_Variable( x ); } };
    
    /**
        if the cache of recent nodes is "too big", empty it. Then put this latest
        node in, unless caching has been suppressed.
    */
    
    /* package visibility only */ Node( Object label ) 
        {
        this.label = label;
        if (present.size() > THRESHOLD) { /* System.err.println( "> trashing node cache" ); */ present.clear(); }
        if (caching) present.put( label, this );
        }
        
    static private boolean caching = true;
    
    /**
        provided only for testing purposes. _cache(false)_ switches off caching and
        clears the cache. _cache(true)_ switches caching [back] on. This allows
        structural equality to be tested. 
    */
    public static void cache( boolean wantCache )
        {
        if (wantCache == false) present.clear();
        caching = wantCache;
        }
        
    /**
        We object strongly to null labels: for example, they make .equals flaky. We reuse nodes 
        from the recent cache if we can. Otherwise, the maker knows how to construct a new
        node of the correct class (and the Node constructor will then add it to the cache).
    */
    public static Node create( NodeMaker maker, Object label )
        {
        if (label == null) throw new JenaException( "Node.make: null label" );
        Node node = (Node) present.get( label );
        if (node == null) node = maker.construct( label ); 
        return node;
        }
        
	/**
		Nodes only equal other Nodes that have equal labels.
	*/	
    public abstract boolean equals(Object o);
    
    /**
     * Test that two nodes are semantically equivalent.
     * In some cases this may be the sames as equals, in others
     * equals is stricter. For example, two xsd:int literals with
     * the same value but different language tag are semantically
     * equivalent but distinguished by the java equality function
     * in order to support round tripping.
     * <p>Default implementation is to use equals, subclasses should
     * override this.</p>
     */
    public boolean sameValueAs(Object o) {
        return equals(o);
    }

    public int hashCode() {
    	return label.hashCode();
    }
    


    /** 
        Return the N-Triple representation of this node. hedgehog hack: just use 
        the label's string.
    */   
    public String toString() {
    	return label.toString();
    }
}

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
