/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.io.IOException;

import arq.cmd.CmdException;
import arq.cmd.TerminationException;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.util.FileUtils;

public class ModQueryIn implements ArgModuleGeneral
{
    protected final ArgDecl queryFileDecl = new ArgDecl(ArgDecl.HasValue, "query", "file") ;
    protected final ArgDecl querySyntaxDecl = new ArgDecl(ArgDecl.HasValue, "syntax", "syn", "in") ;
    protected final ArgDecl queryBaseDecl = new ArgDecl(ArgDecl.HasValue, "base") ;

    private Syntax querySyntax = Syntax.syntaxARQ ;
    private String queryFilename   = null ;
    private String queryString = null ;
    private Query query = null ;
    private String baseURI = null ;
    
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Query") ;
        cmdLine.add(queryFileDecl,
                    "--query, --file",
                    "File containing a query") ;
        cmdLine.add(querySyntaxDecl,
                    "--syntax, --in",
                    "Syntax of the query") ;
        cmdLine.add(queryBaseDecl,
                    "--base",
                    "Base URI for the query") ;
    }

    public void processArgs(CmdArgModule cmdline) throws IllegalArgumentException
    {
        if ( cmdline.contains(queryBaseDecl) )
            baseURI = cmdline.getValue(queryBaseDecl) ; 
        
        if ( cmdline.contains(queryFileDecl) )
        {
            queryFilename = cmdline.getValue(queryFileDecl) ;
            querySyntax = Syntax.guessQueryFileSyntax(queryFilename) ;
        }
        
        if ( cmdline.getNumPositional() == 0 && queryFilename == null )
            cmdline.cmdError("No query string or query file") ;

        if ( cmdline.getNumPositional() > 1 )
            cmdline.cmdError("Only one query string allowed") ;
        
        if ( cmdline.getNumPositional() == 1 && queryFilename != null )
            cmdline.cmdError("Either query string or query file - not both") ;

        // Guess syntax
        if ( queryFilename == null )
        {
            // One positional argument.
            String qs = cmdline.getPositionalArg(0) ;
            if ( cmdline.matchesIndirect(qs) ) 
                querySyntax = Syntax.guessQueryFileSyntax(qs) ;
            
            queryString = cmdline.indirect(qs) ;
        }
        
        // Set syntax
        if ( cmdline.contains(querySyntaxDecl) )
        {
            // short name
            String s = cmdline.getValue(querySyntaxDecl) ;
            Syntax syn = Syntax.lookup(s) ;
            if ( syn == null )
                cmdline.cmdError("Unrecognized syntax: "+s) ;
            querySyntax = syn ; 
        }
    }
    
    public Syntax getQuerySyntax()
    {
        return querySyntax ;
    }
    
    public Query getQuery()
    {
        if ( query != null )
            return query ;
        
        if ( queryFilename != null && queryString != null )
        {
            System.err.println("Both query string and query file name given" ) ;
            throw new TerminationException(1) ;
        }
        
        if ( queryFilename == null && queryString == null )
        {
            System.err.println("No query string and no query file name given" ) ;
            throw new TerminationException(1) ;
        }

        try
        {
            if ( queryFilename != null )
            {
                if ( queryFilename.equals("-") )
                {
                    try {
                        // Stderr?
                        queryString  = FileUtils.readWholeFileAsUTF8(System.in) ;
                        // And drop into next if
                    } catch (IOException ex)
                    {
                        throw new CmdException("Error reading stdin", ex) ;
                    }
                }
                else
                {
                    query = QueryFactory.read(queryFilename, baseURI, getQuerySyntax()) ;
                    return query ;
                }
            }
            
            query = QueryFactory.create(queryString, baseURI, getQuerySyntax()) ;
            return query ;
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
        catch (QueryParseException parseEx)
        {
            System.err.println(parseEx.getMessage()) ;
            throw new TerminationException(2) ;
        }
        catch (QueryException qEx)
        {
            System.err.println(qEx.getMessage()) ;
            throw new TerminationException(2) ;
        }
        catch (JenaException ex)
        {
            System.err.println(ex.getMessage()) ;
            throw new TerminationException(2) ;
        }
        catch (Exception ex)
        {
            System.out.flush() ;
            ex.printStackTrace(System.err) ;
            throw new TerminationException(98) ;
        }
    }
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