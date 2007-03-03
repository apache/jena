/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.cmd;

import com.hp.hpl.jena.sdb.store.Store;

import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;
import arq.cmdline.CmdGeneral;
import arq.cmdline.ModBase;

public class ModFormat extends ModBase
{
    protected final ArgDecl argDeclFormat = new ArgDecl(ArgDecl.NoValue, "format") ;
    protected final ArgDecl argDeclCreate = new ArgDecl(ArgDecl.NoValue, "create") ;
    protected final ArgDecl argDeclDropIndexes = new ArgDecl(ArgDecl.NoValue, "drop") ;
    protected final ArgDecl argDeclIndexes = new ArgDecl(ArgDecl.NoValue, "build", "indexes", "index") ;
    
    private boolean format = false ;
    private boolean create = false ;
    private boolean dropIndexes = false ;
    private boolean buildIndexes = false ;
    
    public ModFormat() {}

    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.add(argDeclCreate,
                    "--create", "Format a database - destroys any existing data") ;
        cmdLine.add(argDeclDropIndexes,
                    "--drop", "Drop the secondary indexes (primary indexes can't be dropped)") ;
        cmdLine.add(argDeclIndexes,
                    "--indexes", "Build the secondary indexes") ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        format = cmdLine.contains(argDeclFormat) ;
        create = cmdLine.contains(argDeclCreate) ;
        dropIndexes = cmdLine.contains(argDeclDropIndexes) ;
        buildIndexes = cmdLine.contains(argDeclIndexes) ;
    }

    public boolean buildIndexes()     { return buildIndexes ; }

    public boolean dropIndexes()      { return dropIndexes ; }

    public boolean format()           { return format ; }
    public boolean create()           { return create ; }

    public void enact(Store store)
    {
        // "create" = format + build indexes.
        if ( create() )
            store.getTableFormatter().create() ;
        if ( format() && ! create() ) 
            store.getTableFormatter().format() ;
        if ( dropIndexes() ) 
            store.getTableFormatter().dropSecondaryIndexes() ;
        if ( buildIndexes() ) 
            store.getTableFormatter().buildSecondaryIndexes() ;
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