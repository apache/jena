/*
  (c) Copyright 2002, 2003, 2004, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Node.java,v 1.41 2004-07-21 13:11:46 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.rdf.model.AnonId;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.shared.*;

import org.apache.commons.logging.*;

/**
    A Node has five subtypes: Node_Blank, Node_Anon, Node_URI,  
    Node_Variable, and Node_ANY.
    Nodes are only constructed by the node factory methods, and they will
    attempt to re-use existing nodes with the same label if they are recent
    enough.    
    @author Jeremy Carroll and Chris Dollin
*/

public abstract class Node {
    
    final protected Object label;
    static final int THRESHOLD = 10000;
    
    static final NodeCache present = new NodeCache(); 
    // static final HashMap present = new HashMap( THRESHOLD * 2 );

    static final Log log = LogFactory.getLog( Node.class );

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
        <li> 'some text' :: a string literal with that text
        <li> 'some text'someLanguage:: a string literal with that text and language
        <li> 'some text'someURI:: a typed literal with that text and datatype
        <li> digits :: a literal [OF WHAT TYPE] with that [numeric] value
        <li> _XXX :: a bnode with an AnonId built from _XXX
        <li> ?VVV :: a variable with name VVV
        <li> &PPP :: to be done
        <li> name:stuff :: the URI; name may be expanded using the Extended map
        </ul>
        @param x the string describing the node
        @return a node of the appropriate type with the appropriate label
    */
    public static Node create( String x )
        { return create( PrefixMapping.Extended, x ); }
        
    /**
        As for create(String), but the PrefixMapping used to translate URI strings
        is an additional argument.
        @param pm the PrefixMapping for translating pre:X strings
        @param x the string encoding the node to create
        @return a node with the appropriate type and label
    */
    public static Node create( PrefixMapping pm, String x )
        {
        if (x.equals( "" ))
            throw new JenaException( "Node.create does not accept an empty string as argument" );
        char first = x.charAt( 0 );
        if (first == '\'' || first == '\"')
            return Node.createLiteral( newString( pm, first, x ) );
        if (Character.isDigit( first )) 
            return Node.createLiteral( new LiteralLabel( x, "", XSDDatatype.XSDinteger ) );
        if (first == '_')
            return Node.createAnon( new AnonId( x ) );
        if (x.equals( "??" ))
            return Node.ANY;
        if (first == '?')
            return Node.createVariable( x.substring( 1 ) );
        if (first == '&')
            return Node.createURI( "q:" + x.substring( 1 ) );        
        int colon = x.indexOf( ':' );
        String d = pm.getNsPrefixURI( "" );
        return colon < 0 
            ? Node.createURI( (d == null ? "eh:" : d) + x )
            : Node.createURI( pm.expandPrefix( x ) )
            ;
        }
            
    private static RDFDatatype getType( String s )
        { return TypeMapper.getInstance().getSafeTypeByName( s ); }
    
    private static LiteralLabel literal( PrefixMapping pm, String spelling, String langOrType )
        {
        String content = unEscape( spelling );
        int colon = langOrType.indexOf( ':' );
        return colon < 0 
            ? new LiteralLabel( content, langOrType, false )
            : new LiteralLabel( content, "", getType( pm.expandPrefix( langOrType ) ) )
            ;
        }
    
    private static String unEscape( String spelling )
        {
        if (spelling.indexOf( '\\' ) < 0) return spelling;
        StringBuffer result = new StringBuffer( spelling.length() );
        int start = 0;
        while (true)
            {
            int b = spelling.indexOf( '\\', start );
            if (b < 0) break;
            result.append( spelling.substring( start, b ) );
            result.append( unEscape( spelling.charAt( b + 1 ) ) );
            start = b + 2;
            }
        result.append( spelling.substring( start ) );
        return result.toString();
        }
    
    private static char unEscape( char ch )
        {
        switch (ch)
        	{
            case '\\':
            case '\"':
            case '\'': return ch;
            case 'n': return '\n';
            case 's': return ' ';
            case 't': return '\t';
            default: return 'Z';
        	}
        }
    
