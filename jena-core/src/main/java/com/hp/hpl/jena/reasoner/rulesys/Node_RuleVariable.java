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

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * A variation on the normal Node_Variable which support for value bindings.
 * Currently the forward rule system stores the values externally but requires
 * variables to have an offset index in the rule environment vector. The
 * variables can also suport prolog-like reference chains and trails but these
 * are not yet used.
 * <p>
 * Note that this should not be used in a real Triple, in particular
 * it should not end up in a Graph. It is only needed for the rule systems. </p>
 */
public class Node_RuleVariable extends Node_Variable {
    /** The offset of this variable in the Frule's binding table */
    protected int index;

    /** The value to which this variable is bound, can be another variable,
     *  itself (meaning unbound) or an actual value */
    protected Node value;
    
    /** A flag to indicate that the value is reference (pointer to a var) */
    protected boolean isRef = true;
    
    /** A static wildcard - like Node.ANY but tests equl to other Node_RuleVariables */
    public static final Node_RuleVariable WILD = new Node_RuleVariable("*", -1);
         
    /**
     * Constructor
     * @param label the text label for the variable
     * @param index the calculated index of this variable in the rule
     */
    public Node_RuleVariable(String label, int index) {
        super(label);
        this.index = index;
        this.value = this;
    }
    
    /**
     * Returns the variable's index in a binding vector.
     * @return int
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Changes the variable's index. This is used in LP rules which classify the
     * variables into different sequences.
     */
    public void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * Return an indexable object for this Node. This is actually the 
     * rule label. This is weird but needed because equals is (deliberately)
     * perverse on Node_RuleVariable so if we want to put then in a Set or Map
     * we need something with a better equals function.
     */
//    public Object getRepresentative() {
//        return label;
//    }
    
    /**
     * Binds a value to the brule version of the variable. Does not follow
     * any reference trail, assues we have already been derefenced.
     * @param node a concrete Node value or another Node_RuleVariable
     * to alias to
     */
    public void simpleBind(Node node) {
        value = node;
        isRef = node instanceof Node_RuleVariable;
    }
    
    /**
     * Dereference a variable by following the reference chain.
     * @return either a concrete node value or the last variable
     * in the reference chain.
     */
    public Node deref() {
        Node_RuleVariable var = this;
        while (var.isRef) {
            if (var.value == var) {
                return var;
            }
            var = (Node_RuleVariable)var.value;
        }
        return var.value;
    }
    
    /**
     * Return the raw value to which this variable is bound (via LP binding) with
     * no dereferencing.
     */
    public Node getRawBoundValue() {
        return value;
    }
    
    /**
     * Set the variable to be unbound (in the brule sense)
     */
    public void unbind() {
        isRef = true;
        value = this;
    }
    
    /**
     * Test if the variable is unbound (in the brule sense).
     */
    public boolean isUnbound() {
        return (isRef && (value == this));
    }
    
    /**
     * Clone the rule variable to allow multiple rule instaces to be active at the same time.
     */
    public Node_RuleVariable cloneNode() {
        return new Node_RuleVariable(getName(), index);        
    }
    
    /** printable form */        
    @Override
    public String toString() {
        if (getName() == null) return "*";
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node_RuleVariable) {
            String name = getName();
            if (name == null) {
                return this == o;
            } else {
                return name.equals( ((Node_RuleVariable)o).getName() );
            }
        } else 
            return false;
    }

    @Override
    public int hashCode() {
        String name = getName();
        if (name == null) {
            return 0xc3a7;
        } else {
            return name.hashCode();
        }
    }

    /**
     * Test that two nodes are semantically equivalent.
     */
    @Override
    public boolean sameValueAs(Object o) {
        return o instanceof Node_RuleVariable;
    }

    /**
     * Compare two nodes, taking into account variable indices.
     */
    public static boolean sameNodeAs(Node n, Node m) {
        if (n instanceof Node_RuleVariable) {
            if (m instanceof Node_RuleVariable) {
                return ((Node_RuleVariable)n).getIndex() == ((Node_RuleVariable)m).getIndex();
            } else {
                return false;
            }
        } else {
            return n.sameValueAs(m);
        }
    }

}
