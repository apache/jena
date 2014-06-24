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

import org.apache.jena.atlas.io.IndentedLineBuffer ;

import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;

import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.modify.request.UpdateWriter ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;
import com.hp.hpl.jena.util.FileUtils ;

public class uparse extends CmdARQ
{
    protected static final ArgDecl fileArg          = new ArgDecl(ArgDecl.HasValue, "file", "update") ;
    protected static final ArgDecl syntaxArg        = new ArgDecl(ArgDecl.HasValue, "syntax", "syn") ;
    protected static final ArgDecl argDeclPrint     = new ArgDecl(ArgDecl.HasValue, "print") ;
    List<String> requestFiles = null ;
    protected Syntax updateSyntax = Syntax.defaultUpdateSyntax ;
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
    }

    @Override
    protected void processModulesAndArgs()
    {
        requestFiles = getValues(fileArg) ;
        super.processModulesAndArgs() ;
        if ( super.cmdStrictMode )
            updateSyntax = Syntax.syntaxSPARQL_11 ;

        for ( String arg : getValues( argDeclPrint ) )
        {
            if ( arg.equalsIgnoreCase( "query" ) )
            {
                printUpdate = true;
            }
            else if ( arg.equalsIgnoreCase( "none" ) )
            {
                printNone = true;
            }
            else
            {
                throw new CmdException(
                    "Not a recognized print form: " + arg + " : Choices are: query, op, quad, opt, optquad" );
            }
        }
        
        if ( !printUpdate && ! printNone )
            printUpdate = true ;
        
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --file=<request file> | <update string>" ; }

    @Override
    protected void exec()
    {
        for ( String filename : requestFiles )
        {
            String x = oneFile( filename );
            if ( x != null )
            {
                execOne( x );
            }
        }

        for ( String x : super.positionals )
        {
            x = indirect( x );
            execOne( x );
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
    
    private void execOne(String updateString)
    {
        UpdateRequest req ; 
        try {
            req = UpdateFactory.create(updateString, updateSyntax) ;
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
        
        // And some checking.
        IndentedLineBuffer w = new IndentedLineBuffer() ;
        UpdateWriter.output(req, w) ;
        String updateString2 = w.asString() ;
        UpdateRequest req2 = null ;
        try {
            req2 = UpdateFactory.create(updateString2, updateSyntax) ;
        } catch (QueryParseException ex)
        {
            System.err.println("Can not reparse update after serialization") ;
            System.err.println(updateString2) ; 
        }

        if ( ! req.equalTo(req2) )
            System.err.println("Reparsed update does not .equalTo original parsed request") ;
        
        
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
