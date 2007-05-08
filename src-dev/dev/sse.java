/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.IOException;
import java.util.Iterator;

import arq.cmd.CmdException;
import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdARQ;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.SSEParseException;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileUtils;

public class sse extends CmdARQ
{
    protected final ArgDecl queryFileDecl   = new ArgDecl(ArgDecl.HasValue, "query", "file") ;
    protected final ArgDecl queryNumberDecl = new ArgDecl(ArgDecl.HasValue, "num", "number") ;
    protected final ArgDecl noPrintDecl     = new ArgDecl(ArgDecl.NoValue, "n") ;

    String queryFilename = null ;
    boolean print        = true ;
    private boolean lineNumbers = true ;

    public static void main (String [] argv)
    {
        new sse(argv).main() ;
    }

    public sse(String[] argv)
    {
        super(argv) ;
        super.add(queryFileDecl, "--query=FILE", "Algebra file to execute") ;
        super.add(noPrintDecl, "-n",  "Don't print the expression") ;
        super.add(queryNumberDecl,
                    "--num [on|off]",
                    "Numbers") ;
    }

    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( contains(queryFileDecl) )
            queryFilename = super.getValue(queryFileDecl) ;

        print = contains(noPrintDecl) ;
        if ( contains(queryNumberDecl) )
            lineNumbers = getValue(queryNumberDecl).equalsIgnoreCase("on") ;
    }

    protected String getCommandName() { return Utils.className(this) ; }

    protected String getSummary() { return getCommandName() ; }

    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    //static final String divider = "" ;

    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }

    protected void exec()
    {
        if ( queryFilename != null )
            exec1(queryFilename) ;
        
        for ( Iterator iter = super.getPositional().listIterator() ; iter.hasNext();)
        {
            String fn = (String)iter.next() ;
            exec1(fn) ;
        }
    }
    
    protected void exec1(String filename)
    {
        try {

            Item item = null ;
            if ( filename.equals("-") )
            {
                try {
                    String str = FileUtils.readWholeFileAsUTF8(System.in) ;
                    item = SSE.parse(str) ;
                } catch (IOException ex)
                { throw new CmdException("Error reading stdin", ex) ; }
            }
            else
                item = SSE.readFile(filename) ;

            if ( item == null )
            {
                System.err.println("No expression from "+filename) ;
                throw new TerminationException(9) ;
            }
            divider() ;
            IndentedWriter out = new IndentedWriter(System.out, lineNumbers) ;
            item.output(out) ;
            out.ensureStartOfLine() ;
            out.flush();
        }
        catch (SSEParseException sseEx)
        {
            System.err.println(sseEx.getMessage()) ;
            throw new TerminationException(99) ;
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