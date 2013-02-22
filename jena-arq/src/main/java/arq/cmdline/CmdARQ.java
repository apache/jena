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

package arq.cmdline;

import org.apache.jena.riot.RIOT ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase ;
import com.hp.hpl.jena.sparql.util.Utils ;

public abstract class CmdARQ extends CmdGeneral
{
    protected ModSymbol modSymbol = new ModSymbol() ;
    ArgDecl  strictDecl = new ArgDecl(ArgDecl.NoValue, "strict") ;
    
    protected boolean cmdStrictMode = false ; 
    
    protected CmdARQ(String[] argv)
    {
        super(argv) ;
        addModule(modSymbol) ;
        super.add(strictDecl, "--strict", "Operate in strict SPARQL mode (no extensions of any kind)") ;
        super.modVersion.addClass(ARQ.class) ;
        super.modVersion.addClass(RIOT.class) ;
    }
    
    @Override
    protected void processModulesAndArgs()
    { 
        if ( modVersion.getVersionFlag() )
            modVersion.printVersionAndExit() ;
        if ( super.contains(strictDecl) ) 
            ARQ.setStrictMode() ;
        cmdStrictMode = super.contains(strictDecl) ;
        if ( modGeneral.debug )
            QueryIteratorBase.traceIterators = true ;
    }
    
    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
    }
}
