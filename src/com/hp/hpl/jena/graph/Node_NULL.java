package com.hp.hpl.jena.graph;

/**
    A Node_ANY (there should be only one) is a meta-node that is used to stand
    for any other node in a query.
    @author kers
*/

public class Node_NULL extends Node_Concrete
    {
    public Node_NULL() { super( "" ); }
    
    /** Node_NULL's are equal to no null nodes; strictly speaking,
     *  this incorrect but suits our purposes. really want an isNull
     *  method on Node but that's too much surgery. */
    
    public boolean equals( Object other )
        { return other instanceof Node_NULL; }
        
    public Object visitWith( NodeVisitor v )
        { return null; }
                
    public String toString()
        { return "NULL"; }
    }
