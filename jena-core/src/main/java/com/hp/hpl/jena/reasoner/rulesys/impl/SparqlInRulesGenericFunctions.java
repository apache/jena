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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import java.util.ArrayList;
import java.util.List;


public class SparqlInRulesGenericFunctions {
    public static boolean anyVariableInQueryPattern(Query q) {
        boolean retV = false;
        
        ElementGroup el = (ElementGroup)q.getQueryPattern() ;
       
        int i=0;
        List<Element> bgp_list = el.getElements();
        while(i<bgp_list.size() && !retV) {
            Element bgp = bgp_list.get(i);
            if(bgp instanceof ElementPathBlock) {
                ElementPathBlock elp = (ElementPathBlock)bgp ;
                PathBlock pb = elp.getPattern() ;
                for ( TriplePath tp : pb ) {
                    Triple t = tp.asTriple() ;
                    if ( t != null ) {
                        retV = t.getPredicate().isVariable();
                    }
                }               
            } else if(bgp instanceof ElementSubQuery) {
                retV = anyVariableInQueryPattern(((ElementSubQuery)bgp).getQuery());
            }
            i++;
        }
      
        return retV;
    }

    public static ArrayList<Node> getPredicatesQueryPattern(Query q) {
        ArrayList<Node> retNodes = new ArrayList<Node>();
        
        ElementGroup el = (ElementGroup)q.getQueryPattern() ;
       
        for(Element bgp : el.getElements()) {
       
            if(bgp instanceof ElementPathBlock) {
                ElementPathBlock elp = (ElementPathBlock)bgp ;
                PathBlock pb = elp.getPattern() ;
                for ( TriplePath tp : pb ) {
                    Triple t = tp.asTriple() ;
                    if ( t != null ) {
                        Node n = t.getPredicate();
                        if(t.getPredicate().isVariable()) {
                            n = Node.ANY;
                        }
                        if(!retNodes.contains(n)){
                            retNodes.add(n);
                        }
                    }
                }               
            } else if(bgp instanceof ElementSubQuery) {
                for(Node n : getPredicatesQueryPattern(((ElementSubQuery)bgp).getQuery())) {
                    if(!retNodes.contains(n)){
                            retNodes.add(n);
                        }
                }
            }
        }

        return retNodes;
    }    
    
    public static ArrayList<Triple> getQueryPattern(Query q) {
        ArrayList<Triple> retTriples = new ArrayList<Triple>();
        
        ElementGroup el = (ElementGroup)q.getQueryPattern() ;
       
        for(Element bgp : el.getElements()) {
       
            if(bgp instanceof ElementPathBlock) {
                ElementPathBlock elp = (ElementPathBlock)bgp ;
                PathBlock pb = elp.getPattern() ;
                for ( TriplePath tp : pb ) {
                    Triple t = tp.asTriple() ;
                    if ( t != null ) {
                        Node subject = t.getSubject();
                        if(subject.isVariable()) {
                            subject = Node.ANY;
                        }
                        Node predicate = t.getPredicate();
                        if(predicate.isVariable()) {
                            predicate = Node.ANY;
                        }
                        Node object = t.getObject();
                        if(object.isVariable()) {
                            object = Node.ANY;
                        }
                        retTriples.add(new Triple(subject, predicate, object));
                    }        
                }               
            } else if(bgp instanceof ElementSubQuery) {
                for(Triple t : getQueryPattern(((ElementSubQuery)bgp).getQuery())) {
                    if(!retTriples.contains(t)){
                            retTriples.add(t);
                        }
                }
            }
        }

        return retTriples;
    }
}
