/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.io.IOException;

import arq.cmd.CmdException;
import arq.cmd.TerminationException;
import arq.cmdline.*;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.resultset.ResultSetException;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileUtils;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryException;

public class qexec extends CmdARQ
{
    protected final ArgDecl queryFileDecl = new ArgDecl(ArgDecl.HasValue, "query", "file") ;
    ModDataset    modDataset =  new ModAssembler() ;    // extends ModDataset
    ModResultsOut modResults =  new ModResultsOut() ;
    ModTime       modTime =     new ModTime() ;
    String queryFilename = null ;
    String queryString   = null ;
    
    public static void main (String [] argv)
    {
        new qexec(argv).main() ;
    }
    
    public qexec(String[] argv)
    {
        super(argv) ;
        super.add(queryFileDecl, "--query=FILE", "Algebra file to execute") ;
        super.addModule(modResults) ;
        super.addModule(modDataset) ;
        super.addModule(modTime) ;
    }

    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( contains(queryFileDecl) )
            queryFilename = super.getValue(queryFileDecl) ;
        
    }
    
    protected String getCommandName() { return Utils.className(this) ; }
    
    protected String getSummary() { return getCommandName()+" --data=<file> --query=<query>" ; }

    protected void exec()
    {
    try {
        // ModAlgebra?
        
        Dataset dataset = modDataset.getDataset() ;
        // Check there is a dataset
        if ( dataset == null )
        {
            System.err.print("No dataset") ;
            throw new TerminationException(1) ;
        }
        
        Op op = null ;
        
        if ( queryFilename != null )
        {
            if ( queryFilename.equals("-") )
            {
                try {
                    // Stderr?
                    queryString  = FileUtils.readWholeFileAsUTF8(System.in) ;
                    // And drop into next if
                } catch (IOException ex)
                { throw new CmdException("Error reading stdin", ex) ; }
            }
            else
                op = Algebra.read(queryFilename) ;
        }

        if ( queryString != null )
            op = Algebra.parse(queryString) ;
        
        if ( op == null )
        {
            System.err.println("No query expression to execute") ;
            throw new TerminationException(9) ;
        }
        
        modTime.startTimer() ;
        QueryExecUtils.executeAlgebra(op, dataset, modResults.getResultsFormat()) ;
        long time = modTime.endTimer() ;
        if ( modTime.timingEnabled() )
            System.out.println("Time: "+modTime.timeStr(time)) ;
        
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
    }
    catch (ResultSetException ex)
    {
        System.err.println(ex.getMessage()) ;
        ex.printStackTrace(System.err) ;
    }
    catch (QueryException qEx)
    {
        //System.err.println(qEx.getMessage()) ;
        throw new CmdException("Query Exeception", qEx) ;
    }
    catch (JenaException ex) { throw ex ; } 
    catch (CmdException ex) { throw ex ; } 
    catch (Exception ex)
    {
        throw new CmdException("Exception", ex) ;
    }
}    

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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