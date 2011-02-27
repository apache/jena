/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.writers;

import java.io.OutputStream ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpPrefixesUsed ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.algebra.table.TableUnit ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.QuadPattern ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.ExprAggregator ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

// ToDo extract write of:
// Table
// Triple and quads

public class WriterOp
{
    private static final int NL = WriterLib.NL ;
    private static final int NoNL = WriterLib.NoNL ;    // No newline, with space
    private static final int NoSP = WriterLib.NoSP ;
    
    public static void output(Op op)
    { output(System.out, op) ; }
    
    public static void output(Op op, PrefixMapping pMap)
    { output(System.out, op, pMap) ; }
    
    public static void output(Op op, Prologue prologue)
    { output(System.out, op, prologue) ; }
    
    public static void output(OutputStream out, Op op)
    { output(out, op, (PrefixMapping)null) ; }

    public static void output(OutputStream out, Op op, PrefixMapping pMap)
    { output(new IndentedWriter(out), op, pMap) ; }

    public static void output(OutputStream out, Op op, Prologue prologue)
    { output(new IndentedWriter(out), op, prologue) ; }

    public static void output(IndentedWriter iWriter, Op op)
    { output(iWriter, op, (PrefixMapping)null) ; }

    public static void output(IndentedWriter iWriter, Op op, PrefixMapping pMap)
    {
        if ( pMap == null )
            pMap = OpPrefixesUsed.used(op, ARQConstants.getGlobalPrefixMap()) ;
        SerializationContext sCxt = new SerializationContext(pMap) ;
        output(iWriter, op, sCxt) ;
    }

    public static void output(IndentedWriter iWriter, Op op, Prologue prologue)
    {
        SerializationContext sCxt = new SerializationContext(prologue) ;
        output(iWriter, op, sCxt) ;
    }
    
    public static void out(OutputStream out, Op op, SerializationContext sCxt)
    {
        output(new IndentedWriter(out), op, sCxt) ;
    }

    // Actual work
    public static void output(final IndentedWriter iWriter, final Op op, SerializationContext sCxt)
    {
        if ( sCxt == null )
            sCxt = new SerializationContext() ;
        final SerializationContext sCxt2 = sCxt ;
        WriterBasePrefix.Fmt fmt = 
            new WriterBasePrefix.Fmt() {
                public void format() {op.visit(new OpWriterWorker(iWriter, sCxt2)) ;}
                } ;
        WriterBasePrefix.output(iWriter, fmt, sCxt2.getPrologue()) ;
    }        
    
    // Without the base/prefix wrapper. 
    static void outputNoPrologue(final IndentedWriter iWriter, final Op op, final SerializationContext sCxt)
    {
        OpWriterWorker v = new OpWriterWorker(iWriter, sCxt) ;
        op.visit(v) ;
    }        
    
    
    public static class OpWriterWorker implements OpVisitor
    {
        private IndentedWriter out ;
        private SerializationContext sContext ;
        
        public OpWriterWorker(IndentedWriter out, SerializationContext sCxt)
        { 
            this.sContext = sCxt ;
            this.out = out ;
        }
        
        private void visitOpN(OpN op)
        {
            start(op, NL) ;
            for ( Iterator<Op> iter = op.iterator() ; iter.hasNext() ; )
            {
                Op sub = iter.next() ;
                out.ensureStartOfLine() ;
                printOp(sub) ;
            }
            finish(op) ;
        }
        
