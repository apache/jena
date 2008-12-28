/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;

public class ModItem implements ArgModuleGeneral
{
    protected final ArgDecl queryFileDecl = new ArgDecl(ArgDecl.HasValue, "file") ;

    private String filename = null ;
    private String parseString = null ; 
    private Item item = null ;
    
    public ModItem() {}
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Item") ;
        cmdLine.add(queryFileDecl, "--file=", "File") ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(queryFileDecl) )
        {
            filename = cmdLine.getValue(queryFileDecl) ;
            parseString = FileManager.get().readWholeFileAsUTF8(filename) ;
            return ;
        }
    
        if ( cmdLine.getNumPositional() == 0 && filename == null )
            cmdLine.cmdError("No query string or query file") ;

        if ( cmdLine.getNumPositional() > 1 )
            cmdLine.cmdError("Only one query string allowed") ;
    
        if ( cmdLine.getNumPositional() == 1 && filename != null )
            cmdLine.cmdError("Either query string or query file - not both") ;

        if ( filename == null )
        {
            String qs = cmdLine.getPositionalArg(0) ;
            parseString = cmdLine.indirect(qs) ;
        }
    }
    
    public Item getItem()
    {
        if ( item != null )
            return item ;
        // Need to get the (outer) prologue.
        item = SSE.parseItem(parseString) ;
        return item ;
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