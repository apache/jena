/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.algebra.table.TableUnit;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.E_Aggregator;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.serializer.FmtExprPrefix;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.NodeToLabelMap;


public class OpWriter
{
    private static final int NL = 1 ;
    private static final int NoNL = -1 ;
    
    public static void out(Op op)
    { out(System.out, op) ; }
    
    public static void out(Op op, PrefixMapping pMap)
    { out(System.out, op, pMap) ; }
    
    public static void out(Op op, Prologue prologue)
    { out(System.out, op, prologue) ; }
    
    public static void out(OutputStream out, Op op)
    { out(out, op, (PrefixMapping)null) ; }

    public static void out(OutputStream out, Op op, PrefixMapping pMap)
    { out(new IndentedWriter(out), op, pMap) ; }

    public static void out(OutputStream out, Op op, Prologue prologue)
    { out(new IndentedWriter(out), op, prologue) ; }

    public static void out(IndentedWriter iWriter, Op op)
    { 
        PrefixMapping pmap = ARQConstants.getGlobalPrefixMap() ;
        out(iWriter, op, pmap) ;
    }

    public static void out(IndentedWriter iWriter, Op op, PrefixMapping pMap)
    {
        SerializationContext sCxt = new SerializationContext(pMap) ;
        out(iWriter, op, sCxt) ;
    }

    public static void out(IndentedWriter iWriter, Op op, Prologue prologue)
    {
        SerializationContext sCxt = new SerializationContext(prologue) ;
        out(iWriter, op, sCxt) ;
    }
    
    public static void out(OutputStream out, Op op, SerializationContext sCxt)
    {
        out(new IndentedWriter(out), op, sCxt) ;
    }

    // Actual work
    public static void out(IndentedWriter iWriter, Op op, SerializationContext sCxt)
    {
        // TODO Consider whether this ought to always fix the bNode label map or not.
        NodeToLabelMap lmap = new NodeToLabelMap() ;
        sCxt.setBNodeMap(lmap) ;

        int closeCount = 0 ;
        
//        if ( sCxt.getBaseIRI() != null )
//        {
              // And serialize URIs based on base
//            iWriter.print("(base <") ;
//            iWriter.print(sCxt.getBaseIRI()) ;
//            iWriter.println(">") ;
//            iWriter.incIndent() ;
//            closeCount ++ ;
//        }
        if ( sCxt.getPrefixMapping() != null )
        {
            Map m = sCxt.getPrefixMapping().getNsPrefixMap() ;
            if ( ! m.isEmpty() )
            {
                String tagStr = "(prefix (" ;
                int len = tagStr.length() ;
                iWriter.print(tagStr) ;
                iWriter.incIndent(len) ;
                Iterator iter = m.keySet().iterator();
                boolean first = true ;
                for ( ; iter.hasNext() ; )
                {
                    if ( ! first )
                        iWriter.println() ;
                    first = false ;
                    String prefix = (String)iter.next();
                    String uri = sCxt.getPrefixMapping().getNsPrefixURI(prefix) ;
                    iWriter.print("("+prefix+": <"+uri+">)") ;
                }
                iWriter.println(")") ;
                iWriter.decIndent(len) ;
                iWriter.incIndent() ;
                closeCount ++ ;
            }
        }
        
        op.visit(new OpWriterWorker(iWriter, sCxt)) ;
        for ( int i = 0 ; i < closeCount ; i++)
        {
            iWriter.print(")") ;
            iWriter.decIndent() ;
        }
        iWriter.ensureStartOfLine() ;
        iWriter.flush();
    }

    
    static class OpWriterWorker implements OpVisitor
    {
        private IndentedWriter out ;
        private SerializationContext sContext ;
        private VarAlloc varAlloc = new VarAlloc("__") ;
        private FmtExprPrefix fmtExpr ; 
        
