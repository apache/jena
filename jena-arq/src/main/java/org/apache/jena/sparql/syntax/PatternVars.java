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

package org.apache.jena.sparql.syntax;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.util.VarUtils;

/**
 * Get the variables potentially bound by an element. All mentioned variables except
 * those in MINUS, SEMIJOIN, ANTIJOIN and FILTER (and hence NOT EXISTS).
 */
public class PatternVars {
    public static Collection<Var> vars(Element element) {
        return vars(new LinkedHashSet<Var>(), element);
    }

    public static Collection<Var> vars(Collection<Var> s, Element element) {
        PatternVarsVisitor v = new PatternVarsVisitor(s);
        vars(element, v);
        return s;
    }

    public static void vars(Element element, PatternVarsVisitor visitor) {
        ElementWalker.EltWalker walker = new WalkerSkipNonBinding(visitor);
        ElementWalker.walk$(element, walker);
    }

    /** Algebra forms that don't contribute variables to "SELECT *". */
    private static class WalkerSkipNonBinding extends ElementWalker.EltWalker {
        protected WalkerSkipNonBinding(ElementVisitor visitor) {
            super(visitor, null, null);
        }

        @Override
        public void visit(ElementMinus el) { proc.visit(el); }

        @Override
        public void visit(ElementSemiJoin el) { proc.visit(el); }

        @Override
        public void visit(ElementAntiJoin el) { proc.visit(el); }
    }

    private static class PatternVarsVisitor extends ElementVisitorBase
    {
        public Collection<Var> acc;
        /*package*/ PatternVarsVisitor(Collection<Var> s) {
            acc = s;
        }

        @Override
        public void visit(ElementTriplesBlock el) {
            for ( Iterator<Triple> iter = el.patternElts(); iter.hasNext(); ) {
                Triple t = iter.next();
                VarUtils.addVarsFromTriple(acc, t);
            }
        }

        @Override
        public void visit(ElementPathBlock el) {
            for ( Iterator<TriplePath> iter = el.patternElts(); iter.hasNext(); ) {
                TriplePath tp = iter.next();
                // If it's triple-izable, then use the triple.
                if ( tp.isTriple() )
                    VarUtils.addVarsFromTriple(acc, tp.asTriple());
                else
                    VarUtils.addVarsFromTriplePath(acc, tp);
            }
        }

        // Variables here are non-binding.
        @Override public void visit(ElementExists el)       { }
        @Override public void visit(ElementNotExists el)    { }
        @Override public void visit(ElementMinus el)        { }
        @Override public void visit(ElementSemiJoin el)     { }
        @Override public void visit(ElementAntiJoin el)     { }
        @Override public void visit(ElementFilter el)       { }

        @Override
        public void visit(ElementNamedGraph el) {
            VarUtils.addVar(acc, el.getGraphNameNode());
        }

        @Override
        public void visit(ElementSubQuery el) {
            VarExprList x = el.getQuery().getProject();
            acc.addAll(x.getVars());
        }

        @Override
        public void visit(ElementAssign el) {
            acc.add(el.getVar());
        }

        @Override
        public void visit(ElementBind el) {
            acc.add(el.getVar());
        }

        @Override
        public void visit(ElementUnfold el) {
            acc.add(el.getVar1());
            if ( el.getVar2() != null )
                acc.add(el.getVar2());
        }

        @Override
        public void visit(ElementData el) {
            acc.addAll(el.getVars());
        }

//        @Override
//        public void visit(ElementService el) {
//            // Although if this isn't defined elsewhere the query won't work.
//            VarUtils.addVar(acc, el.getServiceNode());
//        }
    }
}
