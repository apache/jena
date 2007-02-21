/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.algebra;

import java.io.OutputStream;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.query.algebra.op.*;
import com.hp.hpl.jena.query.core.ARQConstants;
import com.hp.hpl.jena.query.core.Quad;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.Plan;
import com.hp.hpl.jena.query.expr.ExprList;
import com.hp.hpl.jena.query.serializer.SerializationContext;
import com.hp.hpl.jena.query.util.ExprUtils;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.query.util.PrintUtils;

public class OpWriter
{
    private static final int NL = 1 ;
    private static final int NoNL = -1 ;
    
    public static void out(OutputStream out, Op op, PrefixMapping pMap)
    {
        SerializationContext sCxt = new SerializationContext(pMap) ;
        out(new IndentedWriter(out), op, sCxt) ;
    }

    public static void out(OutputStream out, Op op, SerializationContext sCxt)
    {
        out(new IndentedWriter(out), op, sCxt) ;
    }

    public static void out(OutputStream out, Op op)
    { out(out, op, ARQConstants.getGlobalPrefixMap()) ; }

    public static void out(IndentedWriter iWriter, Op op, PrefixMapping pMap)
    {
        SerializationContext sCxt = new SerializationContext(pMap) ;
        out(iWriter, op, sCxt) ;
    }

    public static void out(IndentedWriter iWriter, Op op, SerializationContext sCxt)
    {
        op.visit(new OpWriterWorker(iWriter, sCxt)) ;
        iWriter.ensureStartOfLine() ;
        iWriter.flush();
    }

    public static void out(IndentedWriter iWriter, Op op)
    { out(iWriter, op, ARQConstants.getGlobalPrefixMap()) ; }

    
    static class OpWriterWorker implements OpVisitor
    {
        private IndentedWriter out ;
        private SerializationContext sContext ;
        public OpWriterWorker(IndentedWriter out, SerializationContext sCxt)
        { 
            this.sContext = sCxt ;
            this.out = out ;
        }
        
        private void visitOp2(Op2 op, ExprList exprs)
        {
            start(op, NL) ;
            printOp(op.getLeft()) ;

            //out.println() ; 
            out.ensureStartOfLine() ;

            printOp(op.getRight()) ;

            out.ensureStartOfLine() ;
            //out.println() ; 

            if ( exprs != null )
            {
                out.print("(expr ") ;
                ExprUtils.fmtSPARQL(out, exprs) ;
                out.println(" )") ;
            }
            finish(op) ;
        }

        private void visitOp1(Op1 op)
        {
            start(op, NL) ;
            printOp(op.getSubOp()) ;
            finish(op) ;
        }

        public void visit(OpQuadPattern opQuadP)
        { 
            if ( opQuadP.getQuads().size() == 1 )
            {
                start(opQuadP, NoNL) ;
                out.print(" ") ;
                formatQuad((Quad)opQuadP.getQuads().get(0)) ;
                finish(opQuadP) ;
                return ;
            }
            start(opQuadP, NL) ;
            //boolean first
            for ( Iterator iter = opQuadP.getQuads().listIterator() ; iter.hasNext() ;)
            {
               Quad quad = (Quad)iter.next() ;
               formatQuad(quad) ;
               out.println() ;
            }
            finish(opQuadP) ;
        }
        
        public void visit(OpBGP opBGP)
        {
            if ( opBGP.getPattern().size() == 1 )
            {
                start(opBGP, NoNL) ;
                out.print(" ") ;
                formatTriple((Triple)opBGP.getPattern().get(0)) ;
                finish(opBGP) ;
                return ;
            }
            
            start(opBGP, NL) ;
            for ( Iterator iter = opBGP.getPattern().iterator() ; iter.hasNext() ;)
            {
               Triple t = (Triple)iter.next() ;
               formatTriple(t) ;
               out.println() ;
            }
            finish(opBGP) ;
        }
        
        public void visit(OpJoin opJoin)
        { visitOp2(opJoin, null) ; }

        public void visit(OpLeftJoin opLeftJoin)
        { visitOp2(opLeftJoin, opLeftJoin.getExprs()) ; }
    
        public void visit(OpUnion opUnion)
        { visitOp2(opUnion, null) ; } 
    
