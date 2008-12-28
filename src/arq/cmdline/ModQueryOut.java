/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;


import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;

public class ModQueryOut implements ArgModuleGeneral
{
    protected final ArgDecl queryOutputSyntaxDecl  = new ArgDecl(ArgDecl.HasValue, "out", "format") ;
    protected final ArgDecl queryNumberDecl        = new ArgDecl(ArgDecl.NoValue, "num", "number") ;

    private Syntax outputSyntax = Syntax.syntaxSPARQL ;
    private boolean lineNumbers = false ;
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Output") ;
        cmdLine.add(queryOutputSyntaxDecl, "--out, --format",  "Output syntax") ;
        cmdLine.add(queryNumberDecl, "--num", "Print line numbers") ;
    }

    public void processArgs(CmdArgModule cmdline) throws IllegalArgumentException
    {
        if ( cmdline.contains(queryOutputSyntaxDecl) )
        {
            // short name
            String s = cmdline.getValue(queryOutputSyntaxDecl) ;
            Syntax syn = Syntax.lookup(s) ;
            if ( syn == null )
                cmdline.cmdError("Unrecognized syntax: "+s) ;
            outputSyntax = syn ; 
        }        
        
        lineNumbers = cmdline.contains(queryNumberDecl) ;
    }
    
    public Syntax getOutputSyntax()
    {
        return outputSyntax ;
    }

    public void output(Query query)
    { output(out(), query) ; }
    
    public void output(IndentedWriter out, Query query)
    { PrintUtils.printQuery(out, query, outputSyntax) ; }
    
    public void outputOp(Query query, boolean printOptimized)
    { outputOp(out(), query, printOptimized) ; }

    public void outputOp(IndentedWriter out, Query query, boolean printOptimized)
    { PrintUtils.printOp(out, query, printOptimized) ; }
    
    public void outputQuad(Query query, boolean printOptimized)
    { outputQuad(out(), query, printOptimized) ; }
    
    public void outputQuad(IndentedWriter out, Query query, boolean printOptimized)
    { PrintUtils.printQuad(out, query, printOptimized) ; }
    
    private IndentedWriter out()
    {
        return new IndentedWriter(System.out, lineNumbers) ;
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