/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import arq.sparql;
import arq.sse_query;

import com.hp.hpl.jena.rdf.model.Model;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.path.Path;
import com.hp.hpl.jena.sparql.path.PathCompiler;
import com.hp.hpl.jena.sparql.path.PathParser;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.hp.hpl.jena.sparql.util.VarUtils;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.update.*;

public class Run
{
    public static void main(String[] argv) throws Exception
    {
        
        code() ;
        //runQParse() ;
        execQuery("D.ttl", "Q.arq") ;
        
        path() ; System.exit(0) ;
        {
            String []a = { "--engine=ref", "--file=Q.rq", "--print=op" } ;
            arq.qparse.main(a) ;
            System.exit(0) ;
        }
        {
            String qs = StringUtils.join("\n",
                                         new String[]
                                                    {
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>",
                "INSERT DATA INTO <http://example/bookStore>",
                                                    "{ <http://example/book3>  dc:title  'Fundamentals of Compiler Design'}"}
            ) ;
            UpdateRequest request2 = UpdateFactory.create(qs);
            request2.output(IndentedWriter.stdout);
            IndentedWriter.stdout.flush();
            System.exit(0) ;
        }
        
        
        UpdateRequest r = UpdateFactory.read("update.ru") ;
        GraphStore gs = GraphStoreFactory.create() ;
        System.out.println(r) ;
        
        UpdateAction.execute(r, gs) ;

        SSE.write(gs) ;
        System.exit(0) ;
        
        
        //code() ; System.exit(0) ;
        runUpdate() ; System.exit(0) ;
        
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data=D.ttl",
            "-query=Q.rq" ,
        } ;

