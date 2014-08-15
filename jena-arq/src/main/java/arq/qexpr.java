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

package arq;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.riot.out.NodeFmtLib ;
import arq.cmd.CmdException ;
import arq.cmd.TerminationException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdLineArgs ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.ExprLib ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.WriterSSE ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

/** A program to execute expressions from the command line. */

public class qexpr
{
    // TODO Convert to extends CmdArgModule 
    static { LogCtl.setCmdLogging(); }

    public static void main (String... argv)
    {
        try {
            main2(argv) ;
        }
        catch (TerminationException ex) { System.exit(ex.getCode()) ; }
        catch (CmdException ex)
        {
            System.err.println(ex.getMessage()) ;
            if ( ex.getCause() != null )
                ex.getCause().printStackTrace(System.err) ;
        }
        
    }
    
    public static void execAndReturn(String... argv)
    {
        try {
            main2(argv) ;
        }
        catch (TerminationException ex) { return ; }
        catch (CmdException ex)
        {
            System.err.println(ex.getMessage()) ;
            if ( ex.getCause() != null )
                ex.getCause().printStackTrace(System.err) ;
        }
    }
        
    public static void main2(String... argv)
    {
        
        CmdLineArgs cl = new CmdLineArgs(argv) ;
        
        ArgDecl helpDecl = new ArgDecl(ArgDecl.NoValue, "h", "help") ;
        cl.add(helpDecl) ;
        
        ArgDecl verboseDecl = new ArgDecl(ArgDecl.NoValue, "v", "verbose") ;
        cl.add(verboseDecl) ;
        
        ArgDecl versionDecl = new ArgDecl(ArgDecl.NoValue, "ver", "version", "V") ;
        cl.add(versionDecl) ;
        
        ArgDecl quietDecl = new ArgDecl(ArgDecl.NoValue, "q", "quiet") ;
        cl.add(quietDecl) ;

        ArgDecl reduceDecl =  new ArgDecl(ArgDecl.NoValue, "reduce", "fold", "simplify" ) ;
        cl.add(reduceDecl) ;

        ArgDecl strictDecl =  new ArgDecl(ArgDecl.NoValue, "strict") ;
        cl.add(strictDecl) ;
        
        ArgDecl printDecl =  new ArgDecl(ArgDecl.HasValue, "print") ;
        cl.add(printDecl) ;

        try {
            cl.process() ;
        } catch (IllegalArgumentException ex)
        {
            System.err.println(ex.getMessage()) ;
            usage(System.err) ;
            throw new CmdException() ;
        }

        if ( cl.contains(helpDecl) )
        {
            usage() ;
            throw new TerminationException(0) ;
        }
        
        if ( cl.contains(versionDecl) )
        {
            System.out.println("ARQ Version: "+ARQ.VERSION+" (Jena: "+Jena.VERSION+")") ;
            throw new TerminationException(0) ;
        }
 
        // ==== General things
        boolean verbose = cl.contains(verboseDecl) ;
        boolean quiet = cl.contains(quietDecl) ;

        if ( cl.contains(strictDecl) )
            ARQ.setStrictMode() ;
        
        boolean actionCopySubstitute = cl.contains(reduceDecl) ;
        boolean actionPrintPrefix = false ;
        boolean actionPrintSPARQL = false ; 
        boolean actionPrint = cl.contains(printDecl) ;

        for ( String v : cl.getValues( printDecl ) )
        {
            if ( v.equalsIgnoreCase( "prefix" ) || v.equalsIgnoreCase( "op" ) )
            {
                actionPrintPrefix = true;
            }
            else if ( v.equalsIgnoreCase( "expr" ) )
            {
                actionPrintSPARQL = true;
            }
            else
            {
                System.err.println( "Unknown print form: " + v );
                throw new TerminationException( 0 );
            }
        }

        // ==== Do it
        
        for ( int i = 0 ; i < cl.getNumPositional() ; i++ )
        {
            String exprStr = cl.getPositionalArg(i) ;
            exprStr = cl.indirect(exprStr) ;
            
            try {
                PrefixMapping pmap = PrefixMapping.Factory.create()  ;
                pmap.setNsPrefixes(ARQConstants.getGlobalPrefixMap()) ;
                pmap.setNsPrefix("", "http://example/") ;
                pmap.setNsPrefix("ex", "http://example/ns#") ;
//              Node n = asNode() ;
//              return makeNode(n) ;

                Expr expr = ExprUtils.parse(exprStr, pmap) ;
                if ( verbose )
                    System.out.print(expr.toString()+" => ") ;
                
                if ( actionPrint )
                {
                    if ( actionPrintSPARQL )
                        System.out.println(ExprUtils.fmtSPARQL(expr)) ;
                    if ( actionPrintPrefix )
                        WriterSSE.out(IndentedWriter.stdout, expr, new Prologue(pmap)) ;
                    continue ;
                }
                
                try {
                    if ( actionCopySubstitute )
                    {
                        Expr e = ExprLib.foldConstants(expr) ;
                        System.out.println(e) ;
                    }
                    else
                    {
                        // Default action
                        ARQ.getContext().set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
                        FunctionEnv env = new ExecutionContext(ARQ.getContext(), null, null, null) ; 
                        NodeValue r = expr.eval(null, env) ;
                        //System.out.println(r.asQuotedString()) ;
                        Node n = r.asNode() ;
                        String s = NodeFmtLib.displayStr(n) ;
                        System.out.println(s) ;
                    }
                } catch (ExprEvalException ex)
                {
                    System.out.println("Exception: "+ex.getMessage()) ;
                    throw new TerminationException(2) ;
                }
            } catch (QueryParseException ex)
            {
                System.err.println("Parse error: "+ex.getMessage()) ;
                throw new TerminationException(2) ;
            }
        }
    }
    
    static void usage() { usage(System.out) ; }
    
    static void usage(java.io.PrintStream out)
    {
        out.println("Usage: [--print=[prefix|expr]] expression") ;
    }

 }
