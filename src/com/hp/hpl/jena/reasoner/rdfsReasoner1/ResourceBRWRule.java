/******************************************************************
 * File:        ResourceBRWRule.java
 * Created by:  Dave Reynolds
 * Created on:  28-Jan-03
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: ResourceBRWRule.java,v 1.8 2004-12-07 09:56:19 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rdfsReasoner1;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
 * A special case of a backchaing rule to handle the nasty case
 * of "anything mentioned in any triple is a Resource".
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.8 $ on $Date: 2004-12-07 09:56:19 $
 */
public class ResourceBRWRule extends BRWRule {
    
    /** node form of rdf:type */
    private static Node TYPE = RDF.type.getNode();
    
    /** node form of rdfs:Resource */
    private static Node RESOURCE = RDFS.Resource.getNode();

    /**
     * Constructor
     */
    public ResourceBRWRule() {
        super(new TriplePattern(null, RDF.type.getNode(), RDFS.Resource.getNode()),   
               new TriplePattern(null, null, null));
    }
    
    /**
     * Use the rule to implement the given query. This will
     * instantiate the rule against the query, run the new query
     * against the whole reasoner+rawdata again and then rewrite the
     * results from that query according the rule.
     * @param query the query being processed
     * @param infGraph link to originating inference graph, may be re-invoked after a pattern rewrite
     * @param data the raw data graph which gets passed back to the reasoner as part of the recursive invocation
     * @param firedRules set of rules which have already been fired and should now be blocked
     * @return a ExtendedIterator which aggregates the matches and rewrites them
     * according to the rule
     */
    public ExtendedIterator execute(TriplePattern query, InfGraph infGraph, Finder data, HashSet firedRules) {
        RDFSInfGraph bRr = (RDFSInfGraph)infGraph;
        if (query.getSubject().isVariable()) {
            // Find all things of type resource
            return new ResourceRewriteIterator(bRr.findRawWithContinuation(body, data));
        } else {
            // Just check for a specific resource
            Node subj = query.getSubject();
            TriplePattern pattern = new TriplePattern(subj, null, null);
            String var = "s";
            ExtendedIterator it = bRr.findRawWithContinuation(pattern, data);
            if (!it.hasNext()) {
                pattern = new TriplePattern(null, null, subj);
                var = "o";
                it = bRr.findRawWithContinuation(pattern, data);
                if (!it.hasNext()) {
                    pattern = new TriplePattern(null, subj, null);
                    var = "p";
                    it = bRr.findRawWithContinuation(pattern, data);
                }
            }
            BRWRule rwrule = new BRWRule(new TriplePattern(Node.createVariable(var), TYPE, RESOURCE), body);
            return new RewriteIterator(it, rwrule);
        }
    }    

    /**
     * Return true if this rule is a a complete solution to the given
     * query and the router need look no further
     */
    public boolean completeFor(TriplePattern query) {
        return head.subsumes(query);
    }

    /**
     * Inner class. This implements an iterator that uses the rule to rewrite any
     * results from the supplied iterator according to the rule.
     */
    static class ResourceRewriteIterator extends WrappedIterator  {
        /** short stack of triples generated but not yet delivered */
        private Triple[] lookahead = new Triple[3];
        
        /** the number of values available in lookahead */
        private short nAvailable = 0;
        
        /** The set of objects already seen */
        protected HashSet seen = new HashSet();
    
        /** 
         * Constructor 
         * @param underlying the iterator whose results are to be rewritten
         * @param rule the BRWRule which defines the rewrite
         */
        public ResourceRewriteIterator(Iterator underlying) {
            super(underlying);
        }

        /**
         * Record a new instance of Resource so long as it has not
         * been seen before
         */
        private void push(Node resource) {
            if (seen.add(resource)) {
                lookahead[nAvailable++] = new Triple(resource, TYPE, RESOURCE);
            }
        }
                
        /**
         * @see Iterator#hasNext()
         */
        public boolean hasNext() {
            while (nAvailable == 0 && super.hasNext()) {
                Triple value = (Triple)super.next();
                if (seen.add(value)) {
                    push(value.getSubject());
                    push(value.getPredicate());
                    Node object = value.getObject();
                    if (!object.isLiteral()) {
                        push(object);
                    }
                    
                }
            }
            return nAvailable > 0;
        }
        
        /**
         * @see Iterator#next()
         */
        public Object next() {
            if (nAvailable == 0 && ! hasNext()) {
                throw new NoSuchElementException("No element available");
            }
            return lookahead[--nAvailable];
        }

    }    // End of inner class - ResourceRewriteIterator
    
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
