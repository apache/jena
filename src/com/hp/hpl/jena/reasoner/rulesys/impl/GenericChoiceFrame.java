/******************************************************************
 * File:        GenericChoiceFrame.java
 * Created by:  Dave Reynolds
 * Created on:  07-Aug-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: GenericChoiceFrame.java,v 1.3 2004-12-07 09:56:32 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

/**
 * Core properties of choice frames used use to represent the OR state of
 * the backtracking search. Specific variants of this need to preserve additional
 * choice state.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type and assumes privileged access to the interpreter state.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2004-12-07 09:56:32 $
 */
public class GenericChoiceFrame extends FrameObject {

    /** The environment frame describing the state of the AND tree at this choice point */
    EnvironmentFrame envFrame;

    /** The top of the trail stack at the time of the call */
    int trailIndex;
    
    /** The continuation program counter offet in the parent clause's byte code */
    int cpc;
    
    /** The continuation argument counter offset in the parent clause's arg stream */
    int cac;

    /**
     * Initialize a choice point to preserve the current context of the given intepreter 
     * and then call the given set of predicates.
     * @param interpreter the LPInterpreter whose state is to be preserved
     */
    public void init(LPInterpreter interpreter) {
        envFrame = interpreter.envFrame;
        trailIndex = interpreter.trail.size();
    }

    /**
     * Set the continuation point for this frame.
     */
    public void setContinuation(int pc, int ac) {
        cpc = pc;
        cac = ac; 
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