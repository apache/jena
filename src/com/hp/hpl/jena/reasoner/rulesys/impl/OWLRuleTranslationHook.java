/******************************************************************
 * File:        OWLRuleTranslationHook.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: OWLRuleTranslationHook.java,v 1.7 2005-02-21 12:17:57 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

/**
 * A rule preprocessor that scans the data looking for interesection
 * definitions and augements the rule base by translations of the
 * intersection statement.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2005-02-21 12:17:57 $
 */
public class OWLRuleTranslationHook implements RulePreprocessHook {

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
    public void run(FBRuleInfGraph infGraph, Finder dataFind, Graph inserts) {
        Iterator it = dataFind.find(new TriplePattern(null, OWL.intersectionOf.asNode(), null));
        while (it.hasNext()) {
            Triple decl = (Triple)it.next();
            Node className = decl.getSubject();
            List elements = new ArrayList();
            translateIntersectionList(decl.getObject(), dataFind, elements);
            // Generate the corresponding ruleset
            List recognitionBody = new ArrayList();
            Node var = new Node_RuleVariable("?x", 0);
            for (Iterator i = elements.iterator(); i.hasNext(); ) {
                Node description = (Node)i.next();
                // Implication rule
                Rule ir = new Rule("intersectionImplication", new ClauseEntry[] {
                                    new TriplePattern(className, RDFS.subClassOf.asNode(), description)
                                    }, new ClauseEntry[0]);
                ir.setBackward(false);
//                System.out.println("translation hook => " + ir);
                infGraph.addRuleDuringPrepare(ir);
               // Recognition rule elements
               recognitionBody.add(new TriplePattern(var, RDF.type.asNode(), description));
            }
            List recognitionHead = new ArrayList(1);
            recognitionHead.add(new TriplePattern(var, RDF.type.asNode(), className));
            Rule rr = new Rule("intersectionRecognition", recognitionHead, recognitionBody);
            rr.setBackward(true);
//            System.out.println("translation hook => " + rr);
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
    protected static void translateIntersectionList(Node node, Finder dataFind, List elements) {
        if (node == null) {
            throw new ReasonerException("Illegal list structure in owl:intersectionOf");
        }
        if (node.equals(RDF.nil.asNode())) {
            return; // end of list
        } 
        Node description = Util.getPropValue(node, RDF.first.asNode(), dataFind);
        if (description == null) {
            throw new ReasonerException("Illegal list structure in owl:intersectionOf");
        }
        // Translate the first description element
        /* - temp comment out during debugging
        if (dataFind.contains(new TriplePattern(description, RDF.type.asNode(), OWL.Restriction.asNode()))) {
            // Process a restriction element
            Node onprop = Util.getPropValue(description, OWL.onProperty.asNode(), dataFind);
            Node value;
            if ((value = Util.getPropValue(description, OWL.allValuesFrom.asNode(), dataFind)) != null) {
                elements.add(Functor.makeFunctorNode("all", new Node[] {onprop, value}));
            } else if ((value = Util.getPropValue(description, OWL.someValuesFrom.asNode(), dataFind)) != null) {
                elements.add(Functor.makeFunctorNode("some", new Node[] {onprop, value}));
            } else if ((value = Util.getPropValue(description, OWL.hasValue.asNode(), dataFind)) != null) {
                elements.add(Functor.makeFunctorNode("hasValue", new Node[] {onprop, value}));
            } else if ((value = Util.getPropValue(description, OWL.minCardinality.asNode(), dataFind)) != null) {
                elements.add(Functor.makeFunctorNode("min", new Node[] {onprop, value}));
            } else if ((value = Util.getPropValue(description, OWL.maxCardinality.asNode(), dataFind)) != null) {
                elements.add(Functor.makeFunctorNode("max", new Node[] {onprop, value}));
            } else if ((value = Util.getPropValue(description, OWL.cardinality.asNode(), dataFind)) != null) {
                elements.add(Functor.makeFunctorNode("max", new Node[] {onprop, value}));
                elements.add(Functor.makeFunctorNode("min", new Node[] {onprop, value}));
            } else {
                elements.add(description);
            }
        } else {
            // Assume its a class name
            elements.add(description);
        }
        */
        // Above used to translated intersections into direct functor tests but in fact the
        // references to the restriction bNode is sufficent and a better match to the current rules
        elements.add(description);
        // Process the list tail
        Node next = Util.getPropValue(node, RDF.rest.asNode(), dataFind);
        translateIntersectionList(next, dataFind, elements);
    }

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