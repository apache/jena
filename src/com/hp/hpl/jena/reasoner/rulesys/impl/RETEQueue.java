/******************************************************************
 * File:        RETEQueue.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RETEQueue.java,v 1.4 2003-06-11 17:08:28 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
 * Represents one input left of a join node. The queue points to 
 * a sibling queue representing the other leg which should be joined
 * against.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-06-11 17:08:28 $
 */
public class RETEQueue implements RETESinkNode, RETESourceNode {
    
    /** A multi-set of partially bound envionments */
    protected HashMap queue = new HashMap();
    
    /** A set of variable indices which should match between the two inputs */
    protected byte[] matchIndices;
    
    /** The sibling queue which forms the other half of the join node */
    protected RETEQueue sibling;
    
    /** The node that results should be passed on to */
    protected RETESinkNode continuation;
    
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
     * Set the continuation node for this node (and any sibling)
     */
    public void setContinuation(RETESinkNode continuation) {
        this.continuation = continuation;
        if (sibling != null) sibling.continuation = continuation;
    }

    /** 
     * Propagate a token to this node.
     * @param env a set of variable bindings for the rule being processed. 
     * @param isAdd distinguishes between add and remove operations.
     */
    public void fire(BindingVector env, boolean isAdd) {
        // Store the new token in this store
        Count count = (Count)queue.get(env);
        if (count == null) {
            // no entry yet
            if (!isAdd) return;
            queue.put(env, new Count(1));
        } else {
            if (isAdd) {
                count.inc();
            } else {
                count.dec();
            }
        }
        
        // Cross match new token against the entries in the sibling queue
        for (Iterator i = sibling.queue.keySet().iterator(); i.hasNext(); ) {
            Node[] candidate = ((BindingVector)i.next()).getEnvironment();
            Node[] envNodes = env.getEnvironment();
            boolean matchOK = true;
            for (int j = 0; j < matchIndices.length; j++) {
                int index = matchIndices[j];
                if ( ! candidate[index].sameValueAs(envNodes[index])) {
                    matchOK = false;
                    break;
                }
            }
            if (matchOK) {
                // Instantiate a new extended environment
                Node[] newNodes = new Node[candidate.length];
                for (int j = 0; j < candidate.length; j++) {
                    Node n = candidate[j];
                    newNodes[j] = (n == null) ? envNodes[j] : n;
                }
                BindingVector newEnv = new BindingVector(newNodes);
                // Fire the successor processing
                continuation.fire(newEnv, isAdd);
            }
        }
    }

    /**
     * Inner class used to represent an updatable count.
     */
    protected static class Count {
        /** the count */
        int count;
        
        /** Constructor */
        public Count(int count) {
            this.count = count;
        }
        
        /** Access count value */
        public int getCount() {
            return count;
        }
        
        /** Increment the count value */
        public void inc() {
            count++;
        }
        
        /** Decrement the count value */
        public void dec() {
            count--;
        }
        
        /** Set the count value */
        public void setCount(int count) {
            this.count = count;
        }
    }
    
    /**
     * Clone this node in the network.
     * @param context the new context to which the network is being ported
     */
    public RETENode clone(Map netCopy, RETERuleContext context) {
        RETEQueue clone = (RETEQueue)netCopy.get(this);
        if (clone == null) {
            clone = new RETEQueue(matchIndices);
            netCopy.put(this, clone);
            clone.setSibling((RETEQueue)sibling.clone(netCopy, context));
            clone.setContinuation((RETESinkNode)continuation.clone(netCopy, context));
            clone.queue.putAll(queue);
        }
        return clone;
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