package com.hp.hpl.jena.graph;

/**
	@author kers
*/

public class Node_ANY extends Node
    {
    public Node_ANY() { super( "" ); }
    
    public boolean equals( Object other )
        { return other instanceof Node_ANY; }
    }
