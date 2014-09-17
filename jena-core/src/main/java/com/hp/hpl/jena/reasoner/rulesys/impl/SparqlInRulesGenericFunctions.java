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
 
 
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class SparqlInRulesGenericFunctions {
    
    static String [][] validHex = {
            {"0030","0039"} , {"0041","005a"} , {"005f", "005f"}, 
            {"0061","007a"}, 
            {"00C0","00D6"} , {"00D8","00F6"} , {"00F8","02FF"} , 
            {"0370","037D"} , {"037F","1FFF"} , {"200C","200D"} , 
            {"2070","218F"} , {"2C00","2FEF"} , {"3001","D7FF"} , 
            {"F900","FDCF"} , {"FDF0","FFFD"} , {"10000","EFFFF"}
            };
        
    
    static String [][] validHex2 = {
        {"00B7", "00B7"}, {"0300", "036F"}, {"203F", "2040"}
    };
    
    public static boolean validCharFirst(char ch) {
        return validChar(ch, validHex);
    }
    
    public static boolean validCharOthers(char ch) {
        return validChar(ch, validHex) || validChar(ch, validHex2);
    }

    private static boolean validChar(char ch, String [][] validHex) {
        boolean retV = false;
        int i=-1;
        
        while(i<validHex.length && ((int) ch) >= Integer.parseInt(validHex[i+1][0], 16)) {
            i++;
        }
        if(i>= 0 && ch <= Integer.parseInt(validHex[i][1], 16)) {
            retV = true;
        }
        
        return retV;
    }
    
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
    
    public static ArrayList<String> getAllVars(Query q) {
        ArrayList<String> retVars = new ArrayList<String>();
        ElementGroup el = (ElementGroup)q.getQueryPattern() ;

        for(Element bgp : el.getElements()) {
            al_addAll_withoutRep(retVars, getAllVars(bgp));
        }
        if(q.getGroupBy() != null) {
            for(Var v : q.getGroupBy().getVars()) {
                al_add_withoutRep(retVars, v.getVarName());
                if(q.getGroupBy().hasExpr(v)) {
                    al_addAll_withoutRep(retVars, getVarExpr(q.getGroupBy().getExpr(v)));
                }    
            }
        }
        List<Expr> having = q.getHavingExprs();
        if(having != null) {
            for(Expr expr : q.getHavingExprs()) {
                al_addAll_withoutRep(retVars, getVarExpr(expr));
            }
        }
        List<ExprAggregator> agg = q.getAggregators();
        if(agg != null) {
            ListIterator<ExprAggregator> li = agg.listIterator();
            while(li.hasNext()){
                al_addAll_withoutRep(retVars, getVarExpr(li.next().getExpr()));
            }
        }
        
        return retVars;
    }
    
    public static ArrayList<String> getAllVars(Element bgp) {
        ArrayList<String> retVars = new ArrayList<String>();
     
        if(bgp instanceof ElementPathBlock) {
                ElementPathBlock el_bgp = (ElementPathBlock)bgp ;
                PathBlock pb = el_bgp.getPattern() ;
                for ( TriplePath tp : pb ) {
                    Triple t = tp.asTriple() ;
                    if(t.getSubject().isVariable()) {
                        al_add_withoutRep(retVars, t.getSubject().getName());
                    }
                    if(t.getPredicate().isVariable()) {
                        al_add_withoutRep(retVars, t.getPredicate().getName());
                    }
                    if(t.getObject().isVariable()) {
                        al_add_withoutRep(retVars, t.getObject().getName());
                    }
                }               
            }
            if(bgp instanceof ElementAssign) {
                ElementAssign el_bgp = (ElementAssign) bgp;
                al_addAll_withoutRep(retVars, getVarExpr(el_bgp.getExpr()));
            }
            if(bgp instanceof ElementBind) {
                ElementBind el_bgp = (ElementBind) bgp;
                al_addAll_withoutRep(retVars, getVarExpr(el_bgp.getExpr()));
            }
            if(bgp instanceof ElementData) {
                ElementData el_bgp = (ElementData) bgp;
                for(Var v : el_bgp.getVars()) {
                    al_add_withoutRep(retVars, v.getVarName());
                }
            }
            if(bgp instanceof ElementDataset) {
                ElementDataset el_bgp = (ElementDataset) bgp;
                al_addAll_withoutRep(retVars, getAllVars(el_bgp.getPatternElement()));
            }
            if(bgp instanceof ElementExists) {
                ElementExists el_bgp = (ElementExists) bgp;
                al_addAll_withoutRep(retVars, getAllVars(el_bgp.getElement()));
            }
            if(bgp instanceof ElementFilter) {
                ElementFilter el_bgp = (ElementFilter) bgp;
                al_addAll_withoutRep(retVars, getVarExpr(el_bgp.getExpr()));               
            }
            if(bgp instanceof ElementGroup) {
                ElementGroup el_bgp = (ElementGroup) bgp;
                for(Element elTmp : el_bgp.getElements()) {
                    al_addAll_withoutRep(retVars, getAllVars(elTmp));
                }

            }
            if(bgp instanceof ElementMinus) {
                ElementMinus el_bgp = (ElementMinus) bgp;
                al_addAll_withoutRep(retVars, getAllVars(el_bgp.getMinusElement()));
            }
            if(bgp instanceof ElementNamedGraph) {
                ElementNamedGraph el_bgp = (ElementNamedGraph) bgp;
                al_addAll_withoutRep(retVars, getAllVars(el_bgp.getElement()));
            }
            if(bgp instanceof ElementNotExists) {
                ElementNotExists el_bgp = (ElementNotExists) bgp;
                al_addAll_withoutRep(retVars, getAllVars(el_bgp.getElement()));
            }
            if(bgp instanceof ElementOptional) {
                ElementOptional el_bgp = (ElementOptional) bgp;
                al_addAll_withoutRep(retVars, getAllVars( el_bgp.getOptionalElement()));
            }
            if(bgp instanceof ElementService) {
                ElementService el_bgp = (ElementService) bgp;
                al_addAll_withoutRep(retVars, getAllVars( el_bgp.getElement()));
            }
            if(bgp instanceof ElementSubQuery) {
                ElementSubQuery el_bgp = (ElementSubQuery) bgp;
                al_addAll_withoutRep(retVars, getAllVars( el_bgp.getQuery()));
            }
            if(bgp instanceof ElementUnion) {
                ElementUnion el_bgp = (ElementUnion) bgp;
               for(Element elTmp : el_bgp.getElements()) {
                    al_addAll_withoutRep(retVars, getAllVars(elTmp));
                }
            }
       return retVars;
    }
             
    public static void al_addAll_withoutRep(ArrayList<String> al1, ArrayList<String> al2) {
        for(int i=0; i<al2.size(); i++) {
            if(!al1.contains(al2.get(i))) {
                al1.add(al2.get(i));
            }
        }
    }
    

    public static void al_add_withoutRep(ArrayList<String> al1, String s) {
        if(!al1.contains(s)) {
            al1.add(s);
        }
    }

    
    public static ArrayList<String>  getVarExpr(Expr expr) {
        ArrayList<String> retVars = new ArrayList<String>();
        if(expr != null &&  expr.getVarsMentioned() != null) {
            Iterator<Var> exprVars =  expr.getVarsMentioned().iterator();
            while(exprVars.hasNext()) {
                al_add_withoutRep(retVars, exprVars.next().getVarName());
            }
        }
        
        return retVars;
    }
}
