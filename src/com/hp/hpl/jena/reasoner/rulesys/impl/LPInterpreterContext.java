/******************************************************************
 * File:        LPInterpeterContext.java
 * Created by:  Dave Reynolds
 * Created on:  09-Aug-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: LPInterpreterContext.java,v 1.4 2005-02-21 12:17:55 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

/**
 * The context in which an LPInterpreter instance is running.
 * The context the entity that should be notified when a branch has been
 * suspended awaiting further results for a given generator.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2005-02-21 12:17:55 $
 */
public interface LPInterpreterContext extends LPInterpreterState {

    /** Notify this context that a branch was suspended awaiting futher
     *  results for the given choice point. */
    public void notifyBlockedOn(ConsumerChoicePointFrame ccp);
    
    /** Test if one of our top level choice points is ready to be reactivated */
    public boolean isReady();
    
    /** Notify this context that the given choice point has terminated
     *  and can be remove from the wait list. */
    public void notifyFinished(ConsumerChoicePointFrame ccp);
    
    /** Called by a generating choice point to indicate we can be run
     * because the indicated choice point is ready. */
    public void setReady(ConsumerChoicePointFrame ccp);

}



/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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