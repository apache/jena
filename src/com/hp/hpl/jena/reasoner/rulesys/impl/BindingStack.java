/******************************************************************
 * File:        BindingStack.java
 * Created by:  Dave Reynolds
 * Created on:  28-Apr-03
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: BindingStack.java,v 1.7 2005-02-21 12:17:39 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

import java.util.*;

/**
 * Provides a trail of possible variable bindings for a forward rule.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2005-02-21 12:17:39 $
 */
public class BindingStack implements BindingEnvironment {
    
    // Current slightly quirky implementation tries to avoid allocating
    // store in which will be an inner loop. Does one array copy on
    // push but handles both versions of pop with point manipulation.
    
    /** The current binding set */
    protected Node[] environment;
    
    /** A stack of prior binding sets */
    protected ArrayList trail = new ArrayList();
    
    /** Index of the current binding set */
    protected int index = 0;
    
    /** Index of maximum allocated slot in the trail */
    protected int highWater = 0;
    
    /** Maximum number of distinct variables allowed in rules */
    protected static final int MAX_VAR = 10;
    
    /**
     * Constructor
     */
    public BindingStack() {
        trail.add(new Node[MAX_VAR]);
        environment = (Node[])trail.get(0);
        index = highWater = 0;
    }
    
    /**
     * Save the current environment on an internal stack 
     */
    public void push() {
        if (index == highWater) {
            trail.add(new Node[MAX_VAR]);
            highWater++;
        }
        Node[] newenv = (Node[]) trail.get(++index);
        System.arraycopy(environment, 0, newenv, 0, MAX_VAR);
        environment = newenv;
    }
    
    /**
     * Forget the current environment and return the previously
     * pushed state.
     * @throws IndexOutOfBoundsException if there was not previous push
     */
    public void unwind() throws IndexOutOfBoundsException {
        if (index > 0) {
            // just point to previous stack entry
            environment = (Node[]) trail.get(--index);
        } else {
            throw new IndexOutOfBoundsException("Underflow of BindingEnvironment");
        }
    }
    
    /**
     * Forget the previously pushed state but keep the current environment.
     * @throws IndexOutOfBoundsException if there was not previous push
     */
    public void commit() throws IndexOutOfBoundsException {
        if (index > 0) {
            // Swap top and previous stack entries and point to previous
            Node[] newenv = (Node[]) trail.get(index-1);
            trail.set(index-1, environment);
            trail.set(index, newenv);
            --index;
        } else {
            throw new IndexOutOfBoundsException("Underflow of BindingEnvironment");
        }
    }
   
    /**
     * Reset the binding environment to empty.
     */
    public void reset() {
        index = 0;
        environment = (Node[]) trail.get(0);
        Arrays.fill(environment, null);
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
                    // Not sufficent bound to instantiate functor yet
                    return null;
                }
                boundargs.add(binding);
            }
            Functor newf = new Functor(functor.getName(), boundargs);
            return Functor.makeFunctorNode( newf );
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
     * Instantiate a triple pattern against the current environment.
     * This version handles unbound varibles by turning them into bNodes.
     * @param clause the triple pattern to match
     * @param env the current binding environment
     * @return a new, instantiated triple
     */
    public Triple instantiate(TriplePattern pattern) {
        Node s = getGroundVersion(pattern.getSubject());
        if (s.isVariable()) s = Node.createAnon();
        Node p = getGroundVersion(pattern.getPredicate());
        if (p.isVariable()) p = Node.createAnon();
        Node o = getGroundVersion(pattern.getObject());
        if (o.isVariable()) o = Node.createAnon();
        return new Triple(s, p, o);
    }
    
}

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

