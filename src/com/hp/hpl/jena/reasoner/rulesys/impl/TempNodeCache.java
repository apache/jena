/******************************************************************
 * File:        TempNodeCache.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TempNodeCache.java,v 1.4 2003-08-14 07:51:10 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.util.OneToManyMap;

/**
 * In some rules we need to be able to create temporary property values 
 * which are inferred from ontology constraints but not present in the ground
 * data. This structure is used to manage a pool of such temporary nodes.
 * It is only needed in situations where the data can not be added directly
 * to a deductions graph due to the risk of concurrent access.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-08-14 07:51:10 $
 */

// Implementation note: We need to map from a pair of values (instance and prop).
// The current implementation in terms on NodePair will turn over storage during
// lookup. Could replace this with a cascaded hash table.

public class TempNodeCache {

    /** Map from instance+property to value */
    protected OneToManyMap ipMap = new OneToManyMap();
    
    /** Map from temp to RDF class, if any */
    protected Map classMap = new HashMap(); 
    
    /**
     * Cosntructor.
     * @param infgraph Parent inference graph, used to be needed for synchronization, don't think
     * we need it any more 
     */
    public TempNodeCache(InfGraph infgraph) {
    }
    
    /**
     * Retrieve or create a bNode representing an inferred property value.
     * @param instance the base instance node to which the property applies
     * @param prop the property node whose value is being inferred
     * @param pclass the (optional, can be null) class for the inferred value.
     * @return the bNode representing the property value 
     */
    public synchronized Node getTemp(Node instance, Node prop, Node pclass) {
        NodePair ip = new NodePair(instance, prop);
        Node result = null;
        for (Iterator i = ipMap.getAll(ip); i.hasNext(); ) {
            Node t = (Node)i.next();
            if (pclass != null) {
                if (classMap.get(t).equals(pclass)) {
                    result = t;
                    break;
                }
            } else {
                result = t;
                break;
            }
        }
        if (result == null) {
            // No value yet, so create one
            result = Node.createAnon();
            ipMap.put(ip, result);
            if (pclass != null) {
                classMap.put(result, pclass);
            }
        }
        return result;
    }
    
    /**
     * Inner class used to hold and hash a node pair.
     */
    public static class NodePair {
        /** first node in the pair */
        protected Node first;
        
        /** second node in the pair */
        protected Node second;
        
        /** Constructor */
        public NodePair(Node first, Node second) {
            this.first = first;
            this.second = second;
        }
        
        /**
         * Return the first node in the pair.
         */
        public Node getFirst() {
            return first;
        }
        
        /**
         * Return the second node in the pair.
         */
        public Node getSecond() {
            return second;
        }
        
        /**
         * Equality of each component.
         */
        public boolean equals(Object o) {
            return o instanceof NodePair &&
                        first.equals(((NodePair)o).first) && 
                        second.equals(((NodePair)o).second); 
        }
        /**
         * Simple combined hashcode.
         */
        public int hashCode() {
            return first.hashCode() ^ (second.hashCode() << 1);
        }

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