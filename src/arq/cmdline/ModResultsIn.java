/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import arq.cmd.TerminationException;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;

public class ModResultsIn implements ArgModuleGeneral
{
    protected final ArgDecl resultsInputFmtDecl = new ArgDecl(ArgDecl.HasValue, "in") ;
    protected final ArgDecl fileDecl = new ArgDecl(ArgDecl.HasValue, "file") ;

    private ResultSetFormat inputFormat = ResultSetFormat.syntaxText ;
    private String resultsFilename = null ;
    private ResultSet resultSet = null ;
    
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
            inputFormat = ResultSetFormat.guessSyntax(resultsFilename) ;

        // Set format
        if ( cmdline.contains(resultsInputFmtDecl) )
        {
            String rFmt = cmdline.getValue(resultsInputFmtDecl) ;
            inputFormat = ResultSetFormat.lookup(rFmt) ;
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

    
    public ResultSetFormat getInputFormat() { return inputFormat ; }

}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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