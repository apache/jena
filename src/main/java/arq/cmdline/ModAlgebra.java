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

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.util.FileManager ;

public class ModAlgebra implements ArgModuleGeneral
{
    protected final ArgDecl queryFileDecl = new ArgDecl(ArgDecl.HasValue, "query", "file") ;

    private String queryFilename   = null ;
    private String queryString = null ;
    private Op op = null ;
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Query") ;
        cmdLine.add(queryFileDecl,
                    "--query, --file",
                    "File containing an algebra query") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(queryFileDecl) )
        {
            queryFilename = cmdLine.getValue(queryFileDecl) ;
            queryString = FileManager.get().readWholeFileAsUTF8(queryFilename) ;
        }
    
        if ( cmdLine.getNumPositional() == 0 && queryFilename == null )
            cmdLine.cmdError("No query string or query file") ;

        if ( cmdLine.getNumPositional() > 1 )
            cmdLine.cmdError("Only one query string allowed") ;
    
        if ( cmdLine.getNumPositional() == 1 && queryFilename != null )
            cmdLine.cmdError("Either query string or query file - not both") ;

        
        if ( queryFilename == null )
        {
            String qs = cmdLine.getPositionalArg(0) ;
            queryString = cmdLine.indirect(qs) ;
        }
    }
    
    public Op getOp()
    {
        if ( op != null )
            return op ;
        op = SSE.parseOp(queryString) ;
        if ( op == null )
            System.err.println("Failed to parse : "+queryString) ;
        return op ;
    }
}
