/**
 * 
 */
package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.Node;

public class QueryNode
    {
    public static final int NO_INDEX = -1;
    
    public final Node node;
    public final int index;
    
    protected QueryNode( Node node )
        { this( node, NO_INDEX ); }
    
    protected QueryNode( Node node, int index )
        { this.node = node; this.index = index; }
    
    public static class Fixed extends QueryNode
        {
        public Fixed( Node n )
            { super( n ); }
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