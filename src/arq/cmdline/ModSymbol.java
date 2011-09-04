/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.io.PrintStream ;
import java.util.Iterator ;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.MappingRegistry ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class ModSymbol implements ArgModuleGeneral
{
    protected final ArgDecl setDecl = new ArgDecl(ArgDecl.HasValue, "set", "define", "defn", "def") ;
    Context context = new Context() ;
    private String namespace ;
    
    public ModSymbol() { this(ARQ.arqSymbolPrefix) ; }
    
    public ModSymbol(String namespace) { this.namespace = namespace ; }
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Symbol definition") ;
        cmdLine.add(setDecl, "--set", "Set a configuration symbol to a value") ;
    }
    
    public void checkCommandLine(CmdArgModule cmdLine)
    {}

    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.getValues(setDecl) == null || cmdLine.getValues(setDecl).size() == 0 )
            return ;
        
        for ( Iterator<String> iter = cmdLine.getValues(setDecl).iterator() ; iter.hasNext(); )
        {
            String arg = iter.next();
            String[] frags = arg.split("=", 2) ;
            if ( frags.length != 2)
                throw new RuntimeException("Can't split '"+arg+"' -- looking for '=' to separate name and value") ;
            
            String symbolName = frags[0] ;
            String value = frags[1] ;

            // Make it a long name.
            symbolName = MappingRegistry.mapPrefixName(symbolName) ;
            Symbol symbol = Symbol.create(symbolName) ;
            context.set(symbol, value) ;
        }
        
        ARQ.getContext().putAll(context) ;
    }
    
    public void verbose() { verbose(System.out) ; }
    
    public void verbose(PrintStream stream)
    {
        IndentedWriter out = new IndentedWriter(stream) ;
        verbose(out) ;
        out.flush();
    }
    
    public void verbose(IndentedWriter out)
    {
        for ( Iterator<Symbol> iter = context.keys().iterator() ; iter.hasNext() ; )
        {
            Symbol symbol = iter.next();
            String value = context.getAsString(symbol) ;
            out.println(symbol+" -> "+value) ;
        }
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