/******************************************************************
 * File:        LPPartialDerivation.java
 * Created by:  Dave Reynolds
 * Created on:  18-Aug-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPPartialDerivation.java,v 1.1 2003-08-18 13:50:31 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.TriplePattern;

import java.util.*;

/**
 * Structure used to record the partial state of an LP derivation. Used
 * to build up the information need for derivation logging.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-08-18 13:50:31 $
 */
public class LPPartialDerivation {
    /** The initial starting arguments for the call */
    Node[] argVars = new Node[RuleClauseCode.MAX_ARGUMENT_VARS];
        
    /** The set of instantiated subgoals processed so far */
    List matchList = new ArrayList();
        
    /** Constructor */
    public LPPartialDerivation(Node[] args) {
        System.arraycopy(args, 0, argVars, 0, RuleClauseCode.MAX_ARGUMENT_VARS);
    }
        
    /** Instantiate and record a matched subgoal */
    public void noteMatch(TriplePattern pattern) {
        Triple match = new Triple(LPInterpreter.deref(pattern.getSubject()), 
                                    LPInterpreter.deref(pattern.getPredicate()),
                                    LPInterpreter.deref(pattern.getObject()));
        matchList.add(match);
    }

    /**
     * Return the final instantiated goal given the current binding state.
     */
    public Triple getResult() {
        return new Triple(
                    LPInterpreter.deref(argVars[0]),
                    LPInterpreter.deref(argVars[1]), 
                    LPInterpreter.derefPossFunctor(argVars[2]));
    }
    
    /**
     * Return the current list of matched subgoals in this subderivation.
     */
    public List getMatchList() {
        return matchList;
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