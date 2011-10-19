/******************************************************************
 * File:        OWLExptRuleTranslationHook.java
 * Created by:  Dave Reynolds
 * Created on:  10-Jul-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: OWLExptRuleTranslationHook.java,v 1.1 2009-06-29 08:55:33 castagna Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

/**
 * Experimental change to OWL translation hook that doesn't handle translation
 * of restrictions to functors.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:33 $
 */
public class OWLExptRuleTranslationHook implements RulePreprocessHook  {

    /**
     * Invoke the preprocessing hook. This will be called during the
     * preparation time of the hybrid reasoner.
     * @param infGraph the inference graph which is being prepared,
     * the hook code can use this to addDeductions or add additional
     * rules (using addRuleDuringPrepare).
     * @param dataFind the finder which packages up the raw data (both
     * schema and data bind) and any cached transitive closures.
     * @param inserts a temporary graph into which the hook should insert
     * all new deductions that should be seen by the rules.
     */
    @Override
    public void run(FBRuleInfGraph infGraph, Finder dataFind, Graph inserts) {
        Iterator<Triple> it = dataFind.find(new TriplePattern(null, OWL.intersectionOf.asNode(), null));
        while (it.hasNext()) {
            Triple decl = it.next();
            Node className = decl.getSubject();
            List<Node> elements = new ArrayList<Node>();
            translateIntersectionList(decl.getObject(), dataFind, elements);
            // Generate the corresponding ruleset
            List<ClauseEntry> recognitionBody = new ArrayList<ClauseEntry>();
            Node var = new Node_RuleVariable("?x", 0);
            for (Iterator<Node> i = elements.iterator(); i.hasNext(); ) {
                Node description = i.next();
                // Implication rule
                Rule ir = new Rule("intersectionImplication", new ClauseEntry[] {
                                    new TriplePattern(className, RDFS.subClassOf.asNode(), description)
                                    }, new ClauseEntry[0]);
                ir.setBackward(false);
                infGraph.addRuleDuringPrepare(ir);
               // Recognition rule elements
               recognitionBody.add(new TriplePattern(var, RDF.type.asNode(), description));
            }
            List<ClauseEntry> recognitionHead = new ArrayList<ClauseEntry>(1);
            recognitionHead.add(new TriplePattern(var, RDF.type.asNode(), className));
            Rule rr = new Rule("intersectionRecognition", recognitionHead, recognitionBody);
            rr.setBackward(true);
            infGraph.addRuleDuringPrepare(rr);
        }
    }
    
    /**
     * Translation code to translate a list of intersection elements into a 
     * Java list of corresponding class names or restriction functors.
     * @param node the list node currently being processed
     * @param data the source data to use as a context for this processing
     * @param elements the list of elements found so far
     */
    protected static void translateIntersectionList(Node node, Finder dataFind, List<Node> elements) {
        if (node.equals(RDF.nil.asNode())) {
            return; // end of list
        } 
        Node description = Util.getPropValue(node, RDF.first.asNode(), dataFind);
        elements.add(description);
        // Process the list tail
        Node next = Util.getPropValue(node, RDF.rest.asNode(), dataFind);
        translateIntersectionList(next, dataFind, elements);
    }
    
    /**
     * Validate a triple add to see if it should reinvoke the hook. If so
     * then the inference will be restarted at next prepare time. Incremental
     * re-processing is not yet supported.
     */
    @Override
    public boolean needsRerun(FBRuleInfGraph infGraph, Triple t) {
        return (t.getPredicate().equals(OWL.intersectionOf.asNode()));
    }

}


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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