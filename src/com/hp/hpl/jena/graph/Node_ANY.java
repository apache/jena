package com.hp.hpl.jena.graph;

/**
	@author kers
<br>
    A Node_ANY (there should be only one) is a meta-node that is used to stand
    for any other node in a query.
*/

public class Node_ANY extends Node
    {
    public Node_ANY() { super( "" ); }
    
    /** Node_ANY's are only equal to other Node_ANY's */
    public boolean equals( Object other )
        { return other instanceof Node_ANY; }
        
    public Object visitWith( NodeVisitor v )
        { return v.visitAny( this ); }
        
    public boolean matches( Node other )
        { return other != null; }
    }
