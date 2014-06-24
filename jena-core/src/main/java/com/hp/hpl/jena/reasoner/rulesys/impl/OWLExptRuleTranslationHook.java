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
 * Experimental change to OWL translation hook that doesn't handle translation
 * of restrictions to functors.
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
                infGraph.addRuleDuringPrepare( ir );
                // Recognition rule elements
                recognitionBody.add( new TriplePattern( var, RDF.type.asNode(), description ) );
            }
            List<ClauseEntry> recognitionHead = new ArrayList<>(1);
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
