/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: QueryNode.java,v 1.9 2005-08-25 10:14:13 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query;

import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.BrokenException;

/**
     A QueryNode is a wrapped node that has been processed against
     some variable-binding map to discover (a) what kind of node it is
     and (b) what index in the binding map it has.
<p>
    There are five sub-classes of QueryNode
    
    <ul>
    <li>Fixed - a concrete node, with pseudo-index NO_INDEX
    <li>Bind - a variable node's first, binding, appearance
    <li>JustBound - a variable node's bound appearance in the same
         [triple] context as its binding appearance
    <li>Bound - a variable node's bound appearance
    <li>Any - Node.ANY (or, in principle, a free variable)
    </ul>
    
    @author hedgehog
*/
public abstract class QueryNode
    {
    /**
        Internal exception to throw if, against all chance, something that
        shouldn't be involved in a match <i>is</i>.
        @author kers
    */
    public class MustNotMatchException extends BrokenException
        {
        public MustNotMatchException( String message )
            { super( message ); }
        }

    /**
        Fake index value to use when no index makes sense; we choose a value
        that will fail any array-bound check if it happens to be used anyway.
    */
    public static final int NO_INDEX = -1;
    
    /**
        The Node value on which this QueryNode is based.
    */
    public final Node node;
    
    /**
        The index value allocated to this query node; NO_INDEX except for a
        variable node, in which case it is the allocated index in the domain
        object.
    */
    public final int index;
    
    protected QueryNode( Node node )
        { this( node, NO_INDEX ); }
    
    protected QueryNode( Node node, int index )
        { this.node = node; this.index = index; }
    
    /**
        Return a handy string representation for debugging purposes. Not for
        machine consumption.
    */
    public String toString()
        { return node.toString() + "[" + index + "]"; }
    
    /**
        Answer a Node value to use when this QueryValue is used to select 
        objects in a Graph::find() operation; for concrete nodes, that very 
        node, for variables their current value (ANY if not bound).
    */
    public Node finder( Domain d )
        { return Node.ANY; }
    
    /**
        Answer true iff this QueryNode must be used in a triple-match of its
        owning QueryTriple.
    */
    public boolean mustMatch()
        { return false; }
    
    /**
        Answer true iff this QueryNode matches, in the context of the binding
        Domain <code>d</code>, the node <code>x</code>.
    */
    public boolean match( Domain d, Node x )
        { throw new MustNotMatchException( "QueryNode " + this + " cannot match" ); }
    
    /**
        Optimisation: the action to be performed when matching a just-bound
        variable or binding a newly-bound variable, or nothing for any other
        kind of QueryNode.
    */
    public abstract boolean matchOrBind( Domain d, Node x );
        
    /**
        Answer a QueryNode that classifies the argument node <code>n</code>.
        The factory <code>f</code> is used to create the different QueryNodes,
        allowing different classifiers to use their own subclasses of QueryNode
        if they wish. <code>map</code> is the variable-to-index map, and
        <code>recent</code> is the set of those variables "just" bound, ie,
        earlier in the same triple.
    */
    public static QueryNode classify
        ( QueryNodeFactory f, Mapping map, Set recent, Node n )
        {
        if (n.equals( Node.ANY ))
            return f.createAny();
        if (n.isVariable())
            {
            if (map.hasBound( n ))
                {
                if (recent.contains( n ))
                    return f.createJustBound( n, map.indexOf( n ) );
                else
                    return f.createBound( n, map.indexOf( n ) );
                }
            else
                {
                recent.add( n );
                return f.createBind( n, map.newIndex( n ) );
                }
            }
        return new Fixed( n );
        }
    
    public static final QueryNodeFactory factory = new QueryNodeFactoryBase();

    public static class Fixed extends QueryNode
        {
        public Fixed( Node n )
            { super( n ); }
        
        public Node finder( Domain d )
            { return node; }
        
        public boolean matchOrBind( Domain d, Node x )
            { return node.matches( x ); }
        
        public String toString()
            { return node.toString() + "[fixed]"; }
        }
    
    public static class Bind extends QueryNode
        {
        public Bind( Node n, int index )
            { super( n, index ); }
        
        public boolean mustMatch()
            { return true; }
        
        public boolean match( Domain d, Node value )
            { d.setElement( index, value );
            return true; }
        
        public boolean matchOrBind( Domain d, Node value )
            { d.setElement( index, value );
            return true; }
        
        public String toString()
            { return node.toString() + "[bind " + index + "]"; }
        }
    
    public static class JustBound extends QueryNode
        {
        public JustBound( Node n, int index )
            { super( n, index ); }  
        
        public boolean mustMatch()
            { return true; } 
        
        public boolean match( Domain d, Node X )
            { return X.matches( d.getElement( index ) ); }
        
        public boolean matchOrBind( Domain d, Node x )
            { return x.matches( d.getElement( index ) ); }

        public String toString()
            { return node.toString() + "[just " + index + "]"; }
        }
        
    public static class Bound extends QueryNode
        {
        public Bound( Node n, int index )
            { super( n, index ); }
        
        public Node finder( Domain d )
            { return d.getElement( index ); }
        
        public boolean matchOrBind( Domain d, Node x )
            { return d.getElement( index ).matches( x ); }
        
        public String toString()
            { return node.toString() + "[bound " + index + "]"; }
        }        

    public static class Any extends QueryNode
        {
        public Any()
            { super( Node.ANY ); }
        
        public boolean matchOrBind( Domain d, Node x )
            { return true; }
        
        public String toString()
            { return "ANY"; }
        }
    }
/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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