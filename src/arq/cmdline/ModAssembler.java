/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import arq.cmd.CmdException;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.NotFoundException;

import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;


public class ModAssembler extends ModBase
{
    protected final ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset") ;
    private String assemblerFile = null ;
    Object thingDescribed = null ;
    
    public ModAssembler()
    { 
        // Wire in assmebler implementations
        AssemblerUtils.init() ;
    }
    
    // Dataset : default graph and named graphs

    //@Override
    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(assemblerDescDecl) )
            assemblerFile = cmdLine.getValue(assemblerDescDecl) ;
    }
    
    public void registerWith(CmdGeneral cmdLine)
    {
        //cmdLine.getUsage().startCategory("Dataset") ;
        cmdLine.add(assemblerDescDecl,
                    "--desc=",
                    "Assembler description file") ;
    }
    
    public String getAssemblerFile() { return assemblerFile ; }
    
    // Should subclass and apply typing.
    
    public Object create(Resource type)
    {
        Object thing = null ;
        try {
            thing = AssemblerUtils.build(assemblerFile, type) ;
        }
        catch (ARQException ex) { throw ex; }
        catch (NotFoundException ex)
        { throw new CmdException("Not found: "+ex.getMessage()) ; }
        catch (JenaException ex)
        { throw ex ; }
        catch (Exception ex)
        { throw new CmdException("Error creating", ex) ; }
        
        return thing ;
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