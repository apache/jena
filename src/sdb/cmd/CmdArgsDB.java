/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.cmd;

import arq.cmdline.CmdArgModule;
import arq.cmdline.ModTime;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.shared.Command;

public abstract class CmdArgsDB extends CmdArgModule
{
    
    
    static {
        //  Tune N3 output for result set output.
        System.setProperty("usePropertySymbols",   "false") ;
        System.setProperty("objectLists" ,         "false") ;
        System.setProperty("minGap",               "2") ;
        System.setProperty("propertyColumn",       "14") ;
    }
    
    ModStore modStore = new ModStore() ;
    ModTime  modTime  = new ModTime() ;

    protected CmdArgsDB(String argv[])
    {
        super(argv) ;
        addModule(modStore) ;
        addModule(modTime) ;
    }
    
    protected void setModStore(ModStore modStore) { this.modStore = modStore ; }
    
    protected ModStore getModStore() { return modStore  ; }
    protected ModTime getModTime()   { return modTime  ; }
    
    protected abstract void exec0() ;
    // true means continue transaction
    protected abstract boolean exec1(String arg) ;

    @Override
    protected void exec()
    {
        SDB.init() ;
        checkCommandLine() ;
        
        if ( getNumPositional() > 0 )
            execWithArgs() ;
        else
            execZero() ;
    }
    
    protected void execZero()
    {
        exec0() ;
        modStore.closedown() ;
    }

    private int index ;
    protected void execWithArgs()
    {
        index = 0 ;
        
        while ( index < getNumPositional() )
        {
            modStore.getStore().getConnection().executeInTransaction(new Command(){
                public Object execute()
                {
                    for ( ; index < getNumPositional() ; )
                    {
                        // Execute until end or false.
                        boolean rc = exec1(getPositionalArg(index)) ;
                        index++ ;
                        if ( ! rc )
                            break ;
                    }
                    return null ;
                }}) ;
        }
        modStore.closedown() ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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