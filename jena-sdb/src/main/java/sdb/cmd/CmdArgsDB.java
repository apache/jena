/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sdb.cmd;

import java.util.List;

import arq.cmdline.CmdGeneral;
import arq.cmdline.ModSymbol;
import arq.cmdline.ModTime;

import com.hp.hpl.jena.Jena;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;

public abstract class CmdArgsDB extends CmdGeneral
{
    static {
        //  Tune N3 output for result set output.
        System.setProperty("usePropertySymbols",   "false") ;
        System.setProperty("objectLists" ,         "false") ;
        System.setProperty("minGap",               "2") ;
        System.setProperty("propertyColumn",       "14") ;
    }
    
    private ModSymbol modSymbol = new ModSymbol() ;
    private ModStore  modStore  = new ModStore() ;
    private ModTime   modTime   = new ModTime() ;
    private ModLogSQL modLogSQL = new ModLogSQL() ;

    protected CmdArgsDB(String argv[])
    {
        super(argv) ;
        addModule(modSymbol) ;
        addModule(modStore) ;
        addModule(modLogSQL) ;
        addModule(modTime) ;
        ARQ.init() ;
        super.modVersion.addClass(Jena.class) ;
        super.modVersion.addClass(ARQ.class) ;
        super.modVersion.addClass(SDB.class) ;
    }
    
    protected void setModStore(ModStore modStore) { this.modStore = modStore ; }
    
    protected ModStore  getModStore()   { return modStore  ; }
    protected ModTime   getModTime()    { return modTime ; }
    protected ModLogSQL getModDBlog()   { return modLogSQL ; }
    protected StoreDesc getStoreDesc()  { return modStore.getStoreDesc() ; }
    protected Store     getStore()      { return modStore.getStore() ; }
    
    protected abstract void execCmd(List<String> positionalArgs) ;
    
    @Override
    final
    protected void exec()
    {
        SDB.init() ;                // Gets called anyway by Store assembler processing.
        AssemblerVocab.init() ;     // Call to install the assemblers
        List<String> positionalArgs = super.getPositional() ;
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
