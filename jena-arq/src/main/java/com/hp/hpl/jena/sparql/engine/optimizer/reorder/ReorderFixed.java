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

package com.hp.hpl.jena.sparql.engine.optimizer.reorder;

import com.hp.hpl.jena.sparql.engine.optimizer.Pattern ;
import com.hp.hpl.jena.sparql.engine.optimizer.StatsMatcher ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.PatternTriple ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformationSubstitution ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.sse.Item ;
import static com.hp.hpl.jena.sparql.engine.optimizer.reorder.PatternElements.* ;

/** Fixed scheme for choosing based on the triple patterns, without
 *  looking at the data.  It gives a weight to a triple, with more grounded terms
 *  being considered better.  It weights against rdf:type because that can be 
 *  very unselective (e.g. ?x rdf:type rdf:Resource)
 */
public class ReorderFixed extends ReorderTransformationSubstitution {
    /*
     * How it works:
     * 
     * Choose the 'best' pattern, propagate the fact that variables are now
     * bound (ReorderTransformationSubstitution) then chooses the next triple
     * pattern.
     * 
     * Instead of just one set of rules, we make an exception is rdf:type. ?x
     * rdf:type :T or ?x rdf:type ?type are often very much less selective and
     * we want to give them special, poorer weighings. We do this by controlling
     * the order of matching: first check to see if it's rdf;type in the
     * predicate position, then apply the appropriate matcher.
     * 
     * If we just used a single StatsMatcher with all the rules, the
     * VAR/TERM/TERM or VAR/TERM/VAR rules match rdf:type with lower weightings.
     * 
     * The relative order of equal weightings is not changed.
     * 
     * There are two StatsMatchers: 'matcher' and 'matcherRdfType'
     * applied for the normal case and the rdf:type case.
     */
    
    public ReorderFixed() {}

    private static Item              type                = Item.createNode(NodeConst.nodeRDFType) ;

    /** The number of triples used for the base scale */
    public static final int                MultiTermSampleSize = 100 ;

    // Used for general patterns
    private final static StatsMatcher matcher             = new StatsMatcher() ;
    // Used to override choices made by the matcher above.
    private final static StatsMatcher matcherRdfType      = new StatsMatcher() ;
    
    static { init() ; }
    
    private static void init() {
        // rdf:type can be a bad choice e.g rdf:type rdf:Resource
        // with inference enabled.
        // Weight use of rdf:type worse then the general pattern
        // that would also match by using two matchers. 
        
        // Numbers chosen as an approximation ratios for a graph of 100 triples

        // 1 : TERM type TERM is builtin (SPO).
        // matcherRdfType.addPattern(new Pattern(1, TERM, TERM, TERM)) ; 
        matcherRdfType.addPattern(new Pattern(5, VAR, type, TERM)) ;
        matcherRdfType.addPattern(new Pattern(50, VAR, type, VAR)) ;
        
        // SPO - built-in - not needed as a rule
        // matcher.addPattern(new Pattern(1, TERM, TERM, TERM)) ; 

        matcher.addPattern(new Pattern(2, TERM, TERM, VAR)) ;                   // SP?
        matcher.addPattern(new Pattern(3, VAR, TERM, TERM)) ;                   // ?PO
        matcher.addPattern(new Pattern(2, TERM, TERM, TERM)) ;                  // S?O

        matcher.addPattern(new Pattern(10, TERM, VAR, VAR)) ;                   // S??
        matcher.addPattern(new Pattern(20, VAR, VAR, TERM)) ;                   // ??O
        matcher.addPattern(new Pattern(30, VAR, TERM, VAR)) ;                   // ?P?

        matcher.addPattern(new Pattern(MultiTermSampleSize, VAR, VAR, VAR)) ;   // ???
    }

    @Override
    public double weight(PatternTriple pt) {
        // Special case rdf:type first to make it lower(worse) than 
        // VAR, TERM, TERM which would otherwise be used.
        if ( type.equals(pt.predicate) ) {
            double w = matcherRdfType.match(pt) ;
            if ( w > 0 )
                return w ;
        }
        return matcher.match(pt) ;
    }
}
