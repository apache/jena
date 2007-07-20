/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.util.StringUtils;

/** Convert an Op expression in SPARQL syntax, that is, the reverse of algebra generation */   
public class OpToSyntax
{
    public static void main(String[] argv)
    {
        String [] a1 = new String[]{
            "PREFIX : <http://example/>",
            "SELECT *",
            " { {?s ?p ?o }",
            "   UNION {",
            "     ?s ?p ?o . ",
            "     FILTER(?o)",
            "     OPTIONAL{?s :p :z }",
            "   } UNION { ?a ?b ?c }",
            "}"
        } ;

        String [] a2 = new String[]{
            "PREFIX : <http://example/>",
            "SELECT *",
            "{ { ?s1 ?p1 ?o1 } UNION { ?s2 ?p2 ?o2 } }" //UNION { ?s3 ?p3 ?o3 } }" 
        } ;

        String qs = StringUtils.join("\n", a2) ;
        
        Query query = QueryFactory.create(qs) ;
        Op op = Algebra.compile(query) ;
        System.out.print(op) ;
        System.out.println() ;
        Query query2 = asQuery(op) ;
        System.out.print(query2) ;
    }
    
    public static Query asQuery(Op op)
    {
        Query query = QueryFactory.make() ;
        query.setQueryResultStar(true) ;
        
        Converter v = new Converter(query) ;
        //OpWalker.walk(op, v) ;
        op.visit(v) ;
        
        query.setQueryPattern(v.currentGroup) ;
        query.setQuerySelectType() ;
        
        query.setResultVars() ; 
        // Always name the variables, no "SELECT *"
        query.setQueryResultStar(false) ;
        return query ; 
    }
    
    static class Converter implements OpVisitor
    {
        Query query ;
        Element element = null ;
        ElementGroup currentGroup = null ;
        Stack stack = new Stack() ;
        public Converter(Query query)
        {
            this.query = query ;
        }

        Element asElement(Op op)
        {
            op.visit(this) ;
            Element el = element ;
            element = null ;
            return el ;
//            Element e = pop() ;
//            return e ;
        }
        
        public void visit(OpBGP opBGP)
        {
            ElementTriplesBlock e = new ElementTriplesBlock() ;
            Iterator iter = opBGP.getPattern().iterator() ;
            for ( ; iter.hasNext() ; )
            {
                Triple t = (Triple)iter.next() ;
                e.addTriple(t) ;
            }
            element = e ;
        }

        public void visit(OpQuadPattern quadPattern)
        { throw new ARQNotImplemented("OpQuadPattern") ; }

        public void visit(OpJoin opJoin)
        {
            Element eLeft = asElement(opJoin.getLeft()) ;
            Element eRight = asElement(opJoin.getRight()) ;
            
            ElementGroup g = currentGroup() ;
            g.addElement(eLeft) ;
            g.addElement(eRight) ;
            return ;
        }

        public void visit(OpLeftJoin opLeftJoin)
        {
            Element eLeft = asElement(opLeftJoin.getLeft()) ;
            Element eRight = asElement(opLeftJoin.getRight()) ;
            ElementGroup g = currentGroup() ;
            g.addElement(eLeft) ;
            ElementOptional opt = new ElementOptional(eRight) ;
            g.addElement(opt) ;
        }

        public void visit(OpDiff opDiff)
        { throw new ARQNotImplemented("OpDiff") ; }

        public void visit(OpUnion opUnion)
        {
            Element eLeft = asElement(opUnion.getLeft()) ;
            Element eRight = asElement(opUnion.getRight()) ;
            if ( eLeft instanceof ElementUnion )
            {
                ElementUnion elUnion = (ElementUnion)eLeft ;
                elUnion.addElement(eRight) ;
                return ;
            }
            
//            if ( eRight instanceof ElementUnion )
//            {
//                ElementUnion elUnion = (ElementUnion)eRight ;
//                elUnion.getElements().add(0, eLeft) ;
//                return ;
//            }
            
            ElementUnion elUnion = new ElementUnion() ;
            elUnion.addElement(eLeft) ;
            elUnion.addElement(eRight) ;
            currentGroup().addElement(elUnion) ;
        }

        public void visit(OpFilter opFilter)
        {
            Element e = asElement(opFilter.getSubOp()) ;
            if ( currentGroup() != e )
                currentGroup().addElement(e) ;
            element = currentGroup() ;      // Was cleared by asElement. 
            
            ExprList exprs = opFilter.getExprs() ;
            for ( Iterator iter = exprs.iterator() ; iter.hasNext(); )
            {
                Expr expr = (Expr)iter.next();
                ElementFilter f = new ElementFilter(expr) ;
                currentGroup().addElement(f) ;
            }
        }

        public void visit(OpGraph opGraph)
        {}

        public void visit(OpService opService)
        { throw new ARQNotImplemented("OpService") ; }


        public void visit(OpDatasetNames dsNames)
        { throw new ARQNotImplemented("OpDatasetNames") ; }

        public void visit(OpTable opTable)
        { throw new ARQNotImplemented("OpTable") ; }

        public void visit(OpExt opExt)
        { throw new ARQNotImplemented("OpExt") ; }

        public void visit(OpNull opNull)
        { throw new ARQNotImplemented("OpNull") ; }

        public void visit(OpList opList)
        { /* No action */ }

        public void visit(OpOrder opOrder)
        {
            List x = opOrder.getConditions() ;
            Iterator iter = x.iterator() ;
            for ( ; iter.hasNext(); )
            {
                SortCondition sc = (SortCondition)iter.next();
                query.addOrderBy(sc);
            }
        }

        public void visit(OpProject opProject)
        {
            query.setQueryResultStar(false) ;
            Iterator iter = opProject.getVars().iterator() ;
            for ( ; iter.hasNext() ; )
            {
                Var v = (Var)iter.next();
                query.addResultVar(v) ;
            }
        }

        public void visit(OpReduced opReduced)
        {}

        public void visit(OpDistinct opDistinct)
        { query.setDistinct(true) ; }

        public void visit(OpSlice opSlice)
        {
            if ( opSlice.getStart() != Query.NOLIMIT )
                query.setOffset(opSlice.getStart()) ;
            if ( opSlice.getLength() != Query.NOLIMIT )
                query.setLimit(opSlice.getLength()) ;
        }

        public void visit(OpGroupAgg opGroupAgg)
        { throw new ARQNotImplemented("OpGroupAgg") ; }
        
        private Element lastElement()
        {
            ElementGroup g = currentGroup ;
            if ( g == null || g.getElements().size() == 0 )
                return null ;
            int len = g.getElements().size() ;
            return (Element)g.getElements().get(len-1) ;
        }

        
        private ElementGroup currentGroup()
        {
            ElementGroup g = currentGroup ;
            if ( g == null )
            {
                g = new ElementGroup() ;
                if ( element != null )
                    System.err.println("element not null") ;
                element = g ;
                currentGroup = g ;
            }
            return g ;
        }
//            ElementGroup g = null ;
//            if ( peek() instanceof ElementGroup )
//                g = (ElementGroup)(peek()) ;
//            else
//            {
//                g = new ElementGroup() ;
//                push(g) ;
//            }
//            return g ;
//        }
        
//        private Element peek()
//        {
//            if ( stack.size() == 0 )
//                return null ;
//            return (Element)stack.peek();
//        }
//        private Element pop() { return (Element)stack.pop(); }
//        private void push(Element el) { stack.push(el); }
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */