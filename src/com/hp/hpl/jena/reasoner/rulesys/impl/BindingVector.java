/******************************************************************
 * File:        BindingVector.java
 * Created by:  Dave Reynolds
 * Created on:  28-Apr-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BindingVector.java,v 1.2 2003-05-05 21:52:42 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;

import java.util.*;

/**
 * An implementation of a binding environment that maintains
 * a single array of bound values for the variables in a rule.
 * Stack management is done externally.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-05-05 21:52:42 $
 */
public class BindingVector implements BindingEnvironment {
    
    /** The current binding set */
    protected Node[] environment;
    
    /**
     * Constructor - create an empty binding environment 
     */
    public BindingVector() {
        environment = new Node[BindingStack.MAX_VAR]; 
    }
    
    /**
     * Constructor - create a binding environment which is a copy
     * of the given environment
     */
    public BindingVector(BindingVector clone) {
        Node[] orig = clone.environment;
        environment = new Node[BindingStack.MAX_VAR];
        System.arraycopy(orig, 0, environment, 0, BindingStack.MAX_VAR); 
    }
    
    /**
     * Return the current array of bindings
     */
    public Node[] getEnvironment() {
        return environment;
    }
        
    /**
     * If the node is a variable then return the current binding (null if not bound)
     * otherwise return the node itself.
     */
    public Node getBinding(Node node) {
        if (node instanceof Node_RuleVariable) {
            return environment[((Node_RuleVariable)node).getIndex()];
        } else if (node instanceof Node_ANY) {
            return null;
        } else if (Functor.isFunctor(node)) {
            Functor functor = (Functor)node.getLiteral().getValue();
            if (functor.isGround()) return node;
            Node[] args = functor.getArgs();
            ArrayList boundargs = new ArrayList(args.length);
            for (int i = 0; i < args.length; i++) {
                Object binding = getBinding(args[i]);
                if (binding == null) {
                    // Not sufficently bound to instantiate functor yet
                    return null;
                }
                boundargs.add(binding);
            }
            Functor newf = new Functor(functor.getName(), boundargs);
            LiteralLabel ll = new LiteralLabel(newf, null, Functor.FunctorDatatype.theFunctorDatatype);
            return new Node_Literal(ll);
        } else {
            return node;
        }
    }
    
    /**
     * Return the most ground version of the node. If the node is not a variable
     * just return it, if it is a varible bound in this enviroment return the binding,
     * if it is an unbound variable return the variable.
     */
    public Node getGroundVersion(Node node) {
        Node bind = getBinding(node);
        if (bind == null) {
            return node;
        } else {
            return bind;
        }
    }
    
    /**
     * Bind the ith variable in the current envionment to the given value.
     * Checks that the new binding is compatible with any current binding.
     * @return false if the binding fails
     */
    public boolean bind(int i, Node value) {
        Node node = environment[i];
        if (node == null) {
            environment[i] = value;
            return true;
        } else {
            return node.sameValueAs(value);
        }
    }
    
    /**
     * Bind a variable in the current envionment to the given value.
     * Checks that the new binding is compatible with any current binding.
     * @param var a Node_RuleVariable defining the variable to bind
     * @param value the value to bind
     * @return false if the binding fails
     */
    public boolean bind(Node var, Node value) {
        if (var instanceof Node_RuleVariable) {
            return bind(((Node_RuleVariable)var).getIndex(), value);
        } else {
            return var.sameValueAs(value);
        }
    }
    
    /**
     * Bind a variable in the current envionment to the given value.
     * Overrides and ignores any current binding.
     * @param var a Node_RuleVariable defining the variable to bind
     * @param value the value to bind
     */
    public void bindNoCheck(Node_RuleVariable var, Node value) {
        environment[var.getIndex()] = value;
    }
    
    /**
     * Bind the variables in a goal pattern using the binding environment, to
     * generate a more specialized goal
     * @param goal the TriplePattern to be instantiated
     * @return a TriplePattern obtained from the goal by substituting current bindinds
     */
    public TriplePattern bind(TriplePattern goal) {
        return new TriplePattern(
                getGroundVersion(goal.getSubject()),
                getGroundVersion(goal.getPredicate()),
                getGroundVersion(goal.getObject())
        );
    }
    
    /**
     * Instatiate a goal pattern using the binding environment
     * @param goal the TriplePattern to be instantiated
     * @return an instantiated Triple
     */
    public Triple instantiate(TriplePattern goal) {
        return new Triple(
                getGroundVersion(goal.getSubject()),
                getGroundVersion(goal.getPredicate()),
                getGroundVersion(goal.getObject())
        );
    }
    
}



/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