        private void visitOp2(Op2 op, ExprList exprs)
        {
            start(op, NL) ;
            printOp(op.getLeft()) ;

            out.ensureStartOfLine() ;

            printOp(op.getRight()) ;
            if ( exprs != null )
            { 
                out.ensureStartOfLine() ;
                WriterExpr.output(out, exprs, sContext) ;
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
            QuadPattern quads = opQuadP.getPattern() ;
            if ( quads.size() == 1 )
            {
                start(opQuadP, NoNL) ;
                formatQuad(quads.get(0)) ;
                finish(opQuadP) ;
                return ;
            }
            start(opQuadP, NL) ;
            for ( Quad quad : quads )
            {
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
                write(opBGP.getPattern(), true) ;
                finish(opBGP) ;
                return ;
            }
            
            start(opBGP, NL) ;
            write(opBGP.getPattern(), false) ;
            finish(opBGP) ;
        }
        
        private void write(BasicPattern pattern, boolean oneLine)
        {
            boolean first = true ;
            for ( Triple t : pattern )
            {
               formatTriple(t) ;
               if ( oneLine )
               {
                   if ( ! first ) out.print(" ") ;
               }
               else
                   out.println() ;
               first = false ;
            }
        }
        
        public void visit(OpTriple opTriple)
        {
            formatTriple(opTriple.getTriple()) ;
        }

        public void visit(OpPath opPath)
        {
            //start(opPath, NoNL) ;
            formatTriplePath(opPath.getTriplePath()) ;
            //finish(opPath) ;
        }

        public void visit(OpProcedure opProc)
        {
            start(opProc, NoNL) ;
            WriterNode.output(out, opProc.getProcId(), sContext) ;
            out.println();
            WriterExpr.output(out, opProc.getArgs(), true, false, sContext) ;
            out.println() ;
            printOp(opProc.getSubOp()) ;
            finish(opProc) ;
        }
        
        public void visit(OpPropFunc opPropFunc)
        {
            start(opPropFunc, NoNL) ;
            out.print(FmtUtils.stringForNode(opPropFunc.getProperty(), sContext)) ;
            out.println();

            outputPF(opPropFunc.getSubjectArgs()) ;
            out.print(" ") ;
            outputPF(opPropFunc.getObjectArgs()) ;
            out.println() ;
            printOp(opPropFunc.getSubOp()) ;
            finish(opPropFunc) ;
        }
        
        private void outputPF(PropFuncArg pfArg)
        {
            if ( pfArg.isNode() )
            {
                WriterNode.output(out, pfArg.getArg(), sContext) ;
                return ;
            }
            WriterNode.output(out, pfArg.getArgList(), sContext) ;
        }
        
        public void visit(OpJoin opJoin)
        { visitOp2(opJoin, null) ; }

        public void visit(OpSequence opSequence)
        { visitOpN(opSequence) ; }

        public void visit(OpDisjunction opDisjunction)
        { visitOpN(opDisjunction) ; }
        
        public void visit(OpLeftJoin opLeftJoin)
        { visitOp2(opLeftJoin, opLeftJoin.getExprs()) ; }
        
        public void visit(OpDiff opDiff)
        { visitOp2(opDiff, null) ; }
    
        public void visit(OpMinus opMinus)
        { visitOp2(opMinus, null) ; }

        public void visit(OpUnion opUnion)
        { visitOp2(opUnion, null) ; } 
    
        public void visit(OpConditional opCondition)
        { visitOp2(opCondition, null) ; }

        public void visit(OpFilter opFilter)
        { 
            start(opFilter, NoNL) ;
//            int x = out.getCurrentOffset() ;
//            out.incIndent(x) ;
            
            ExprList exprs = opFilter.getExprs() ;
            if ( exprs == null )
            { start() ; finish() ; }
            else
                WriterExpr.output(out, exprs, sContext) ;
            out.println();
            printOp(opFilter.getSubOp()) ;
            
//            out.decIndent(x) ;
            
            finish(opFilter) ;
        }
    
        public void visit(OpGraph opGraph)
        {
            start(opGraph, NoNL) ;
            out.println(FmtUtils.stringForNode(opGraph.getNode(), sContext)) ;
            opGraph.getSubOp().visit(this) ;
            finish(opGraph) ;
        }

        public void visit(OpService opService)
        {
            start(opService, NoNL) ;
            if ( opService.getSilent() )
                out.println("silent ") ;
            out.println(FmtUtils.stringForNode(opService.getService(), sContext)) ;
            opService.getSubOp().visit(this) ;
            finish(opService) ;
        }

        public void visit(OpTable opTable)
        {
            if ( TableUnit.isJoinUnit(opTable.getTable()) )
            {
                start(opTable, NoNL) ;
                out.print("unit") ;
                finish(opTable) ;
                return ;
            }
            
            start(opTable, NL) ;
            WriterTable.outputPlain(out, opTable.getTable(), sContext);
            finish(opTable) ;
        }

        public void visit(OpDatasetNames dsNames)
        {
            start(dsNames, NoNL) ;
            WriterNode.output(out, dsNames.getGraphNode(), sContext) ;
            finish(dsNames) ;
        }

        public void visit(OpExt opExt)
        {
            //start(opExt, NL) ;
            opExt.output(out, sContext) ;
            //finish(opExt) ;
        }

        public void visit(OpNull opNull)
        { start(opNull, NoSP) ; finish(opNull) ; } 
        
        public void visit(OpLabel opLabel)
        { 
            String x = FmtUtils.stringForString(opLabel.getObject().toString()) ;
            if ( opLabel.hasSubOp() )
            {
                start(opLabel, NL) ;
                out.println(x) ;
                printOp(opLabel.getSubOp()) ;
                finish(opLabel) ;
            }
            else
            {
                start(opLabel, NoNL) ;
                out.print(x) ;
                finish(opLabel) ;
            }
        }

        public void visit(OpList opList)
        {
            visitOp1(opList) ;
        }
        
        public void visit(OpGroup opGroup)
        {
            start(opGroup, NoNL) ;
            writeNamedExprList(opGroup.getGroupVars()) ;
            if ( ! opGroup.getAggregators().isEmpty() )
            {
                // --- Aggregators
                out.print(" ") ;
                start() ;
                out.incIndent() ;
                boolean first = true ;
                for ( Iterator<ExprAggregator> iter = opGroup.getAggregators().iterator() ; iter.hasNext() ; )
                {
                    ExprAggregator agg = iter.next();
                    if ( ! first )
                        out.print(" ") ;
                    first = false ;
                    Var v = agg.getVar() ;
                    String str = agg.getAggregator().toPrefixString() ;
                    start() ;
                    out.print(v) ;
                    out.print(" ") ;
                    out.print(str) ;
                    finish() ;
                }
                finish() ;
                out.decIndent() ;
            }
            out.println() ;
            printOp(opGroup.getSubOp()) ;
            finish(opGroup) ;
        }
        
        public void visit(OpOrder opOrder)
        { 
            start(opOrder, NoNL) ;
            
            // Write conditions
            start() ;

            boolean first = true ;
            for ( SortCondition sc : opOrder.getConditions() )
            {
                if ( ! first )
                    out.print(" ") ;
                first = false ;
                formatSortCondition(sc) ;
            }
            finish() ;
            out.newline();
            printOp(opOrder.getSubOp()) ;
            finish(opOrder) ;
        }
        
        public void visit(OpTopN opTop)
        { 
            start(opTop, NoNL) ;
            
            // Write conditions
            start() ;
            writeIntOrDefault(opTop.getLimit()) ;
            
            boolean first = true ;
            for ( SortCondition sc : opTop.getConditions() )
            {
                if ( ! first )
                    out.print(" ") ;
                first = false ;
                formatSortCondition(sc) ;
            }
            finish() ;
            out.newline();
            printOp(opTop.getSubOp()) ;
            finish(opTop) ;
        }
        
        // Neater would be a pair of explicit SortCondition formatter
        private void formatSortCondition(SortCondition sc)
        {
            boolean close = true ;
            String tag = null ;
            
            if ( sc.getDirection() != Query.ORDER_DEFAULT ) 
            {            
                if ( sc.getDirection() == Query.ORDER_ASCENDING )
                {
                    tag = Tags.tagAsc ; 
                    WriterLib.start(out, tag, NoNL) ;
                }
            
                if ( sc.getDirection() == Query.ORDER_DESCENDING )
                {
                    tag = Tags.tagDesc ; 
                    WriterLib.start(out, tag, NoNL) ;
                }
                
            }
            
            WriterExpr.output(out, sc.getExpression(), sContext) ;
            
            if ( tag != null )
                WriterLib.finish(out, tag) ;
        }



        public void visit(OpProject opProject)
        { 
            start(opProject, NoNL) ;
            writeVarList(opProject.getVars()) ;
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
        
        public void visit(OpAssign opAssign)
        {
            start(opAssign, NoNL) ;
            writeNamedExprList(opAssign.getVarExprList()) ;
            out.println();
            printOp(opAssign.getSubOp()) ;
            finish(opAssign) ;
        }
        
        public void visit(OpExtend opExtend)
        {
            start(opExtend, NoNL) ;
            writeNamedExprList(opExtend.getVarExprList()) ;
            out.println();
            printOp(opExtend.getSubOp()) ;
            finish(opExtend) ;
        }
        
        public void visit(OpSlice opSlice)
        { 
            start(opSlice, NoNL) ;
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
            
        private void start(Op op, int newline)
        {
            WriterLib.start(out, op.getName(), newline) ;
        }
        
        private void finish(Op op)
        {
            WriterLib.finish(out, op.getName()) ;
        }

        private void start()    { WriterLib.start(out) ; }
        private void finish()   { WriterLib.finish(out) ; }

        
        private void printOp(Op op)
        {
            if ( op == null )
            {
                WriterLib.start(out, Tags.tagNull, NoSP) ;
                WriterLib.finish(out, Tags.tagNull) ;
            }
            else
                op.visit(this) ;
        }
        
        private void writeVarList(List<Var> vars)
        {
            start() ;
            boolean first = true ;
            for (Var var : vars)
            {
                if ( ! first )
                  out.print(" ") ;
                first = false ;
                out.print(var.toString()) ;
            }
            finish() ;
        }
        
        private void writeNamedExprList(VarExprList project)
        {
            start() ;
            boolean first = true ;
            for ( Var v : project.getVars() )
            {
                if ( ! first )
                  out.print(" ") ;
                first = false ;
                Expr expr = project.getExpr(v) ;
                if ( expr != null )
                {
                    start() ;
                    out.print(v.toString()) ;
                    out.print(" ") ;
                    WriterExpr.output(out, expr, sContext) ;
                    finish() ;
                }
                else
                    out.print(v.toString()) ;
            }
            finish() ;
        }

        private void formatTriple(Triple tp)
        { WriterNode.output(out, tp, sContext) ; }
        
        private void formatQuad(Quad qp)
        { WriterNode.output(out, qp, sContext) ; }
        
        private void formatTriplePath(TriplePath tp)
        { WriterPath.output(out, tp, sContext) ; }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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