/******************************************************************
 * File:        StateFlag.java
 * Created by:  Dave Reynolds
 * Created on:  03-May-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: StateFlag.java,v 1.4 2004-12-07 09:56:32 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

/**
 * A set of constants used to record state information in the
 * backchaining rule interepreter. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2004-12-07 09:56:32 $
 */
public class StateFlag {
    
    /** Label for printing */
    private String label;

    /** Indicates a goal has failed and return no more answers at this time */
    public static final StateFlag FAIL = new StateFlag("FALL");
    
    /** Indicates that all currently available results have been returned and
     *  the goal should be suspended into new subgoal results have been generated */
    public static final StateFlag SUSPEND = new StateFlag("SUSPEND");
    
    /** Indicates that the goal remains active */
    public static final StateFlag ACTIVE = new StateFlag("ACTIVE");
    
    /** Indicates a fully satisfied goal */
    public static final StateFlag SATISFIED = new StateFlag("SATISFIED");
    
    /** Constructor */
    private StateFlag(String label) {
        this.label = label;
    }
    
    /** Print string */
    public String toString() {
        return label;
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