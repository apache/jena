/******************************************************************
 * File:        Node_RuleVariable.java
 * Created by:  Dave Reynolds
 * Created on:  30-Mar-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Node_RuleVariable.java,v 1.2 2003-05-05 15:16:00 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.Node_Variable;

/**
 * A variation on the normal Node_Variable which includes an
 * index value (an offset into a binding table). 
 * <p>
 * Note that this should not be used in a real Triple, in particular
 * it should not end up in a Graph. It is only needed for the rule systems. </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-05-05 15:16:00 $
 */
public class Node_RuleVariable extends Node_Variable {

    /** The offset of this variable in the rule's binding table */
    protected int index;
    
    /**
     * Constructor
     * @param label the text label for the variable
     * @param index the calculated index of this variable in the rule
     */
    public Node_RuleVariable(String label, int index) {
        super(label);
        this.index = index;
    }
    
    /**
     * Returns the index.
     * @return int
     */
    public int getIndex() {
        return index;
    }
    
    /** Equality override - all rule variables are treated as equal
     *  to support easy variant matching. */
    public boolean equals(Object o) {
        return o instanceof Node_RuleVariable;
    }
        
    /** hash function override - all vars have same hash code to support fast
     *  search of variant tables */
    public int hashCode() {
        return 0xc3a7;
    }

}