        public OpWriterWorker(IndentedWriter out, SerializationContext sCxt)
        { 
            this.sContext = sCxt ;
            this.out = out ;
            this.fmtExpr = new FmtExprPrefix(out, sCxt) ;
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
                ExprUtils.fmtPrefix(out, exprs) ;
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
                formatTriple(opBGP.getPattern().get(0)) ;
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
        
        public void visit(OpDiff opDiff)
        { visitOp2(opDiff, null) ; }
    
        public void visit(OpUnion opUnion)
        { visitOp2(opUnion, null) ; } 
    
        public void visit(OpFilter opFilter)
        { 
            start(opFilter, NoNL) ;
            out.print(" ") ;
            ExprList exprs = opFilter.getExprs() ;
            if ( exprs == null )
                out.print("()") ;
            else
                //ExprUtils.fmtSPARQL(out, exprs, sContext.getPrefixMapping()) ;
                ExprUtils.fmtPrefix(out, exprs, sContext.getPrefixMapping()) ;
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

        public void visit(OpService opService)
        {
            start(opService, NoNL) ;
            out.print(" ") ;
            out.println(FmtUtils.stringForNode(opService.getService())) ;
            out.incIndent() ;
            opService.getSubOp().visit(this) ;
            finish(opService) ;
        }

        public void visit(OpTable opTable)
        {
            if ( TableUnit.isJoinUnit(opTable.getTable()) )
            {
                start(opTable, NoNL) ;
                out.print(" unit") ;
                finish(opTable) ;
                return ;
            }
            
            start(opTable, NL) ;
            outputTable(opTable.getTable());
            finish(opTable) ;
        }

        private void outputTable(Table table)
        {
            QueryIterator qIter = table.iterator(null) ; 
            for ( ; qIter.hasNext(); )
            {
                Binding b = qIter.nextBinding() ;
                outputRow(b) ;
                out.println() ;
            }
            qIter.close() ;
        }
        
        private void outputRow(Binding binding)
        {
            out.print(Plan.startMarker) ;
            out.print("row") ;
            Iterator iter = binding.vars() ;
            for ( ; iter.hasNext() ; )
            {
                Var v = (Var)iter.next() ;
                Node n = binding.get(v) ;
                out.print(" ") ;
                out.print(Plan.startMarker2) ;
                out.print(FmtUtils.stringForNode(v, sContext)) ;
                out.print(" ") ;
                out.print(FmtUtils.stringForNode(n, sContext)) ;
                out.print(Plan.finishMarker2) ;
            }
            out.print(Plan.finishMarker) ;
        }
        
        public void visit(OpDatasetNames dsNames)
        {
            start() ;
            out.print("TableDatasetNames") ;
            out.print(" "); 
            out.print(slotToString(dsNames.getGraphNode())) ;
            finish() ;
        }

        public void visit(OpExt opExt)
        {
            //start("OpExt") ;
            opExt.output(out, sContext) ;
            //finish() ;
        }

        public void visit(OpNull opNull)
        { start(opNull, NoNL) ; finish() ; } 

        public void visit(OpList opList)
        {
            visitOp1(opList) ;
        }
        
        public void visit(OpGroupAgg opGroup)
        {
            start(opGroup, NoNL) ;
            out.print(" ") ;
            writeNamedExprList(opGroup.getGroupVars()) ;
            if ( ! opGroup.getAggregators().isEmpty() )
            {
                // --- Aggregators
                out.print(" (") ;
                out.incIndent() ;
                boolean first = true ;
                for ( Iterator iter = opGroup.getAggregators().iterator() ; iter.hasNext() ; )
                {
                    if ( ! first )
                        out.print(" ") ;
                    first = false ;
                    E_Aggregator agg = (E_Aggregator)iter.next();
                    Var v = agg.asVar() ;
                    String str = agg.getAggregator().toPrefixString() ;
                    out.print("(") ;
                    out.print(v.toString()) ;
                    out.print(" ") ;
                    out.print(str) ;
                    out.print(")") ;
                }
                out.print(")") ;
                out.decIndent() ;
            }
            out.println() ;
            printOp(opGroup.getSubOp()) ;
            finish(opGroup) ;
        }
        
        public void visit(OpOrder opOrder)
        { 
            start(opOrder, NoNL) ;
            out.print(" (") ;

            boolean first = true ;
            for ( Iterator iter = opOrder.getConditions().iterator() ; iter.hasNext(); )
            {
                if ( ! first )
                    out.print(" ") ;
                first = false ;
                SortCondition sc = (SortCondition)iter.next() ;
                formatSortCondition(sc) ;
            }
            out.println(")") ;
            printOp(opOrder.getSubOp()) ;
            finish(opOrder) ;
        }
        
        
        // Neater would be a pair of explicit SortCondition formatter
        public void formatSortCondition(SortCondition sc)
        {
            boolean close = true ;
            
            if ( sc.getDirection() != Query.ORDER_DEFAULT ) 
            {            
                if ( sc.getDirection() == Query.ORDER_ASCENDING )
                    out.print("(asc ") ;
            
                if ( sc.getDirection() == Query.ORDER_DESCENDING )
                    out.print("(desc ") ;
            }
            
            fmtExpr.format(sc.getExpression()) ;
            
            if ( sc.getDirection() != Query.ORDER_DEFAULT )
                out.print(")") ;
        }



        public void visit(OpProject opProject)
        { 
            start(opProject, NoNL) ;
            out.print(" ") ;
            writeNamedExprList(opProject.getProject()) ;
            out.println();
            printOp(opProject.getSubOp()) ;
            finish(opProject) ;
        }

        public void visit(OpDistinct opDistinct)
        {
            visitOp1(opDistinct) ;
        }
        
        public void visit(OpReduced opReduced)
        {
            visitOp1(opReduced) ;
        }
        
        public void visit(OpSlice opSlice)
        { 
            start(opSlice, NoNL) ;
            out.print(" ") ;
            writeIntOrDefault(opSlice.getStart()) ;
            out.print(" ") ;
            writeIntOrDefault(opSlice.getLength()) ;
            out.println() ;
            printOp(opSlice.getSubOp()) ;
            finish(opSlice) ;
        }

        private void writeIntOrDefault(long value)
        {
            String x = "_" ;
            if ( value != Query.NOLIMIT )
                x = Long.toString(value) ;
            out.print(x) ;
        }
            
        private void start()
        { out.print(Plan.startMarker) ; }
        
        private void start(Op op, int newline)
        {
            start() ;
            out.print(op.getName()) ;
            if ( newline == NL ) out.println();
            out.incIndent() ;
        }
        
        private void finish()
        { out.print(Plan.finishMarker) ; }

        private void finish(Op op)
        {
            out.decIndent() ;
            finish();
        }
        
        private void printOp(Op op)
        {
            if ( op == null )
                out.print("(null)") ;
            else
                op.visit(this) ;
        }
        
        private void writeNamedExprList(VarExprList project)
        {
            out.print("(") ;
            boolean first = true ;
            for ( Iterator iter = project.getVars().iterator() ; iter.hasNext() ; )
            {
                if ( ! first )
                  out.print(" ") ;
                first = false ;
                Var v = (Var)iter.next() ;
                Expr expr = project.getExpr(v) ;
                if ( expr != null )
                {
                    out.print("(") ;
                    out.print(v.toString()) ;
                    out.print(" ") ;
                    ExprUtils.fmtPrefix(out, expr, sContext.getPrefixMapping()) ;
                    out.print(")") ;
                }
                else
                    out.print(v.toString()) ;
            }
            out.print(")") ;
        }

        private void formatTriple(Triple tp)
        {
            out.print(Plan.startMarker2) ; 
            out.print("triple") ;
            out.print(" ") ;
            out.print(slotToString(tp.getSubject())) ;
            out.print(" ") ;
            out.print(slotToString(tp.getPredicate())) ;
            out.print(" ") ;
            out.print(slotToString(tp.getObject())) ;
            out.print(Plan.finishMarker2) ; 
        }

        private void formatQuad(Quad qp)
        {
            out.print(Plan.startMarker2) ; 
            out.print("quad") ;
            out.print(" ") ;
            out.print(slotToString(qp.getGraph())) ;
            out.print(" ") ;
            out.print(slotToString(qp.getSubject())) ;
            out.print(" ") ;
            out.print(slotToString(qp.getPredicate())) ;
            out.print(" ") ;
            out.print(slotToString(qp.getObject())) ;
            out.print(Plan.finishMarker2) ;
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