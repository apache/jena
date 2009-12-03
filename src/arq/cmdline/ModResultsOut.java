/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;


import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;

public class ModResultsOut implements ArgModuleGeneral
{
    protected final 
    ArgDecl resultsFmtDecl = new ArgDecl(ArgDecl.HasValue, "results", "out", "rfmt") ;

    private ResultsFormat resultsFormat = ResultsFormat.FMT_UNKNOWN ;
    
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