/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */


package arq;

import java.io.PrintStream ;
import java.util.Iterator ;

import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModEngine ;
import arq.cmdline.ModQueryIn ;
import arq.cmdline.ModQueryOut ;

import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.QueryCheckException ;
import com.hp.hpl.jena.sparql.resultset.ResultSetException ;
import com.hp.hpl.jena.sparql.util.QueryOutputUtils ;
import com.hp.hpl.jena.sparql.util.QueryUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** A program to parse and print a query. */

public class qparse extends CmdARQ
{
    protected ModQueryIn    modQuery        = new ModQueryIn() ;
    protected ModQueryOut   modOutput       = new ModQueryOut() ; 
    protected ModEngine     modEngine       = new ModEngine() ;
    protected final ArgDecl argDeclPrint    = new ArgDecl(ArgDecl.HasValue, "print") ;
    protected final ArgDecl argDeclOpt      = new ArgDecl(ArgDecl.NoValue, "opt", "optimize") ;
    protected final ArgDecl argDeclExplain  = new ArgDecl(ArgDecl.NoValue, "explain") ;
    
    protected boolean printQuery            = false ;
    protected boolean printOp               = false ;
    protected boolean printOpt              = false ;
    protected boolean printQuad             = false ;
    protected boolean printQuadOpt          = false ;
    protected boolean printPlan             = false ;
    
    public static void main(String... argv)
    {
        new qparse(argv).mainRun() ;
    }
    
    public qparse(String[] argv)
    {
        super(argv) ;
        super.addModule(modQuery) ;
        super.addModule(modOutput) ;
        super.addModule(modEngine) ;
        super.getUsage().startCategory(null) ;
        super.add(argDeclPrint, "--print", "Print in various forms [query, op, quad, plan]") ;
        super.add(argDeclExplain, "--explain", "Print with algebra-level optimization") ;
        super.add(argDeclOpt, "--opt", "[deprecated]") ; 
    }
    
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        
        if ( contains(argDeclOpt) )
            printOpt = true ;
        if ( contains(argDeclExplain) )
        {
            printQuery = true ;
            printOpt = true ;
        }

        for ( Iterator<String> iter = getValues(argDeclPrint).iterator() ; iter.hasNext() ; )
        {
            String arg = iter.next() ;
            if ( arg.equalsIgnoreCase("query"))         { printQuery = true ; }
            else if ( arg.equalsIgnoreCase("op") ||
                      arg.equalsIgnoreCase("alg") || 
                      arg.equalsIgnoreCase("algebra") ) { printOp = true ; }
            else if ( arg.equalsIgnoreCase("quad"))     { printQuad = true ; }
            else if ( arg.equalsIgnoreCase("quads"))    { printQuad = true ; }
            else if ( arg.equalsIgnoreCase("plan"))     { printPlan = true ; }
            else if ( arg.equalsIgnoreCase("opt"))      { printOpt = true ; }
            else if ( arg.equalsIgnoreCase("optquad"))  { printQuadOpt = true ; }
            else if ( arg.equalsIgnoreCase("quadopt"))  { printQuadOpt = true ; }
            else
                throw new CmdException("Not a recognized print form: "+arg+" : Choices are: query, op, quad, opt, optquad") ;
        }
        
        if ( ! printQuery && ! printOp && ! printQuad && ! printPlan && ! printOpt && ! printQuadOpt )
            printQuery = true ;
    }

    static String usage = qparse.class.getName()+" [--in syntax] [--out syntax] [--print=FORM] [\"query\"] | --query <file>" ;
    
    @Override
    protected String getSummary()
    {
        return usage ;
    }
    
    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    //static final String divider = "" ;
    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    @Override
    protected void exec()
    {
        try{
            Query query = modQuery.getQuery() ;
            try {
                QueryUtils.checkQuery(query, true) ;
            } catch (QueryCheckException ex)
            {
                System.err.println() ;
                System.err.println("**** Check failure: "+ex.getMessage()) ;
                if ( ex.getCause() != null )
                    ex.getCause().printStackTrace(System.err) ;
            }
            
            // Print the query out in some syntax
            if ( printQuery )
            { divider() ; modOutput.output(query) ; }

            // Print internal forms.
            if ( printOp )
            { divider() ; modOutput.outputOp(query, false) ; }
            
            if ( printQuad )
            { divider() ; modOutput.outputQuad(query, false) ; }
            
            if ( printOpt )
            { divider() ; modOutput.outputOp(query, true) ; }
            
            if ( printQuadOpt )
            { divider() ; modOutput.outputQuad(query, true) ; }
            
            if ( printPlan )
            { 
                divider() ;
                // This forces internal query initialization - must be after QueryUtils.checkQuery
                QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create()) ;
                QueryOutputUtils.printPlan(query, qExec) ; 
            }
        }
        catch (ARQInternalErrorException intEx)
        {
            System.err.println(intEx.getMessage()) ;
            if ( intEx.getCause() != null )
            {
                System.err.println("Cause:") ;
                intEx.getCause().printStackTrace(System.err) ;
                System.err.println() ;
            }
            intEx.printStackTrace(System.err) ;
        }
        catch (ResultSetException ex)
        {
            System.err.println(ex.getMessage()) ;
            ex.printStackTrace(System.err) ;
        }
        catch (QueryException qEx)
        {
            //System.err.println(qEx.getMessage()) ;
            throw new CmdException("Query Exeception", qEx) ;
        }
        catch (JenaException ex) { 
            ex.printStackTrace() ;
            throw ex ; } 
        catch (CmdException ex) { throw ex ; } 
        catch (Exception ex)
        {
            throw new CmdException("Exception", ex) ;
        }
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }

//    static String usage = qparse.class.getName()+
//            " [--in syntax] [--out syntax] [\"query\" | --query <file>\n"+
//            "  where syntax is one of ARQ, SPARQL\n" +
//            "Other options: \n"+
//            "  --num on|off   Line number ('on' by default)\n"+
//            "  -n             Same as --num=off\n"+
//            "  --base URI     Set the base URI for resolving relative URIs\n"+
//            "  --plain        No pretty printing\n"+
//            "  --parse        Parse only - don't print\n"+
//            "  ---planning    Turn planning on/off\n"+
//            "  --show X       Show internal structure (X = query or plan)\n" ;
    
    static void writeSyntaxes(String msg, PrintStream out)
    {
        if ( msg != null )
            out.println(msg) ;
        for ( Iterator<String> iter = Syntax.querySyntaxNames.keys() ; iter.hasNext() ; )
        {
            String k = iter.next() ;
            Syntax v = Syntax.lookup(k) ;
            k = padOut(k,10) ;
            out.println("  "+k+"  "+v) ;
        }
    }
    // printf ... java 1.5 .. mutter,mutter
    static String padOut(String x, int len)
    {
        StringBuffer r = new StringBuffer(x) ;
        for ( int i = x.length() ; i <= len ; i++ )
            r.append(" ") ;
        return r.toString() ; 
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
