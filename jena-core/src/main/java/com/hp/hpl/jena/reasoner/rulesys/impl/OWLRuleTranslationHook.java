/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    @Override
    public void run(FBRuleInfGraph infGraph, Finder dataFind, Graph inserts) {
        Iterator<Triple> it = dataFind.find(new TriplePattern(null, OWL.intersectionOf.asNode(), null));
        while (it.hasNext()) {
            Triple decl = it.next();
            Node className = decl.getSubject();
            List<Node> elements = new ArrayList<>();
            translateIntersectionList(decl.getObject(), dataFind, elements);
            // Generate the corresponding ruleset
            List<ClauseEntry> recognitionBody = new ArrayList<>();
            Node var = new Node_RuleVariable("?x", 0);
            for ( Node description : elements )
            {
                // Implication rule
                Rule ir = new Rule( "intersectionImplication", new ClauseEntry[]{
                    new TriplePattern( className, RDFS.subClassOf.asNode(), description ) }, new ClauseEntry[0] );
                ir.setBackward( false );
//                System.out.println("translation hook => " + ir);
                infGraph.addRuleDuringPrepare( ir );
                // Recognition rule elements
                recognitionBody.add( new TriplePattern( var, RDF.type.asNode(), description ) );
            }
            List<ClauseEntry> recognitionHead = new ArrayList<>(1);
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
    protected static void translateIntersectionList(Node node, Finder dataFind, List<Node> elements) {
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
