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


import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;

public class ModResultsOut implements ArgModuleGeneral
{
    protected final 
    ArgDecl resultsFmtDecl = new ArgDecl(ArgDecl.HasValue, "results", "out", "rfmt") ;

    private ResultsFormat resultsFormat = ResultsFormat.FMT_UNKNOWN ;
    
    @Override
    public void processArgs(CmdArgModule cmdline) throws IllegalArgumentException
    {
        if ( cmdline.contains(resultsFmtDecl) )
        {
            String rFmt = cmdline.getValue(resultsFmtDecl) ;
            resultsFormat = ResultsFormat.lookup(rFmt) ;
            if ( resultsFormat == null )
                cmdline.cmdError("Unrecognized output format: "+rFmt) ;
        }
    }
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Results") ;
        cmdLine.add(resultsFmtDecl,
                    "--results",
                    "Results format (Result set: text, XML, JSON, CSV, TSV; Graph: RDF serialization)") ;  
    }

    public void checkCommandLine(CmdArgModule cmdLine)
    {}

    public void printResultSet(ResultSet resultSet, Prologue prologue)
    {
        QueryExecUtils.outputResultSet(resultSet, prologue, resultsFormat) ;
    }
    
    public ResultsFormat getResultsFormat() { return resultsFormat ; }

}