    private static LiteralLabel newString( PrefixMapping pm, char quote, String nodeString )
        {
        int close = nodeString.lastIndexOf( quote );
        return literal( pm, nodeString.substring( 1, close ), nodeString.substring( close + 1 ) );
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
       
    /** make a blank node with a fresh anon id */ 
    public static Node createAnon()
        { return createAnon( new AnonId() ); }
        
    /** make a variable node with a given name */
    public static Node createVariable( String name )
        { return create( makeVariable, Node_Variable.variable( name ) ); }
        
    public static Node createLiteral( String value )
        { return createLiteral( value, "", false ); }
    
    /** make a literal with specified language and XMLishness.
        _lit_ must *not* be null. This intermediate implementation logs
        a warning to allow users moving over to Jena2 to correct their
        code. When they've had the opportunity, arrange to throw an
        exception, and delete _nullLiteralsGenerateWarnings_ and
        update the regression tests as directed. 
        @param isXml If true then lit is exclusive canonical XML of type rdf:XMLLiteral, and no checking will be invoked.
    */
    public static Node createLiteral( String lit, String lang, boolean isXml )
        {
        if (lit == null) 
            {
            // throw new SomeSuitableException( "null in createLiteral" );
            System.err.println /* log.warn */ ( "null treated as empty string in createLiteral: this will become illegal." );
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
    public static Node createLiteral( String lex, String lang, RDFDatatype dtype ) 
        throws DatatypeFormatException 
        { return createLiteral( new LiteralLabel( lex, lang, dtype ) ); }
    
    public static Node createUncachedLiteral( Object value, String lang, RDFDatatype dtype ) 
        throws DatatypeFormatException 
        { return new Node_Literal( new LiteralLabel( value, lang, dtype ) ); }
                                                   
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
        { throw new UnsupportedOperationException( this + " is not a blank node" ); }
    
    /** get the literal value of a literal node, otherwise die horribly */
    public LiteralLabel getLiteral()
        { throw new UnsupportedOperationException( this + " is not a literal node" ); }
    
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
        { Node construct( Object x ) { return new Node_Blank( x ); } };
        
    static final NodeMaker makeLiteral = new NodeMaker()
        { Node construct( Object x ) { return new Node_Literal( x ); } };
        
    static final NodeMaker makeURI = new NodeMaker()
        { Node construct( Object x ) { return new Node_URI( x ); } };
        
    static final NodeMaker makeVariable = new NodeMaker()
        { Node construct( Object x ) { return new Node_Variable( x ); } };
        
    /**
        The canonical NULL. It appears here so that revised definitions [eg as a bnode]
        that require the cache-and-maker system will work; the NodeMaker constants
        should be non-null at this point.
    */       
    public static final Node NULL = new Node_NULL(); 
    
    /**
        keep the distinguishing label value.
    */
    
    /* package visibility only */ Node( Object label ) 
        { this.label = label; }
        
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
        node of the correct class, and we add that node to the cache. create is
        synchronised to avoid threading problems - a separate thread might zap the
        cache entry that get is currently looking at.
    */
    public static synchronized Node create( NodeMaker maker, Object label )
        {
        if (label == null) throw new JenaException( "Node.make: null label" );
        Node node = (Node) present.get( label );
        return node == null ? cacheNewNode( label, maker.construct( label ) ) : node;
        }
        
    /**
         cache the node <code>n</code> under the key <code>label</code>,
         and return that node.
    */
    private static Node cacheNewNode( Object label, Node n )
        { 
        if (present.size() > THRESHOLD) { /* System.err.println( "> trashing node cache" ); */ present.clear(); }
        if (caching) present.put( label, n );
        return n;
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
    public String toString()
    	{ return toString( null ); }
    
    /**
        Answer a human-readable representation of the Node, leaving literals unquoted
        but compressing URIs.
    */
    public String toString( PrefixMapping pm )
        { return toString( pm, false ); }
        
    /**
        Answer a human readable representation of this Node, quoting literals if specified,
        and compressing URIs using the prefix mapping supplied.
    */
    public String toString( PrefixMapping pm, boolean quoting )
        { return label.toString(); }    
        
}

/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
