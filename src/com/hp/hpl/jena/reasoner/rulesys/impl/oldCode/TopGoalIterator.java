/******************************************************************
 * File:        TopGoalIterator.java
 * Created by:  Dave Reynolds
 * Created on:  30-May-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TopGoalIterator.java,v 1.3 2004-12-07 09:56:31 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl.oldCode;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.impl.StateFlag;

import java.util.Iterator;

/**
 * Wraps up the backward chaining engine as an iterator-like object that can be
 * used in InfGraphs to implement a "find" operation. It creates
 * a top level GoalState and pumps that for results until the 
 * agenda is empty.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2004-12-07 09:56:31 $
 */
public class TopGoalIterator implements Iterator {
        
    /** The GoalState which is traversing the top level derivation tree */
    GoalState goalState;
    
    /** The next result to be returned, or null if we have finished */
    Object lookAhead;
    
    /** The parent backward chaining engine */
    BRuleEngine engine;
    
    /**
     * Constructor. Wraps a top level goal state as an iterator
     */
    public TopGoalIterator(BRuleEngine engine, TriplePattern goal) {
        this.engine = engine;
        this.goalState = engine.findGoal(goal);
        moveForward();
    }
    
    /**
     * Find the next result in the goal state and put it in the
     * lookahead buffer.
     */
    private void moveForward() {
        lookAhead = goalState.next();
        if (lookAhead == StateFlag.SUSPEND) {
            if (engine.next(goalState) != null) {
                lookAhead = goalState.next();
            } else {
                lookAhead = null;
            }
        } else if (lookAhead == StateFlag.FAIL) {
            lookAhead = null;
        }
        if (lookAhead == null) close();
    }
    
    /**
     * @see com.hp.hpl.jena.util.iterator.ClosableIterator#close()
     */
    public void close() {
        goalState.close();
        engine.halt();
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return (lookAhead != null);
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() {
        Object result = lookAhead;
        moveForward();
        return result;
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

}


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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