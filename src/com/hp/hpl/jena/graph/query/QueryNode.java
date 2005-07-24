/**
 * 
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
public class QueryNode
    {
    public class MustNotMatchException extends BrokenException
        {
        public MustNotMatchException( String message )
            { super( message ); }
        }

    public static final int NO_INDEX = -1;
    
    public final Node node;
    public final int index;
    
    protected QueryNode( Node node )
        { this( node, NO_INDEX ); }
    
    protected QueryNode( Node node, int index )
        { this.node = node; this.index = index; }
    
    public String toString()
        { return node.toString() + "[" + index + "]"; }
    
    public Node finder( Domain d )
        { return Node.ANY; }
    
    public boolean mustMatch()
        { return false; }
    
    public boolean match( Domain d, Node x )
        { throw new MustNotMatchException( "QueryNode " + this + " cannot match" ); }
    
    public static QueryNode classify( Mapping map, Set recent, Node n )
        {
        if (n.equals( Node.ANY ))
            return new Any();
        if (n.isVariable())
            {
            if (map.hasBound( n ))
                {
                if (recent.contains( n ))
                    return new JustBound( n, map.indexOf( n ) );
                else
                    return new Bound( n, map.indexOf( n ) );
                }
            else
                {
                recent.add( n );
                return new Bind( n, map.newIndex( n ) );
                }
            }
        return new Fixed( n );
        }
    
    public static class Fixed extends QueryNode
        {
        public Fixed( Node n )
            { super( n ); }
        
        public Node finder( Domain d )
            { return node; }
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
        }
    
    public static class JustBound extends QueryNode
        {
        public JustBound( Node n, int index )
            { super( n, index ); }  
        
        public boolean mustMatch()
            { return true; } 
        
        public boolean match( Domain d, Node X )
            { return X.matches( d.getElement( index ) ); }
        }
        
    public static class Bound extends QueryNode
        {
        public Bound( Node n, int index )
            { super( n, index ); }
        
        public Node finder( Domain d )
            { return  d.getElement( index ); }
        }        

    public static class Any extends QueryNode
        {
        public Any()
            { super( Node.ANY ); }
        }
    }