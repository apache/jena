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
    @Override
    public String toString()
        { return node.toString() + "[" + index + "]"; }

    /**
        Answer true iff this node is "frozen", ie its value is fixed, when it
        is encountered; that is, it is not a Bind or JustBound node.
    */
    public boolean isFrozen()
        { return true; }
    
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
        ( QueryNodeFactory f, Mapping map, Set<Node> recent, Node n )
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
        
        @Override
        public Node finder( Domain d )
            { return node; }
        
        @Override
        public boolean matchOrBind( Domain d, Node x )
            { return node.matches( x ); }
        
        @Override
        public String toString()
            { return node.toString() + "[fixed]"; }
        }
    
    public static class Bind extends QueryNode
        {
        public Bind( Node n, int index )
            { super( n, index ); }
        
        @Override
        public boolean mustMatch()
            { return true; }
        
        @Override
        public boolean isFrozen()
            { return false; }
        
        @Override
        public boolean match( Domain d, Node value )
            { d.setElement( index, value );
            return true; }
        
        @Override
        public boolean matchOrBind( Domain d, Node value )
            { d.setElement( index, value );
            return true; }
        
        @Override
        public String toString()
            { return node.toString() + "[bind " + index + "]"; }
        }
    
    public static class JustBound extends QueryNode
        {
        public JustBound( Node n, int index )
            { super( n, index ); }  
        
        @Override
        public boolean mustMatch()
            { return true; } 
        
        @Override
        public boolean isFrozen()
            { return false; }
        
        @Override
        public boolean match( Domain d, Node X )
            { return X.matches( d.getElement( index ) ); }
        
        @Override
        public boolean matchOrBind( Domain d, Node x )
            { return x.matches( d.getElement( index ) ); }

        @Override
        public String toString()
            { return node.toString() + "[just " + index + "]"; }
        }
        
    public static class Bound extends QueryNode
        {
        public Bound( Node n, int index )
            { super( n, index ); }
        
        @Override
        public Node finder( Domain d )
            { return d.getElement( index ); }
        
        @Override
        public boolean matchOrBind( Domain d, Node x )
            { return d.getElement( index ).matches( x ); }
        
        @Override
        public String toString()
            { return node.toString() + "[bound " + index + "]"; }
        }        

    public static class Any extends QueryNode
        {
        public Any()
            { super( Node.ANY ); }
        
        @Override
        public boolean matchOrBind( Domain d, Node x )
            { return true; }
        
        @Override
        public String toString()
            { return "ANY"; }
        }
    }
