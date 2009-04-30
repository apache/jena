/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq;

import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdARQ_SSE;

import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemWriter;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

public class sse extends CmdARQ_SSE
{
    protected final ArgDecl numberDecl      = new ArgDecl(ArgDecl.HasValue, "num", "number") ;
    protected final ArgDecl noPrintDecl     = new ArgDecl(ArgDecl.NoValue, "n") ;
    protected final ArgDecl noResolveDecl   = new ArgDecl(ArgDecl.NoValue, "raw") ;

    private boolean         print       = true ;
    private boolean         structural  = true ;
    private boolean         lineNumbers = false ;

    public static void main (String... argv)
    {
        new sse(argv).mainRun() ;
    }

    public sse(String[] argv)
    {
        super(argv) ;
        super.add(noPrintDecl,      "-n",               "Don't print the expression") ;
        super.add(numberDecl,       "--num [on|off]",   "Numbers") ;
        super.add(noResolveDecl,    "--raw", "Don't handle base or prefix names specially") ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        print = !contains(noPrintDecl) ;
        if ( contains(numberDecl) )
            lineNumbers = getValue(numberDecl).equalsIgnoreCase("on") ;
        
        if ( contains(noResolveDecl) )
            SSE.setUseResolver(false) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }

    @Override
    protected String getSummary() { return getCommandName() ; }

    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    //static final String divider = "" ;

    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }

    @Override
    protected void exec(Item item)
    {
        if ( ! print )
            return ;
        
        if ( item == null )
        {
            System.err.println("No expression") ;
            throw new TerminationException(9) ;
        }
        divider() ;
        IndentedWriter out = new IndentedWriter(System.out, lineNumbers) ;
        
        // Need to check if used.
        //PrefixMapping pmap = SSE.getDefaultPrefixMapWrite() ;
        PrefixMapping pmap = null ;
        SerializationContext sCxt = new SerializationContext(pmap) ;
        ItemWriter.write(out, item, sCxt) ;
        //item.output(out) ;
        out.ensureStartOfLine() ;
        out.flush();
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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