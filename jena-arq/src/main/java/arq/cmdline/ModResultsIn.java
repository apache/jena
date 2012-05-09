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

import arq.cmd.TerminationException ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.shared.NotFoundException ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;

public class ModResultsIn implements ArgModuleGeneral
{
    protected final ArgDecl resultsInputFmtDecl = new ArgDecl(ArgDecl.HasValue, "in") ;
    protected final ArgDecl fileDecl = new ArgDecl(ArgDecl.HasValue, "file") ;

    private ResultsFormat inputFormat = ResultsFormat.FMT_TEXT ;
    private String resultsFilename = null ;
    private ResultSet resultSet = null ;
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Results") ;
        cmdLine.add(fileDecl,
                    "--file",
                    "Input file") ;
        cmdLine.add(resultsInputFmtDecl,
                    "--in",
                    "Results format (XML, JSON; RDF serialization)") ;  
    }

    @Override
    public void processArgs(CmdArgModule cmdline) throws IllegalArgumentException
    {
        // Input file.
        if ( cmdline.contains(fileDecl) )
            resultsFilename = cmdline.getValue(fileDecl) ;
        
        if ( cmdline.getNumPositional() == 0 && resultsFilename == null )
            cmdline.cmdError("No results file") ;

        if ( cmdline.getNumPositional() > 1 )
            cmdline.cmdError("Only one result set file allowed") ;
            
        if ( cmdline.getNumPositional() == 1 && resultsFilename != null )
            cmdline.cmdError("Either result set file or --file - not both") ;

        if ( resultsFilename == null )
            // One positional argument.
            resultsFilename = cmdline.getPositionalArg(0) ;
        
        // Guess format
        if ( resultsFilename != null )
            inputFormat = ResultsFormat.guessSyntax(resultsFilename) ;

        // Set format
        if ( cmdline.contains(resultsInputFmtDecl) )
        {
            String rFmt = cmdline.getValue(resultsInputFmtDecl) ;
            inputFormat = ResultsFormat.lookup(rFmt) ;
            if ( inputFormat == null )
                cmdline.cmdError("Unrecognized output format: "+rFmt) ;
        }
    }
    
    public void checkCommandLine(CmdArgModule cmdLine)
    {}
    
    
    public ResultSet getResultSet()
    {
        if ( resultSet != null )
            return resultSet ;
        
        if ( resultsFilename == null )
        {
            System.err.println("No result file name" ) ;
            throw new TerminationException(1) ;
        }

        try
        {
            if ( resultsFilename.equals("-") )
                return ResultSetFactory.load(System.in, inputFormat) ;
            ResultSet rs = ResultSetFactory.load(resultsFilename, inputFormat) ;
            if ( rs == null )
            {
                System.err.println("Failed to read the result set") ;
                throw new TerminationException(9) ;
            }
            resultSet = rs ;
            return resultSet ;
        }
        catch (NotFoundException ex)
        {
            System.err.println("File not found: "+resultsFilename) ;
            throw new TerminationException(9) ;
        }
        catch (ARQInternalErrorException intEx)
        {
            System.err.println(intEx.getMessage()) ;
            if ( intEx.getCause() != null )
            {
                System.err.println("Cause:") ;
                intEx.getCause().printStackTrace(System.err) ;
                System.err.println() ;
            }
            intEx.printStackTrace(System.err) ;
            throw new TerminationException(99) ;
        }
    }

    
    public ResultsFormat getInputFormat() { return inputFormat ; }

}
