/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.util.Iterator;
import java.util.List;

import arq.cmd.CmdException;

import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef;


public class ModEngine extends ModBase
{
    // Special case of a "ModEnvironment"
    // Alters the ARQ environment but provides nothing at execution time.
    // Combine with ModSymbol?
    
    protected final ArgDecl engineDecl = new ArgDecl(ArgDecl.HasValue, "engine") ;
    protected final ArgDecl unEngineDecl = new ArgDecl(ArgDecl.HasValue,
                                                       "unengine",
                                                       "unEngine",
                                                       "removeEngine",
                                                       "removeengine"
                                                       ) ;
    
    private boolean timing = false ;
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Query Engine") ;
        cmdLine.add(engineDecl, "--engine=EngineName", "Register another engine factory[ref]") ; 
        cmdLine.add(unEngineDecl, "--unengine=EngineName", "Unregister an engine factory") ;
    }
    
    public void checkCommandLine(CmdGeneral cmdLine)
    {}

    public void processArgs(CmdArgModule cmdLine)
    {
       
        List<String> x = cmdLine.getValues(engineDecl) ;

        for ( Iterator<String> iter = x.iterator() ; iter.hasNext() ; )
        {
            String engineName = iter.next() ;
            if ( engineName.equalsIgnoreCase("ref") ||
                 engineName.equalsIgnoreCase("reference") )
            {
                QueryEngineRef.register() ;
                continue ;
            }
            
            if ( engineName.equalsIgnoreCase("main") )
            {
                QueryEngineMain.register() ;
                continue ;
            }
            throw new CmdException("Engine name not recognized: "+engineName) ;
        }

        List<String> y = cmdLine.getValues(unEngineDecl) ;
        for (String engineName : y)
        {
            if ( engineName.equalsIgnoreCase("ref") ||
                 engineName.equalsIgnoreCase("reference") )
            {
                QueryEngineRef.unregister() ;
                continue ;
            }
            if ( engineName.equalsIgnoreCase("main") )
            {
                QueryEngineMain.unregister() ;
                continue ;
            }
            throw new CmdException("Engine name not recognized: "+engineName) ;
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