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
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * Frame on the choice point stack used to represent the state of some form of triple
 * match - this is either a direct graph query or a query to a cached set of results.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type and assumes privileged access to the interpreter state.
 * </p>
 */
public class GenericTripleMatchFrame extends GenericChoiceFrame {
    
    /** The variable to the subject of the triple, null if no binding required */
    Node_RuleVariable subjectVar;
    
    /** The variable to the predicate of the triple, null if no binding required */
    Node_RuleVariable predicateVar;
    
    /** The variable to the subject of the triple, null if no binding required */
    Node_RuleVariable objectVar;
    
    /** The functor variable structure to explode the object into, null if not required */
    Functor objectFunctor;
    
    /** The goal state being evaluated */
    TriplePattern goal;

    /**
     * Bind the goal variables to the given result triple.
     * Returns false if the triple doesn't match the goal (due to a functor match failure).
     */
    public boolean bindResult(Triple triple, LPInterpreter interpreter) {
        if (objectVar != null)    interpreter.bind(objectVar,    triple.getObject());
        int mark = interpreter.trail.size();
        if (objectFunctor != null) {
            if (! functorMatch(triple, interpreter)) {
                interpreter.unwindTrail(mark);
                return false;
            }
        }
        if (subjectVar != null) {
            if (! interpreter.unify(subjectVar,   triple.getSubject()) ) {
                interpreter.unwindTrail(mark);
                return false;
            }
        } 
        if (predicateVar != null) {
            if (! interpreter.unify(predicateVar, triple.getPredicate()) ) {
                interpreter.unwindTrail(mark);
                return false;
            }
                
        } 
        return true;
    }
    
    /**
     * Check that the object of a triple match corresponds to the given functor pattern.
     * Side effects the variable bindings.
     */
    public boolean functorMatch(Triple t, LPInterpreter interpreter) {
        Node o = t.getObject();
        if (!Functor.isFunctor(o)) return false;
        Functor f = (Functor)o.getLiteralValue();
        if ( ! f.getName().equals(objectFunctor.getName())) return false;
        if ( f.getArgLength() != objectFunctor.getArgLength()) return false;
        Node[] fargs = f.getArgs();
        Node[] oFargs = objectFunctor.getArgs();
        for (int i = 0; i < fargs.length; i++) {
            if (!interpreter.unify(oFargs[i], fargs[i])) return false;
        }
        return true;
    }
    
    /**
     * Initialize the triple match to preserve the current context of the given
     * LPInterpreter and search for the match defined by the current argument registers
     * @param interpreter the interpreter instance whose env, trail and arg values are to be preserved
     */
    @Override
    public void init(LPInterpreter interpreter) {
        super.init(interpreter);
        Node s = LPInterpreter.deref(interpreter.argVars[0]);
        subjectVar =   (s instanceof Node_RuleVariable) ? (Node_RuleVariable) s : null;
        Node p = LPInterpreter.deref(interpreter.argVars[1]);
        predicateVar = (p instanceof Node_RuleVariable) ? (Node_RuleVariable) p : null;
        Node o = LPInterpreter.deref(interpreter.argVars[2]);
        objectVar =    (o instanceof Node_RuleVariable) ? (Node_RuleVariable) o : null;
        if (Functor.isFunctor(o)) {
            objectFunctor = (Functor)o.getLiteralValue();
            goal = new TriplePattern(s, p, null);
        } else {
            objectFunctor = null;
            goal = new TriplePattern(s, p, o);
        }
    }
        
}
