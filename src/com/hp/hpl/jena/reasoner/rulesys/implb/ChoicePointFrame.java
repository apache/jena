/******************************************************************
 * File:        ChoicePointFrame.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: ChoicePointFrame.java,v 1.2 2003-07-23 16:24:17 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.graph.Node;

import java.util.*;

/**
 * Represents a single frame in the LP interpreter's choice pointt stack,
 * represents the OR part of the search tree.
 * <p>
 * This is used in the inner loop of the interpreter and so is a pure data structure
 * not an abstract data type.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-07-23 16:24:17 $
 */
public class ChoicePointFrame extends FrameObject {

    /** The environment frame describing the state of the AND tree at this choice point */
    EnvironmentFrame envFrame;

    /** The top of the trail stack at the time of the call */
    int trailIndex;

    /** The set of argument variables for the call */
    Node[] argVars;

    /** Iterator over the set of clause code objects comprising the set of choices */
    Iterator clauseIterator;

    /**
     * Constructor.
     */
    public ChoicePointFrame(ChoicePointFactory factory) {
        super(factory);
    }

    /**
     * Initialize a choice point to preserve the current context of the given intepreter 
     * and then call the given set of predicates.
     * @param interpreter the LPInterpreter whose state is to be preserved
     * @param predicateClauses the list of predicates for this choice point
     */
    public void init(LPInterpreter interpreter, List predicateClauses) {
        envFrame = interpreter.envFrame;
        trailIndex = interpreter.trail.size();
        argVars = new Node[interpreter.argVars.length];
        System.arraycopy(interpreter.argVars, 0, argVars, 0, argVars.length);
        clauseIterator = predicateClauses.iterator();
    }

    /**
     * Override close method to reclaim the environment stack (imporant for
     * closing any embedded triple match iterators)
     */
    public void close() {
        if (envFrame != null)
            envFrame.close();
        if (link != null)
            link.close();
        free();
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