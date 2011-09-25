/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */


package arq;

import java.util.Iterator ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.logging.Log ;
import arq.cmd.CmdException ;
import arq.cmd.CmdUtils ;
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
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.WriterSSE ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

/** A program to execute expressions from the command line. */

public class qexpr
{
    // TODO Convert to extends CmdArgModule 
    static { Log.setLog4j() ; CmdUtils.setN3Params() ; }

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
            
        for ( Iterator<String> iter = cl.getValues(printDecl).iterator() ; iter.hasNext(); )
        {
            String v = iter.next();
            if ( v.equalsIgnoreCase("prefix") || v.equalsIgnoreCase("op") )
                actionPrintPrefix = true ;
            else if ( v.equalsIgnoreCase("expr") )   actionPrintSPARQL = true ;
            else
            {
                System.err.println("Unknown print form: "+v) ;
                throw new TerminationException(0) ;
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
                        Expr e = expr.copySubstitute(BindingFactory.create(), true) ;
                        System.out.println(e) ;
                    }
                    else
                    {
                        // Default action
                        ARQ.getContext().set(ARQConstants.sysCurrentTime, NodeFactory.nowAsDateTime()) ;
                        FunctionEnv env = new ExecutionContext(ARQ.getContext(), null, null, null) ; 
                        NodeValue r = expr.eval(null, env) ;
                        //System.out.println(r.asQuotedString()) ;
                        Node n = r.asNode() ;
                        String s = FmtUtils.stringForNode(n) ;
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

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
