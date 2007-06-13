/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.cmd;

import java.util.List;

import arq.cmdline.CmdGeneral;
import arq.cmdline.ModSymbol;
import arq.cmdline.ModTime;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
import com.hp.hpl.jena.sdb.store.Store;

public abstract class CmdArgsDB extends CmdGeneral
{
    static {
        //  Tune N3 output for result set output.
        System.setProperty("usePropertySymbols",   "false") ;
        System.setProperty("objectLists" ,         "false") ;
        System.setProperty("minGap",               "2") ;
        System.setProperty("propertyColumn",       "14") ;
    }
    
    private ModSymbol modSymbol = new ModSymbol(SDB.symbolNamespace) ;
    private ModStore modStore   = new ModStore() ;
    private ModTime  modTime    = new ModTime() ;

    protected CmdArgsDB(String argv[])
    {
        super(argv) ;
        addModule(modSymbol) ;
        addModule(modStore) ;
        addModule(modTime) ;
    }
    
    protected void setModStore(ModStore modStore) { this.modStore = modStore ; }
    
    protected ModStore getModStore() { return modStore  ; }
    protected ModTime  getModTime()  { return modTime  ; }
    protected Store getStore() { return modStore.getStore()  ; }
    
    protected abstract void execCmd(List<String> positionalArgs) ;
    
    @Override
    final
    protected void exec()
    {
        SDB.init() ;                // Gets called anyway by Store assembler processing.
        AssemblerVocab.init() ;     // Call to install the assemblers
        @SuppressWarnings("unchecked")
        List<String> positionalArgs = (List<String>)super.getPositional() ;
        try {
            execCmd(positionalArgs) ;
        }
        finally { 
            if ( getModStore().hasStore() )
                getModStore().getStore().close();
            else
            {
                if ( getModStore().isConnected() )
                    getModStore().getConnection().close();
            }
        }
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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