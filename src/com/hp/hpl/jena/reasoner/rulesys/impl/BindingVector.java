/******************************************************************
 * File:        BindingVector.java
 * Created by:  Dave Reynolds
 * Created on:  28-Apr-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BindingVector.java,v 1.13 2003-06-02 09:04:33 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;

import java.util.*;

/**
 * An implementation of a binding environment that maintains
 * a single array of bound values for the variables in a rule.
 * Stack management is done externally. This is intended for use in
 * the Brule system and so also supports variable-variable bindings by
 * use of reference chains.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.13 $ on $Date: 2003-06-02 09:04:33 $
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
     * Constructor - create a binding environment from a vector of bindings 
     */
    public BindingVector(Node [] env) {
        environment = env; 
    }
    
    /**
     * Constructor - create a binding environment which is a copy
     * of the given environment
     */
    public BindingVector(BindingVector clone) {
        Node[] orig = clone.environment;
        environment = new Node[orig.length];
        System.arraycopy(orig, 0, environment, 0, orig.length); 
    }
    
    /**
     * Return the current array of bindings. Useful for fast access to
     * serveral bindings, not useful for doing updates.
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
            Node val = environment[((Node_RuleVariable)node).getIndex()];
            if (val instanceof Node_RuleVariable) {
                return getBinding(val);
            } else {
                return val;
            }
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
     * Handles aliased variables.
     * @return false if the binding fails
     */
    public boolean bind(int i, Node value) {
        Node node = environment[i];
        if (node == null) {
            environment[i] = value;
            return true;
        } else if (node instanceof Node_RuleVariable) {
            environment[i] = value;
            return bind(((Node_RuleVariable)node).getIndex(), value);
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
     * Bind the variables in a goal pattern using the binding environment, to
     * generate a more specialized goal
     * @param goal the TriplePattern to be instantiated
     * @return a TriplePattern obtained from the goal by substituting current bindinds
     */
    public TriplePattern partInstantiate(TriplePattern goal) {
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
    
    /**
     * Printable form
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < environment.length; i++) {
            if (environment[i] == null) {
                buffer.append("-");
            } else {
                buffer.append(environment[i].toString());
            }
            buffer.append(" ");
        }
        return buffer.toString();
    }
    
    /**
     * Unify a goal with the head of a rule. 
     * @param goal the goal pattern which it being matched to a rule
     * @param head the head pattern of the rule which is being instantiated
     * @return An initialized binding environment for the rule variables
     * or null if the unificatin fails. If a variable in the environment becomes
     * aliased to another variable through the unification this is represented
     * by having its value in the environment be the variable to which it is aliased.
     */ 
    public static BindingVector unify(TriplePattern goal, TriplePattern head) {
        return unify(goal, head, BindingStack.MAX_VAR);
    }
        
    /**
     * Unify a goal with the head of a rule. This is a poor-man's unification,
     * we should try swtiching to a more conventional global-variables-with-trail
     * implementation in the future.
     * @param goal the goal pattern which it being matched to a rule
     * @param head the head pattern of the rule which is being instantiated
     * @param numRuleVars the length of the environment to allocate.
     * @return An initialized binding environment for the rule variables
     * or null if the unification fails. If a variable in the environment becomes
     * aliased to another variable through the unification this is represented
     * by having its value in the environment be the variable to which it is aliased.
     */ 
    public static BindingVector unify(TriplePattern goal, TriplePattern head, int numRuleVars) {
        Node[] gEnv = new Node[BindingStack.MAX_VAR];
        Node[] hEnv = new Node[numRuleVars];
        
        if (!unify(goal.getSubject(), head.getSubject(), gEnv, hEnv)) {
            return null;
        } 
        if (!unify(goal.getPredicate(), head.getPredicate(), gEnv, hEnv)) {
            return null; 
        } 
        
        Node gObj = goal.getObject();
        Node hObj = head.getObject();
        if (Functor.isFunctor(gObj)) {
            Functor gFunctor = (Functor)gObj.getLiteral().getValue();
            if (Functor.isFunctor(hObj)) {
                Functor hFunctor = (Functor)hObj.getLiteral().getValue();
                if ( ! gFunctor.getName().equals(hFunctor.getName()) ) {
                    return null;
                }
                Node[] gArgs = gFunctor.getArgs();
                Node[] hArgs = hFunctor.getArgs();
                if ( gArgs.length != hArgs.length ) return null;
                for (int i = 0; i < gArgs.length; i++) {
                    if (! unify(gArgs[i], hArgs[i], gEnv, hEnv) ) {
                        return null;
                    }
                }
            } else if (hObj instanceof Node_RuleVariable) {
                // temp debug ...
                // Check the goal functor is fully ground
                if (gFunctor.isGround(new BindingVector(gEnv))) {
                    if (!unify(gObj, hObj, gEnv, hEnv)) return null;
                }
                // ... end debug
            } else {
                // unifying simple ground object with functor, failure
                return null;
            }
        } else {
            if (!unify(gObj, hObj, gEnv, hEnv)) return null;
        } 
        // Successful bind if we get here
        return new BindingVector(hEnv);
    }
    
    /**
     * Unify a single pair of goal/head nodes. Unification of a head var to
     * a goal var is recorded using an Integer in the head env to point to a
     * goal env and storing the head var in the goal env slot.
     * @return true if they are unifiable, side effects the environments
     */
    private static boolean unify(Node gNode, Node hNode, Node[] gEnv, Node[] hEnv) {
        if (hNode instanceof Node_RuleVariable) {
            int hIndex = ((Node_RuleVariable)hNode).getIndex();
            if (gNode instanceof Node_RuleVariable) {
                // Record variable bind between head and goal to detect aliases
                int gIndex = ((Node_RuleVariable)gNode).getIndex();
                if (gIndex < 0) return true;
                if (gEnv[gIndex] == null) {
                    // First time bind so record link 
                    gEnv[gIndex] = hNode;
                } else {
                    // aliased var so follow trail to alias
                    // but ignore self-aliases
                    Node gVal = gEnv[gIndex];
                    if (hIndex != gIndex || ! (gVal instanceof Node_RuleVariable)) {
                        hEnv[hIndex] = gVal;
                    }
                }
            } else {
                Node hVal = hEnv[hIndex];
                if (hVal == null) {
                    hEnv[hIndex] = gNode;
                } else {
                    // Already bound
                    if (hVal instanceof Node_RuleVariable) {
                        // Already an aliased variable, so bind both this an the alias
                        hEnv[((Node_RuleVariable)hVal).getIndex()] = gNode;
                        hEnv[hIndex] = gNode;
                    } else {
                        // Already bound to a ground node
                        return hVal.sameValueAs(gNode); 
                    }
                }
            }
            return true;
        } else {
            if (gNode instanceof Node_RuleVariable) {
                int gIndex = ((Node_RuleVariable)gNode).getIndex();
                if (gIndex < 0) return true;
                Node gVal = gEnv[gIndex]; 
                if (gVal == null) {
                    //. No variable alias so just record binding
                    gEnv[gIndex] = hNode;
                } else if (gVal instanceof Node_RuleVariable) {
                    // Already an alias
                    hEnv[((Node_RuleVariable)gVal).getIndex()] = hNode;
                    gEnv[gIndex] = hNode;
                } else {
                    return gVal.sameValueAs(hNode);
                }
                return true;
            } else {
                return hNode.sameValueAs(gNode); 
            }
        }
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
