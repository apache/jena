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

package org.apache.jena.sparql.engine.optimizer.reorder;

import static org.apache.jena.sparql.engine.optimizer.reorder.PatternElements.TERM ;
import static org.apache.jena.sparql.engine.optimizer.reorder.PatternElements.VAR ;
import org.apache.jena.sparql.engine.optimizer.Pattern ;
import org.apache.jena.sparql.engine.optimizer.StatsMatcher ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.sse.Item ;

/**
 * Alternative fixed reorder function. This corresponds to the algorithm prior
 * to Jena 2.11.2. It is susceptible to picking bad orders when there are lots
 * of non-characteristic (non-selective) rdf:type triples.
 * <p>
 * The default "ReorderFixed" is better in most cases because it avoids
 * "? rdf:type T" which can be very unselective. Being data independent, that is
 * a guess. Consider using the stats matcher for detailed control.
 */
public class ReorderFixedAlt extends ReorderTransformationSubstitution
{
    public ReorderFixedAlt() {}
    
    // Fixed scheme for when we have no stats.
    // It chooses a triple pattern by order of preference.
    
    private static Item type = Item.createNode(NodeConst.nodeRDFType) ;
    
    /** The number of triples used for the base scale */
    public static int MultiTermSampleSize = 100 ; 

    /** Maximum value for a match involving two terms. */
    public static int MultiTermMax = 9 ; 
    
    public final static StatsMatcher matcher ;
    static {
        matcher = new StatsMatcher() ;
        
        //matcher.addPattern(new Pattern(1,   TERM, TERM, TERM)) ;     // SPO - built-in - not needed a s a rule
        
        // Numbers choosen as an approximation ratios for a graph of 100 triples
        matcher.addPattern(new Pattern(2,   TERM, TERM, VAR)) ;     // SP?
        
        // Pointless - this rule is over ridden by lower weight ?PO
		// matcher.addPattern(new Pattern(5,   TERM, type, TERM)) ;    // ? type O -- worse than ?PO

        matcher.addPattern(new Pattern(3,   VAR,  TERM, TERM)) ;    // ?PO
        matcher.addPattern(new Pattern(2,   TERM, VAR,  TERM)) ;    // S?O
        
        matcher.addPattern(new Pattern(10,  TERM, VAR,  VAR)) ;     // S??
        matcher.addPattern(new Pattern(20,  VAR,  VAR,  TERM)) ;    // ??O
        matcher.addPattern(new Pattern(30,  VAR,  TERM, VAR)) ;     // ?P?

        matcher.addPattern(new Pattern(MultiTermSampleSize, VAR,  VAR,  VAR)) ;     // ???
    }
    
    @Override
    public double weight(PatternTriple pt)
    {
        return matcher.match(pt) ;
    }
}
