/******************************************************************
 * File:        RETEQueue.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RETEQueue.java,v 1.1 2003-06-09 08:28:19 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;

import java.util.*;

/**
 * Represents one input left of a join node. The queue points to 
 * a sibling queue representing the other leg which should be joined
 * against.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-06-09 08:28:19 $
 */
public class RETEQueue implements RETENode {
    
    /** A multi-set of partially bound envionments */
    protected HashMap queue = new HashMap();
    
    /** A set of variable indices which should match between the two inputs */
    protected byte[] matchIndices;
    
    /** The sibling queue which forms the other half of the join node */
    protected RETEQueue sibling;
    
    /** The node that results should be passed on to */
    protected RETENode continuation;
    
    /** 
     * Constructor. The queue is not usable until it has been bound
     * to a sibling and a continuation node.
     * @param A set of variable indices which should match between the two inputs
     */
    public RETEQueue(byte[] matchIndices) {
        this.matchIndices = matchIndices; 
    }
    
    /** 
     * Constructor. The queue is not usable until it has been bound
     * to a sibling and a continuation node.
     * @param A List of variable indices which should match between the two inputs
     */
    public RETEQueue(List matchIndexList) {
        int len = matchIndexList.size();
        matchIndices = new byte[len];
        for (int i = 0; i < len; i++) {
            matchIndices[i] = (byte) ((Number)matchIndexList.get(i)).intValue();
        }
    }
    
    /**
     * Set the sibling for this node.
     */
    public void setSibling(RETEQueue sibling) {
        this.sibling = sibling;
    }
    
    /**
     * Set the continuation node for this node.
     */
    public void setContinuation(RETENode continuation) {
        this.continuation = continuation;
    }

    /** 
     * Propagate a token to this node.
     * @param env a set of variable bindings for the rule being processed. 
     * @param isAdd distinguishes between add and remove operations.
     */
    public void fire(BindingEnvironment env, boolean isAdd) {
        // TODO: Implement
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