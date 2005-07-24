/**
 * 
 */
package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.Node;

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
public class QueryNode
    {
    public static final int NO_INDEX = -1;
    
    public final Node node;
    public final int index;
    
    protected QueryNode( Node node )
        { this( node, NO_INDEX ); }
    
    protected QueryNode( Node node, int index )
        { this.node = node; this.index = index; }
    
    public String toString()
        { return node.toString() + "[" + index + "]"; }
    
    public static class Fixed extends QueryNode
        {
        public Fixed( Node n )
            { super( n ); }
        }
    
    public static class Bind extends QueryNode
        {
        public Bind( Node n, int index )
            { super( n, index ); }
        }
    
    public static class Bound extends QueryNode
        {
        public Bound( Node n, int index )
            { super( n, index ); }
        }        
    
    public static class JustBound extends QueryNode
        {
        public JustBound( Node n, int index )
            { super( n, index ); }
        }
    
    public static class Any extends QueryNode
        {
        public Any()
            { super( Node.ANY ); }
        }
    }