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


import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.util.QueryOutputUtils ;

public class ModQueryOut implements ArgModuleGeneral
{
    protected final ArgDecl queryOutputSyntaxDecl  = new ArgDecl(ArgDecl.HasValue, "out", "format") ;
    protected final ArgDecl queryNumberDecl        = new ArgDecl(ArgDecl.NoValue, "num", "number") ;

    private Syntax outputSyntax = Syntax.syntaxSPARQL ;
    private boolean lineNumbers = false ;
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Output") ;
        cmdLine.add(queryOutputSyntaxDecl, "--out, --format",  "Output syntax") ;
        cmdLine.add(queryNumberDecl, "--num", "Print line numbers") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdline) throws IllegalArgumentException
    {
        if ( cmdline.contains(queryOutputSyntaxDecl) )
        {
            // short name
            String s = cmdline.getValue(queryOutputSyntaxDecl) ;
            Syntax syn = Syntax.lookup(s) ;
            if ( syn == null )
                cmdline.cmdError("Unrecognized syntax: "+s) ;
            outputSyntax = syn ; 
        }        
        
        lineNumbers = cmdline.contains(queryNumberDecl) ;
    }
    
    public Syntax getOutputSyntax()
    {
        return outputSyntax ;
    }

    public void output(Query query)
    { output(out(), query) ; }
    
    public void output(IndentedWriter out, Query query)
    { QueryOutputUtils.printQuery(out, query, outputSyntax) ; }
    
    public void outputOp(Query query, boolean printOptimized)
    { outputOp(out(), query, printOptimized) ; }

    public void outputOp(IndentedWriter out, Query query, boolean printOptimized)
    { QueryOutputUtils.printOp(out, query, printOptimized) ; }
    
    public void outputQuad(Query query, boolean printOptimized)
    { outputQuad(out(), query, printOptimized) ; }
    
    public void outputQuad(IndentedWriter out, Query query, boolean printOptimized)
    { QueryOutputUtils.printQuad(out, query, printOptimized) ; }
    
    private IndentedWriter out()
    {
        return new IndentedWriter(System.out, lineNumbers) ;
    }
    
}