        sparql.main(a) ;
        System.exit(0) ;
    }
    
    private static void path()
    {
//        String[] a = { "--print=op", "--file=Q.sse" } ;
//        arq.sse_query.main(a) ;
//        System.exit(0) ;
        
        Model model = FileManager.get().loadModel("D.ttl") ;

        PrefixMapping pmap = new PrefixMappingImpl() ;
        pmap.setNsPrefixes(PrefixMapping.Standard) ;
        pmap.setNsPrefix("", "http://example/") ;
        
        // | alt.
        
        if ( false )
        {
            path1(":p*/:q", pmap) ;
            path1("^:p", pmap) ;
            path1("^:p/:q", pmap) ;
            path1("^(:p/:q)", pmap) ;
            path1(":p*/:q", pmap) ;
            path1(":p^:q", pmap) ;
        }
        
        if ( false )
        {
            Path path = PathParser.parse("rdf:type/rdfs:subClassOf*", pmap) ;
            //path = new P_Link(RDF.type.asNode()) ;
            
            TriplePath triplePath = new TriplePath(Var.alloc("s"), path, Var.alloc("o")) ; 
            OpPath opPath = new OpPath(triplePath) ;
            System.out.println(opPath.toString(pmap)) ;
            String x = opPath.toString(pmap) ;
            Op op = SSE.parseOp(x, pmap) ;
            System.out.println(op.toString(pmap)) ;
        }
        
        System.exit(0) ;
    }

    private static void path1(String str, PrefixMapping pmap)
    {
        Path path = PathParser.parse(str, pmap) ;
        PathBlock pBlk = new PathBlock() ;
        Node s = Node.createURI("s") ;
        Node o = Node.createURI("o") ;
        TriplePath tp = new TriplePath(s, path, o) ;
        pBlk.add(tp) ;
        
        System.out.println("Path: "+str) ;
        PathBlock x = new PathCompiler().reduce(pBlk) ;
        for ( Iterator iter = x.iterator() ; iter.hasNext() ; )
            System.out.println("  "+iter.next()) ;
        System.out.println() ;
    }
    
    static class TransformFilterPlacement extends TransformCopy
    {
        public TransformFilterPlacement()
        { }
        
        // **** Replacement for FilterPlacement.
        public Op transform(OpFilter opFilter, Op x)
        {
            if ( ! ( x instanceof OpBGP ) )
                return super.transform(opFilter, x) ;
            
            // Also OpSequence and OpJoin - see OpCompile.compile(OpFilter opFilter,...)
            
            BasicPattern pattern = ((OpBGP)x).getPattern() ;
            
            // Destructive use of exprs - copy it.
            ExprList exprs = new ExprList(opFilter.getExprs()) ;
            Set patternVarsScope = new HashSet() ;

            // Accumulate 
            Op op = insertAnyFilter(exprs, patternVarsScope, null) ;
            
            // Place filter
            for ( Iterator iter = pattern.getList().listIterator() ; iter.hasNext() ; )
            {
                Triple triple = (Triple)iter.next();
                OpBGP opBGP = getBGP(op) ;
                if ( opBGP == null )
                {
                    opBGP = new OpBGP() ;    
                    op = OpSequence.create(op, opBGP) ;
                }
                
                opBGP.getPattern().add(triple) ;
                // Update varaibles in scope.
                VarUtils.addVarsFromTriple(patternVarsScope, triple) ;
                
                // Attempt to place any filters
                op = insertAnyFilter(exprs, patternVarsScope, op) ;
            } 
            //remaining expr
            op = buildFilter(exprs, op) ;
            
            // Punt - for now.
            return op ;
            
        }
        
        private OpBGP getBGP(Op op)
        {
            // Find the current OpBGP, or return null.
            if ( op instanceof OpBGP )
                return (OpBGP)op ;
            
            if ( op instanceof OpSequence )
            {
                OpSequence opSeq = (OpSequence)op ;
                List x = opSeq.getElements() ;
                if ( x.size() > 0 )
                {                
                    Op opTop = (Op)x.get(x.size()-1) ;
                    if ( opTop instanceof OpBGP )
                        return (OpBGP)opTop ;
                    // Drop through
                }
            }
            // Can't find.
            return null ;
        }

        private Op buildFilter(ExprList exprs, Op op)
        {
            if ( exprs.isEmpty() )
                return op ;
        
            for ( Iterator iter = exprs.iterator() ; iter.hasNext() ; )
            {
                Expr expr = (Expr)iter.next() ;
                if ( op == null )
                    op = OpTable.unit() ;
                op = OpFilter.filter(expr, op) ;
                iter.remove();
            }
            return op ;
        }
        
        private Op insertAnyFilter(ExprList exprs, Set patternVarsScope, Op op)
        {
            for ( Iterator iter = exprs.iterator() ; iter.hasNext() ; )
            {
                Expr expr = (Expr)iter.next() ;
                // Cache
                Set exprVars = expr.getVarsMentioned() ;
                if ( patternVarsScope.containsAll(exprVars) )
                {
                    if ( op == null )
                        op = OpTable.unit() ;
                    op = OpFilter.filter(expr, op) ;
                    iter.remove() ;
                }
            }
            return op ;
        }
    }

    static class TransformReorderBGP extends TransformCopy
    {
        public Op transform(OpBGP opBGP)
        {
            BasicPattern pattern = opBGP.getPattern() ;
            BasicPattern pattern2 = new BasicPattern() ;
            
            // Choose order.
            for ( Iterator iter = pattern.getList().listIterator() ; iter.hasNext() ; )
            {
                Triple triple = (Triple)iter.next();
                System.out.println("Process: "+triple) ;
                pattern2.add(triple) ;
            }
            return new OpBGP(pattern2) ; 
            
            //return super.transform(opBGP) ;
        }
    }
    
    public static void code()
    {
        Transform t = new TransformFilterPlacement() ;
        
        Op op = SSE.readOp("Q.sse") ;
        System.out.println(op) ;
        op = Transformer.transform(new TransformFilterPlacement(), op) ;
        System.out.println(op) ;
        System.exit(0) ;
        
    }
    private static void runQParse()
    {
        String []a = { "--engine=quad", "--file=Q.arq", "--print=op" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQParseARQ()
    {
        String []a = { "--file=Q.arq", "--out=arq", "--print=op", "--print=query" } ;
        arq.qparse.main(a) ;
        System.exit(0) ;
    }
    
    private static void runUpdate()
    {
        String a[] = {/*"--desc=etc/graphstore.ttl",*/ "--update=update.ru", "--dump"} ;
        arq.update.main(a) ;
        System.exit(0) ;
    }
    
    private static void runQTest()
    {
        String DIR = "/home/afs/W3C/DataAccess/tests/data-r2/expr-equals/" ;
        String []a1 = { "--strict", "--data="+DIR+"data-eq.ttl",
            "--query="+DIR+"query-eq2-2.rq",
            "--result="+DIR+"result-eq2-2.ttl"} ;

        String []a2 = { "--strict", "--data="+DIR+"data-eq.ttl",
            "--query="+DIR+"query-eq2-graph-1.rq",
            "--result="+DIR+"result-eq2-graph-1.ttl"} ;

        arq.qtest.main(a1) ;
        System.exit(0 ) ; 
  
    }

    private static void execQuery(String datafile, String queryfile)
    {
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref", 
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sparql.main(a) ;
        System.exit(0) ;
    }

    private static void execQuery2(String datafile, String queryfile)
    {
        //QueryEngineMain.register() ;
        String a[] = new String[]{
            //"-v",
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sparql.main(a) ;
        a = new String[]{
            //"-v",
            "--engine=ref", 
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        sparql.main(a) ;
        
        System.exit(0) ;
    }
    
    private static void execQuerySSE(String datafile, String queryfile)
    {
        //com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef.register() ;
        String a[] = new String[]{
            //"-v",
            //"--engine=ref",
            "--data="+datafile,
            "-query="+queryfile , 
        } ;
        
        sse_query.main(a) ;
        System.exit(0) ;
        
    }
    
    private static void execQueryCode(String datafile, String queryfile)
    {
        Model model = FileManager.get().loadModel(datafile) ;
        Query query = QueryFactory.read(queryfile) ;
        
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add("s", model.createResource("http://example.org/x2")) ;
        initialBinding.add("o", model.createResource("http://example.org/z")) ;
        
        QueryExecution qExec = QueryExecutionFactory.create(query, model, initialBinding) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        System.exit(0) ;
    }
}



/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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