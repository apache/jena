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

import java.util.List ;

import org.apache.jena.cmd.*;
import org.apache.jena.sparql.engine.main.QueryEngineMain ;
import org.apache.jena.sparql.engine.main.QueryEngineMainQuad ;
import org.apache.jena.sparql.engine.ref.QueryEngineRef ;
import org.apache.jena.sparql.engine.ref.QueryEngineRefQuad ;


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
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Query Engine") ;
        cmdLine.add(engineDecl, "--engine=EngineName", "Register another engine factory[ref]") ; 
        cmdLine.add(unEngineDecl, "--unengine=EngineName", "Unregister an engine factory") ;
    }
    
    public void checkCommandLine(CmdGeneral cmdLine)
    {}

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
       
        List<String> engineDecls = cmdLine.getValues(engineDecl) ;
        
//        if ( x.size() > 0 )
//            QueryEngineRegistry.get().factories().clear() ;

        for ( String engineName : engineDecls )
        {
			switch (engineName.toLowerCase()) {
				case "reference":
				case "ref":
					QueryEngineRef.register();
					continue;
				case "refQuad":
					QueryEngineRefQuad.register();
					continue;
				case "main":
					QueryEngineMain.register();
					continue;
				case "quad":
					QueryEngineMainQuad.register();
					continue;
				}
			throw new CmdException("Engine name not recognized: " + engineName);
		}

        List<String> unEngineDecls = cmdLine.getValues(unEngineDecl) ;
        for (String engineName : unEngineDecls)
        {
	        	switch (engineName.toLowerCase()) {
				case "reference":
				case "ref":
					QueryEngineRef.register();
					continue;
				case "refQuad":
					QueryEngineRefQuad.register();
					continue;
				case "main":
					QueryEngineMain.register();
					QueryEngineMainQuad.register();
					continue;      
	        }
        		throw new CmdException("Engine name not recognized: "+engineName) ;
        }
    }
}
