/******************************************************************
 * File:        TripleMatchFrame.java
 * Created by:  Dave Reynolds
 * Created on:  23-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TripleMatchFrame.java,v 1.6 2003-08-06 17:00:22 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Frame on the choice point stack used to represent the state of a direct
 * graph triple match.
 *  
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-08-06 17:00:22 $
 */
public class TripleMatchFrame extends FrameObject {
    
    /** The environment frame describing the state of the AND tree at this choice point */
    EnvironmentFrame envFrame;
    
    /** The top of the trail stack at the time of the call */
    int trailIndex;
   
    /** An iterator over triples matching a goal */
    ExtendedIterator matchIterator;
    
    /** The variable to the subject of the triple, null if no binding required */
    Node_RuleVariable subjectVar;
    
    /** The variable to the predicate of the triple, null if no binding required */
    Node_RuleVariable predicateVar;
    
    /** The variable to the subject of the triple, null if no binding required */
    Node_RuleVariable objectVar;
    
    /** The functor variable structure to explode the object into, null if not required */
    Functor objectFunctor;
    
    /** The continuation program counter offet in the parent clause's byte code */
    int cpc;
    
    /** The continuation argument counter offset in the parent clause's arg stream */
    int cac;
    
    /**
     * Constructor.
     */
    public TripleMatchFrame(TripleMatchFactory factory) {
        super(factory);
    }

    /**
     * Find the next result triple and bind the result vars appropriately.
     * @param interpreter the calling interpreter whose trail should be used
     * @return false if there are no more matches in the iterator.
     */
    public boolean nextMatch(LPInterpreter interpreter) {
        if (matchIterator.hasNext()) {
            Triple t = (Triple)matchIterator.next();
            if (objectVar != null)    interpreter.bind(objectVar,    t.getObject());
            if (objectFunctor != null) {
                int mark = interpreter.trail.size();
                while (! functorMatch(t, interpreter)) {
                    interpreter.unwindTrail(mark);
                    if (matchIterator.hasNext()) {
                        t = (Triple) matchIterator.next();
                    } else {
                        return false;
                    }
                }
            }
            if (subjectVar != null)   interpreter.bind(subjectVar,   t.getSubject());
            if (predicateVar != null) interpreter.bind(predicateVar, t.getPredicate());
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Check that the object of a triple match corresponds to the given functor pattern.
     * Side effects the variable bindings.
     */
    public boolean functorMatch(Triple t, LPInterpreter interpreter) {
        Node o = t.getObject();
        if (!Functor.isFunctor(o)) return false;
        Functor f = (Functor)o.getLiteral().getValue();
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
     * @param intepreter the interpreter instance whose env, trail and arg values are to be preserved
     */
    public void init(LPInterpreter interpreter) {
        envFrame = interpreter.envFrame;
//        envFrame.incRefCount();
        trailIndex = interpreter.trail.size();
        Node s = LPInterpreter.deref(interpreter.argVars[0]);
        subjectVar =   (s instanceof Node_RuleVariable) ? (Node_RuleVariable) s : null;
        Node p = LPInterpreter.deref(interpreter.argVars[1]);
        predicateVar = (p instanceof Node_RuleVariable) ? (Node_RuleVariable) p : null;
        Node o = LPInterpreter.deref(interpreter.argVars[2]);
        objectVar =    (o instanceof Node_RuleVariable) ? (Node_RuleVariable) o : null;
        TriplePattern query = null;
        if (Functor.isFunctor(o)) {
            objectFunctor = (Functor)o.getLiteral().getValue();
            query = new TriplePattern(s, p, null);
        } else {
            objectFunctor = null;
            query = new TriplePattern(s, p, o);
        }
        this.matchIterator = interpreter.getEngine().getInfGraph().findDataMatches(query);
    }

    /**
     * Set the continuation point for this frame.
     */
    public void setContinuation(int pc, int ac) {
        cpc = pc;
        cac = ac; 
    }
    
    /**
     * Override close method to reclaim the iterator.
     */
    public void close() {
        if (matchIterator != null) matchIterator.close();
//        if (--refCount == 0) {
//            if (link != null) {
//                link.close();
//            }
//            if (envFrame != null) {
//                envFrame.close();
//            }
//            free();
//        }
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