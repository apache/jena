/******************************************************************
 * File:        Trail.java
 * Created by:  Dave Reynolds
 * Created on:  20-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Trail.java,v 1.1 2003-05-20 17:31:37 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
 * Representation of a trail of variable bindings. Each rule state has its
 * own trail segment which is an instance of this class.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-05-20 17:31:37 $
 */
public class Trail implements BindingEnvironment {
    
    /** A trail of variable bindings made during the processing of this state */
    protected ArrayList trail = new ArrayList();
    
    /**
     * Unwind all the bindings on the trail.
     */
    public void unwindBindings() {
        for (int i = trail.size() - 1; i >= 0; i--) {
            TrailEntry entry = (TrailEntry)trail.get(i);
            entry.var.unbind();
        }
    }
    
    /**
     * Unwind all the bindings on the trail the forget them all.
     */
    public void unwindAndClear() {
        for (int i = trail.size() - 1; i >= 0; i--) {
            TrailEntry entry = (TrailEntry)trail.get(i);
            entry.var.unbind();
        }
        trail.clear();
    }
    
    /**
     * Restore the set of trail bindings.
     */
    public void activate() {
        for (Iterator i = trail.iterator(); i.hasNext(); ) {
            TrailEntry entry = (TrailEntry)i.next();
            entry.var.simpleBind(entry.value);
        }
    }
    
    /**
     * Unify two triple patterns recording all of the bindings on the trail.
     * If the unification fails returns false and the trail is left unchanged.
     */
    public boolean unify(TriplePattern t, TriplePattern tp) {
        int watermark = trail.size();
        boolean ok =  unify(t.getSubject(), tp.getSubject())
                    && unify(t.getPredicate(), tp.getPredicate())
                    && unifyObj(t.getObject(), tp.getObject());
        if (!ok) {
            for (int i = trail.size() - 1; i >= watermark; i--) {
                TrailEntry entry = (TrailEntry)trail.get(i);
                entry.var.unbind();
                trail.remove(i);
            }
        }
        return ok;
    }

    /**
     * Unify a triple against a triple pattern recording all of the bindings on the trail.
     * If the unification fails returns false and the trail is left unchanged.
     */
    public boolean unify(Triple t, TriplePattern tp) {
        int watermark = trail.size();
        boolean ok =  unify(t.getSubject(), tp.getSubject())
                    && unify(t.getPredicate(), tp.getPredicate())
                    && unifyObj(t.getObject(), tp.getObject());
        if (!ok) {
            for (int i = trail.size() - 1; i >= watermark; i--) {
                TrailEntry entry = (TrailEntry)trail.get(i);
                entry.var.unbind();
                trail.remove(i);
            }
        }
        return ok;
    }
        
    /**
     * Unify two nodes, neither can be a literal.
     */
    public boolean unify(Node n1, Node n2) {
        Node dn1 = getGroundVersion(n1);
        Node dn2 = getGroundVersion(n2);
        if (dn1 instanceof Node_RuleVariable) {
            bind(dn1, dn2);
            return true;
        } else if (dn2 instanceof Node_RuleVariable) {
            bind(dn2, dn1);
            return true;
        } else {
            return dn1.sameValueAs(dn2);
        }
    }
        
    /**
     * Unify two nodes, can be a literals.
     */
    public boolean unifyObj(Node n1, Node n2) {
        Node dn1 = getGroundVersion(n1);
        Node dn2 = getGroundVersion(n2);
        if (dn1 instanceof Node_RuleVariable) {
            bind(dn1, dn2);
            return true;
        } else if (dn2 instanceof Node_RuleVariable) {
            bind(dn2, dn1);
            return true;
        } else {
            // Both are ground, either functors or literals
            if (Functor.isFunctor(dn1)) {
                if (Functor.isFunctor(dn2)) {
                    // Unify functors
                    Functor f1 = (Functor)dn1.getLiteral().getValue();
                    Functor f2 = (Functor)dn2.getLiteral().getValue();
                    if ( ! f1.getName().equals(f2.getName()) ) return false;
                    Node[] args1 = f1.getArgs();
                    Node[] args2 = f2.getArgs();
                    if (args1.length != args2.length) return false;
                    for (int i = 0; i < args1.length; i++) {
                        if (! unify(args1[i], args2[i]) ) return false;
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return dn1.sameValueAs(dn2);
            }
        }
    }
   
    /**
     * Return the most ground version of the node. If the node is not a variable
     * just return it, if it is a varible bound in this environment return the binding,
     * if it is an unbound variable return the variable.
     */
    public Node getGroundVersion(Node node) {
        if (node instanceof Node_RuleVariable) {
            return ((Node_RuleVariable)node).deref();
        } else {
            return node;
        }
    }
   
    /**
     * Return the most ground version of the node. This extends getGroundVersion by
     * also grounding any embedded functors.
     */
    public Node getMostGroundVersion(Node node) {
        if (node instanceof Node_RuleVariable) {
            node = ((Node_RuleVariable)node).deref();
        }
        if (Functor.isFunctor(node)) {
            Functor f = (Functor) node.getLiteral().getValue();
            Node[] args = f.getArgs();
            Node[] cargs = new Node[args.length];
            for (int i = 0; i < args.length; i++) {
                cargs[i] = getGroundVersion(args[i]);
            }
            return Functor.makeFunctorNode(f.getName(), cargs);
        } else {
            return node;
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
        if (var == Node_RuleVariable.WILD || value == Node_RuleVariable.WILD) return true;
        Node dvar = getGroundVersion(var);
        if (dvar instanceof Node_RuleVariable) {
            trail.add(new TrailEntry((Node_RuleVariable)dvar, value));
            ((Node_RuleVariable)dvar).simpleBind(value);
            return true;
        } else {
            return dvar.sameValueAs(value);
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
                getMostGroundVersion(goal.getSubject()),
                getMostGroundVersion(goal.getPredicate()),
                getMostGroundVersion(goal.getObject())
        );
    }
    
    /**
     * Inner class used to represent an entry on the binding trail.
     */
    static class TrailEntry {
        /** The deferenced var which was bound */
        protected Node_RuleVariable var;
        
        /** The value to which it was bound */
        protected Node value;
        
        /** constructor */
        TrailEntry(Node_RuleVariable var, Node value) {
            this.var = var;
            this.value = value;
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