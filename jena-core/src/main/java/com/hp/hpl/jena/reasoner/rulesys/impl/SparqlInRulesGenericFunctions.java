/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
 * @author mba
 */
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
