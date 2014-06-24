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
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.*;

/**
 * A rule preprocessor that scans all supplied data looking for instances
 * of container membership properties and adds those to the deductions set.  
 */
public class RDFSCMPPreprocessHook implements RulePreprocessHook {
    protected static String memberPrefix = RDF.getURI() + "_";

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
        ExtendedIterator<Triple> it = dataFind.find(new TriplePattern(null, null, null));
        HashSet<Node> properties = new HashSet<>();
        while (it.hasNext()) {
            Triple triple = it.next();
            Node prop = triple.getPredicate();
            if (prop.equals(RDF.Nodes.type) && triple.getObject().equals(RDF.Nodes.Property) ) {
                prop = triple.getSubject();
            }
            if (properties.add(prop)) {
                if (prop.getURI().startsWith(memberPrefix)) {
                    // A container property
                    inserts.add(new Triple(prop, RDF.Nodes.type, RDFS.Nodes.ContainerMembershipProperty));
                }
            }
        }
    }
    
    /**
     * Validate a triple add to see if it should reinvoke the hook. If so
     * then the inference will be restarted at next prepare time. Incremental
     * re-processing is not yet supported but in this case would be useful.
     */
    @Override
    public boolean needsRerun(FBRuleInfGraph infGraph, Triple t) {
        return (t.getPredicate().getURI().startsWith(memberPrefix));
    }

}
