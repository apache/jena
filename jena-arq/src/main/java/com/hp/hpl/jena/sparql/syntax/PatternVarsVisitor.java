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

package com.hp.hpl.jena.sparql.syntax;

import java.util.Collection ;
import java.util.Iterator ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.util.VarUtils ;

public class PatternVarsVisitor extends ElementVisitorBase
{
    public Collection<Var> acc ;
    public PatternVarsVisitor(Collection<Var> s) { acc = s ; } 
    
    @Override
    public void visit(ElementTriplesBlock el)
    {
        for (Iterator<Triple> iter = el.patternElts() ; iter.hasNext() ; )
        {
            Triple t = iter.next() ;
            VarUtils.addVarsFromTriple(acc, t) ;
        }
    }

    @Override
    public void visit(ElementPathBlock el) 
    {
        for (Iterator<TriplePath> iter = el.patternElts() ; iter.hasNext() ; )
        {
            TriplePath tp = iter.next() ;
            // If it's triple-izable, then use the triple. 
            if ( tp.isTriple() )
                VarUtils.addVarsFromTriple(acc, tp.asTriple()) ;
            else
                VarUtils.addVarsFromTriplePath(acc, tp) ;
        }
    }
        
    // Variables here are non-binding.
    @Override public void visit(ElementExists el)       { }
    @Override public void visit(ElementNotExists el)    { }
    @Override public void visit(ElementMinus el)        { }
    @Override public void visit(ElementFilter el)       { }

    @Override
    public void visit(ElementNamedGraph el)
    {
        VarUtils.addVar(acc, el.getGraphNameNode()) ;
    }

    @Override
    public void visit(ElementSubQuery el)
    {
        el.getQuery().setResultVars() ;
        VarExprList x = el.getQuery().getProject() ;
        acc.addAll(x.getVars()) ;
    }

    @Override
    public void visit(ElementAssign el)
    {
        acc.add(el.getVar()) ;
    }

    @Override
    public void visit(ElementBind el)
    {
        acc.add(el.getVar()) ;
    }

    @Override
    public void visit(ElementData el)
    {
        acc.addAll(el.getVars()) ;
    }

    
//    @Override
//    public void visit(ElementService el)
//    {
//        // Although if this isn't defined elsewhere the query won't work.
//        VarUtils.addVar(acc, el.getServiceNode()) ;
//    }
    
}