        public void visit(OpFilter opFilter)
        { 
            start(opFilter, NoNL) ;
            out.print(" ") ;
            ExprList exprs = opFilter.getExprs() ;
            if ( exprs == null )
                out.print("(null expr)") ;
            else
                ExprUtils.fmtSPARQL(out, exprs, sContext.getPrefixMapping()) ;
            out.println();
            printOp(opFilter.getSubOp()) ;
            finish(opFilter) ;
        }
    
        public void visit(OpGraph opGraph)
        {
            start(opGraph, NoNL) ;
            out.print(" ") ;
            out.println(FmtUtils.stringForNode(opGraph.getNode())) ;
            out.incIndent() ;
            opGraph.getSubOp().visit(this) ;
            finish(opGraph) ;
        }

        public void visit(OpUnit opUnit)
        {
            start(opUnit, NoNL) ;
            finish(opUnit) ;
        }
        
        public void visit(OpDatasetNames dsNames)
        {

            out.print("(TableDatasetNames") ;
            out.print(" "); 
            out.print(slotToString(dsNames.getGraphNode())) ;
        }

        public void visit(OpExt opExt)
        {
            //start("OpExt") ;
            opExt.output(out, sContext) ;
            //finish() ;
        }

        public void visit(OpOrder opOrder)
        { 
            start(opOrder, NoNL) ;
            if ( opOrder.getConditions().size() > 0 )
            {
                String sep = " " ;
                for ( Iterator iter = opOrder.getConditions().iterator() ; iter.hasNext(); )
                {
                    SortCondition sc = (SortCondition)iter.next() ;
                    out.print(sep) ;
                    sc.output(out, sContext) ;
                }
            }
            out.println();
            printOp(opOrder.getSubOp()) ;
            finish(opOrder) ;
        }

        public void visit(OpProject opProject)
        { 
            start(opProject, NoNL) ;
            if ( opProject.getVars().size() > 0 )
            {
                
                out.print(" ") ;
                PrintUtils.Fmt fmt = new PrintUtils.Fmt(){
                    public String fmt(Object thing)
                    {
                        return ((Var)thing).toString() ;
                    }} ;
                PrintUtils.printList(out, opProject.getVars(), " ", fmt) ;
            }
            out.println();
            printOp(opProject.getSubOp()) ;
            finish(opProject) ;
        }

        public void visit(OpDistinct opDistinct)
        {
            visitOp1(opDistinct) ;
        }
        
        public void visit(OpSlice opSlice)
        { 
            start(opSlice, NoNL) ;
            out.print(" ") ;
            out.print(Long.toString(opSlice.getStart())) ;
            out.print(" ") ;
            out.print(Long.toString(opSlice.getLength())) ;
            out.println() ;
            printOp(opSlice.getSubOp()) ;
            finish(opSlice) ; }

//        private void start(Op op)
//        { start(op, true) ; }

//        private void simple(Op op)
//        { start(op, NL) ; finish(op) ; }
        
        private void start(Op op, int newline)
        {

            out.print(Plan.startMarker) ;
            out.print(op.getName()) ;
            if ( newline == NL ) out.println();
            out.incIndent() ;
        }
        
        private void finish(Op op)
        {
            out.decIndent() ;
            // Allow different opens to have different layouts. 
            //out.ensureStartOfLine() ;
            out.print(Plan.finishMarker) ;
        }
        
        private void printOp(Op op)
        {
            if ( op == null )
                out.print("(null)") ;
            else
                op.visit(this) ;
        }
        
        
        
        private void formatTriple(Triple tp)
        {
            out.print(slotToString(tp.getSubject())) ;
            out.print(" ") ;
            out.print(slotToString(tp.getPredicate())) ;
            out.print(" ") ;
            out.print(slotToString(tp.getObject())) ;
        }

        private void formatQuad(Quad qp)
        {
            out.print(slotToString(qp.getGraph())) ;
            out.print(" ") ;
            out.print(slotToString(qp.getSubject())) ;
            out.print(" ") ;
            out.print(slotToString(qp.getPredicate())) ;
            out.print(" ") ;
            out.print(slotToString(qp.getObject())) ;
        }

        private String slotToString(Node n)
        {
            return FmtUtils.stringForNode(n, sContext) ;
        }
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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