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

package arq;

import java.io.IOException ;
import java.util.List ;

import arq.cmdline.CmdARQ ;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.QueryException ;
import org.apache.jena.query.QueryParseException ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.core.QueryCheckException ;
import org.apache.jena.sparql.lang.QueryParserBase ;
import org.apache.jena.sparql.modify.request.UpdateWriter ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.util.FileUtils ;

public class uparse extends CmdARQ
{
    protected static final ArgDecl fileArg          = new ArgDecl(ArgDecl.HasValue, "file", "update") ;
    protected static final ArgDecl syntaxArg        = new ArgDecl(ArgDecl.HasValue, "syntax", "syn") ;
    protected static final ArgDecl argDeclPrint     = new ArgDecl(ArgDecl.HasValue, "print") ;
    protected static final ArgDecl argDeclFixup     = new ArgDecl(ArgDecl.NoValue, "fixup") ;

    List<String> requestFiles = null ;
    protected Syntax updateSyntax = null ;
    private boolean printUpdate = false ;
    private boolean printNone  = false ;
    
    public static void main (String... argv)
    { new uparse(argv).mainRun() ; }
    
    protected uparse(String[] argv)
    {
        super(argv) ;
        super.add(fileArg, "--file=FILE",  "Update commands to parse") ;
        super.add(syntaxArg, "--syntax=name", "Update syntax") ;
        super.add(argDeclPrint, "--print", "Print in various forms [update, none]") ;
        super.add(argDeclFixup, "--fixup", "Convert undeclared prefix names to URIs") ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        requestFiles = getValues(fileArg) ;
        super.processModulesAndArgs() ;
        if ( super.cmdStrictMode )
            updateSyntax = Syntax.syntaxSPARQL_11 ;
        
        if ( contains(argDeclFixup) )
            // Fixup undeclared prefix names.
            ARQ.set(ARQ.fixupUndefinedPrefixes, true);

        // Set syntax
        if ( super.contains(syntaxArg) ) {
            // short name
            String s = super.getValue(syntaxArg) ;
            Syntax syn = Syntax.lookup(s) ;
            if ( syn == null )
                super.cmdError("Unrecognized syntax: " + s) ;
            updateSyntax = syn ;
        }
        
        for ( String arg : getValues( argDeclPrint ) )
        {
            if ( arg.equalsIgnoreCase( "query" ) )
                printUpdate = true;
            else if ( arg.equalsIgnoreCase( "none" ) )
                printNone = true;
            else
                throw new CmdException("Not a recognized print form: " + arg + " : Choices are: update, none" );
        }
        
        if ( !printUpdate && ! printNone )
            printUpdate = true ;
    }
    
    @Override
    protected String getCommandName() { return Lib.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --file=<request file> | <update string>" ; }

    @Override
    protected void exec()
    {
        for ( String filename : requestFiles )
        {
            Syntax syntax = updateSyntax ;
            if ( syntax == null )
                syntax = Syntax.guessFileSyntax(filename) ;
            String x = oneFile( filename );
            if ( x != null )
                execOne( x, syntax );
        }
        
        for ( String x : super.positionals ) {
            Syntax syntax = updateSyntax ;    
            if ( matchesIndirect(x) ) {
                if ( syntax == null )
                    syntax = Syntax.guessFileSyntax(x) ;
                x = indirect( x );
            }
            if ( syntax == null )
                syntax = Syntax.defaultUpdateSyntax ;
            execOne( x, syntax );
        }

    }
    
    private String oneFile(String filename)
    {
        divider() ;
        try
        {
            return FileUtils.readWholeFileAsUTF8(filename) ;
        } catch (IOException ex)
        {
            System.err.println("No such file: "+filename) ;
            return null ;
        }
    }
    
    private void execOne(String updateString, Syntax syntax)
    {
        UpdateRequest req ; 
        try {
            req = UpdateFactory.create(updateString, syntax) ;
        } catch (QueryParseException ex)
        {
            System.err.print("Parse error: ") ;
            System.err.println(ex.getMessage()) ;
            return ; 
        }
        //req.output(IndentedWriter.stderr) ;
        if ( printUpdate )
            System.out.print(req) ;
        
        if ( printNone )
            return ;
        

        try {
            LogCtl.disable(QueryParserBase.ParserLoggerName) ;
            checkUpdate(req, syntax);
        } catch (UpdateCheckException ex)
        {
            System.err.println() ;
            System.err.println("**** Check failure: "+ex.getMessage()) ;
            if ( ex.getCause() != null )
                ex.getCause().printStackTrace(System.err) ;
        }
        finally { LogCtl.setLevel(QueryParserBase.ParserLoggerName, "INFO") ; }
    }
    
    public static class UpdateCheckException extends QueryException
    {
        public UpdateCheckException() { super() ; }
        public UpdateCheckException(Throwable cause) { super(cause) ; }
        public UpdateCheckException(String msg) { super(msg) ; }
        public UpdateCheckException(String msg, Throwable cause) { super(msg, cause) ; }
    }

    
    public static void checkUpdate(UpdateRequest req, Syntax syntax)
    {
        IndentedLineBuffer w = new IndentedLineBuffer() ;
        UpdateWriter.output(req, w) ;
        String updateString2 = w.asString() ;
        
        UpdateRequest req2;
        try {
            String baseURI = null ;
            if ( ! req.explicitlySetBaseURI() )
                baseURI = req.getBaseURI() ;
            req2 = UpdateFactory.create(updateString2, syntax) ;
        } catch (UnsupportedOperationException ex)
        {
            // No parser after all.
            return ;
        }
        catch (QueryException ex)
        {
            System.err.println(updateString2) ;
            throw new QueryCheckException("could not parse output update request", ex) ;
        }
        
//        if ( req.hashCode() != req2.hashCode() )
//            throw new UpdateCheckException("reparsed query hashCode does not equal parsed input update \nUpdate (hashCode: " + req.hashCode() + ")=\n" + req + "\n\nUpdate2 (hashCode: " + req2.hashCode() + ")=\n" + req2) ;
//        
//        if ( ! req.equals(req2) ) 
//            throw new UpdateCheckException("reparsed output does not equal parsed input") ;
    }

    
    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    //static final String divider = "" ;
    static boolean needDivider = false ;
    private static void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
}
