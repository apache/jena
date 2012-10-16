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

import com.hp.hpl.jena.sdb.Store;

import arq.cmdline.*;

public class ModConfig extends ModBase
{
    protected final ArgDecl argDeclFormat = new ArgDecl(ArgDecl.NoValue, "format") ;
    protected final ArgDecl argDeclCreate = new ArgDecl(ArgDecl.NoValue, "create") ;
    protected final ArgDecl argDeclDropIndexes = new ArgDecl(ArgDecl.NoValue, "dropIndexes", "drop") ;
    protected final ArgDecl argDeclIndexes = new ArgDecl(ArgDecl.NoValue, "addIndexes", "indexes", "index") ;
    
    private boolean format = false ;
    private boolean createStore = false ;
    private boolean dropIndexes = false ;
    private boolean createIndexes = false ;
    
    public ModConfig() {}

    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.add(argDeclCreate,
                    "--create", "Format a database and add indexes") ;
        cmdLine.add(argDeclFormat,
                    "--format", "Format a database (no indexes)") ;
        cmdLine.add(argDeclDropIndexes,
                    "--drop", "Drop indexes") ;
        cmdLine.add(argDeclIndexes,
                    "--indexes", "Add indexes") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        format = cmdLine.contains(argDeclFormat) ;
        createStore = cmdLine.contains(argDeclCreate) ;
        dropIndexes = cmdLine.contains(argDeclDropIndexes) ;
        createIndexes = cmdLine.contains(argDeclIndexes) ;
    }

    public boolean addIndexes()     { return createIndexes ; }

    public boolean dropIndexes()      { return dropIndexes ; }

    public boolean format()           { return format ; }
    public boolean createStore()           { return createStore ; }

    public void enact(Store store)      { enact(store, null) ; }
    public void enact(Store store, ModTime timer)
    {
        // "create" = format + build indexes.
        if ( createStore() )
        {
            if ( timer != null  )
                timer.startTimer() ;
            store.getTableFormatter().create() ;
            if ( timer != null  )
            {
                long time = timer.endTimer() ;
                printTime("create", time) ;
            }
            
        }
        if ( format() && ! createStore() )
        {
            if ( timer != null  )
                timer.startTimer() ;
            store.getTableFormatter().format() ;
            if ( timer != null  )
            {
                long time = timer.endTimer() ;
                printTime("format", time) ;
            }
        }
        if ( dropIndexes() )
        {
            if ( timer != null  )
                timer.startTimer() ;
            store.getTableFormatter().dropIndexes() ;
            if ( timer != null  )
            {
                long time = timer.endTimer() ;
                printTime("drop indexes", time) ;
            }
        }
        
        if ( addIndexes() )
        {
            if ( timer != null  )
                timer.startTimer() ;
            store.getTableFormatter().addIndexes() ;
            if ( timer != null  )
            {
                long time = timer.endTimer() ;
                printTime("add indexes", time) ;
            }
        }
    }

    private void printTime(String string, long timeMilli)
    {
            System.out.printf("Operation: %s: Time %.3f seconds\n", 
                              string, timeMilli/1000.0) ;
    }
    
}
