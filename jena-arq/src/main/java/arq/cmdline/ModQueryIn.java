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

package arq.cmdline ;

import java.io.IOException ;

import arq.cmd.CmdException ;
import arq.cmd.TerminationException ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.util.FileUtils ;

public class ModQueryIn implements ArgModuleGeneral {
    protected final ArgDecl queryFileDecl   = new ArgDecl(ArgDecl.HasValue, "query", "file") ;
    protected final ArgDecl querySyntaxDecl = new ArgDecl(ArgDecl.HasValue, "syntax", "syn", "in") ;
    protected final ArgDecl queryBaseDecl   = new ArgDecl(ArgDecl.HasValue, "base") ;

    private Syntax          defaultQuerySyntax     = Syntax.syntaxARQ ;
    private Syntax          querySyntax     = null ;
    private String          queryFilename   = null ;
    private String          queryString     = null ;
    private Query           query           = null ;
    private String          baseURI         = null ;

    public ModQueryIn(Syntax defaultSyntax) {
        defaultQuerySyntax = defaultSyntax ;
        querySyntax = defaultSyntax ;
    }

    @Override
    public void registerWith(CmdGeneral cmdLine) {
        cmdLine.getUsage().startCategory("Query") ;
        cmdLine.add(queryFileDecl,   "--query, --file",  "File containing a query") ;
        cmdLine.add(querySyntaxDecl, "--syntax, --in",   "Syntax of the query") ;
        cmdLine.add(queryBaseDecl,   "--base",           "Base URI for the query") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdline) throws IllegalArgumentException {
        if ( cmdline.contains(queryBaseDecl) )
            baseURI = cmdline.getValue(queryBaseDecl) ;

        if ( cmdline.contains(queryFileDecl) ) {
            queryFilename = cmdline.getValue(queryFileDecl) ;
            querySyntax = Syntax.guessFileSyntax(queryFilename, defaultQuerySyntax) ;
        }

        if ( cmdline.getNumPositional() == 0 && queryFilename == null )
            cmdline.cmdError("No query string or query file") ;

        if ( cmdline.getNumPositional() > 1 )
            cmdline.cmdError("Only one query string allowed") ;

        if ( cmdline.getNumPositional() == 1 && queryFilename != null )
            cmdline.cmdError("Either query string or query file - not both") ;

        if ( queryFilename == null ) {
            // One positional argument.
            String qs = cmdline.getPositionalArg(0) ;
            if ( cmdline.matchesIndirect(qs) )
                querySyntax = Syntax.guessFileSyntax(qs, defaultQuerySyntax) ;

            queryString = cmdline.indirect(qs) ;
        }

        // Set syntax
        if ( cmdline.contains(querySyntaxDecl) ) {
            // short name
            String s = cmdline.getValue(querySyntaxDecl) ;
            Syntax syn = Syntax.lookup(s) ;
            if ( syn == null )
                cmdline.cmdError("Unrecognized syntax: " + s) ;
            querySyntax = syn ;
        }
    }

    public Syntax getQuerySyntax() {
        return querySyntax ;
    }

    public Query getQuery() {
        if ( query != null )
            return query ;

        if ( queryFilename != null && queryString != null ) {
            System.err.println("Both query string and query file name given") ;
            throw new TerminationException(1) ;
        }

        if ( queryFilename == null && queryString == null ) {
            System.err.println("No query string and no query file name given") ;
            throw new TerminationException(1) ;
        }

        try {
            if ( queryFilename != null ) {
                if ( queryFilename.equals("-") ) {
                    try {
                        // Stderr?
                        queryString = FileUtils.readWholeFileAsUTF8(System.in) ;
                        // And drop into next if
                    } catch (IOException ex) {
                        throw new CmdException("Error reading stdin", ex) ;
                    }
                } else {
                    query = QueryFactory.read(queryFilename, baseURI, getQuerySyntax()) ;
                    return query ;
                }
            }

            query = QueryFactory.create(queryString, baseURI, getQuerySyntax()) ;
            return query ;
        } catch (ARQInternalErrorException intEx) {
            System.err.println(intEx.getMessage()) ;
            if ( intEx.getCause() != null ) {
                System.err.println("Cause:") ;
                intEx.getCause().printStackTrace(System.err) ;
                System.err.println() ;
            }
            intEx.printStackTrace(System.err) ;
            throw new TerminationException(99) ;
        }
        catch (JenaException ex) {
            System.err.println(ex.getMessage()) ;
            throw new TerminationException(2) ;
        } catch (Exception ex) {
            System.out.flush() ;
            ex.printStackTrace(System.err) ;
            throw new TerminationException(98) ;
        }
    }
}
