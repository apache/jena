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

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.PrintUtil;

import java.util.*;

/**
 * An implementation of a binding environment that maintains
 * a single array of bound values for the variables in a rule.
 * Stack management is done externally. This is intended for use in
 * the Brule system and so also supports variable-variable bindings by
 * use of reference chains.
 */
public class BindingVector implements BindingEnvironment {
    
    /** The current binding set */
    protected Node[] environment;
    
    /**
     * Constructor - create an empty binding environment 
     */
    public BindingVector(int size) {
        environment = new Node[size]; 
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
            Functor functor = (Functor)node.getLiteralValue();
            if (functor.isGround()) return node;
            Node[] args = functor.getArgs();
            List<Node> boundargs = new ArrayList<>(args.length);
            for ( Node arg : args )
            {
                Node binding = getBinding( arg );
                if ( binding == null )
                {
                    // Not sufficently bound to instantiate functor yet
                    return null;
                }
                boundargs.add( binding );
            }
            Functor newf = new Functor( functor.getName(), boundargs );
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
    @Override
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
    @Override
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
    
// Replaced by version below for consistency with stack variant    
//    /**
//     * Instatiate a goal pattern using the binding environment
//     * @param goal the TriplePattern to be instantiated
//     * @return an instantiated Triple
//     */
//    public Triple instantiate(TriplePattern goal) {
//        return new Triple(
//                getGroundVersion(goal.getSubject()),
//                getGroundVersion(goal.getPredicate()),
//                getGroundVersion(goal.getObject())
//        );
//    }
    
    /**
     * Instantiate a triple pattern against the current environment.
     * This version handles unbound varibles by turning them into bNodes.
     * @param pattern the triple pattern to match
     * @return a new, instantiated triple
     */
    @Override
    public Triple instantiate(TriplePattern pattern) {
        Node s = getGroundVersion(pattern.getSubject());
        if (s.isVariable()) s = NodeFactory.createAnon();
        Node p = getGroundVersion(pattern.getPredicate());
        if (p.isVariable()) p = NodeFactory.createAnon();
        Node o = getGroundVersion(pattern.getObject());
        if (o.isVariable()) o = NodeFactory.createAnon();
        return new Triple(s, p, o);
    }
    
    /**
     * Printable form
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for ( Node anEnvironment : environment )
        {
            if ( anEnvironment == null )
            {
                buffer.append( "-" );
            }
            else
            {
                buffer.append( PrintUtil.print( anEnvironment ) );
            }
            buffer.append( " " );
        }
        return buffer.toString();
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
        Node[] gEnv = new Node[numRuleVars];       // TODO: check
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
            Functor gFunctor = (Functor)gObj.getLiteralValue();
            if (Functor.isFunctor(hObj)) {
                Functor hFunctor = (Functor)hObj.getLiteralValue();
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
  
    /** Equality override */
    @Override
    public boolean equals(Object o) {
        // Pass 1 - just check basic shape
        if (! (o instanceof BindingVector) ) return false;
        Node[] other = ((BindingVector)o).environment;
        if (environment.length != other.length) return false;
        for (int i = 0; i < environment.length; i++) {
            Node n = environment[i];
            Node no = other[i];
            if (n == null) {
                if (no != null) return false;
            } else {
                if (! n.sameValueAs(no)) return false;
            }
        }
        return true;
    }
        
    /** hash function override */
    @Override
    public int hashCode() {
        int hash = 0;
        for ( Node n : environment )
        {
            hash = ( hash << 1 ) ^ ( n == null ? 0x537c : n.hashCode() );
        }
        return hash;
    }
    

}
