/**
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
import java.util.Iterator ;
import java.util.List ;

import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;

import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;
import com.hp.hpl.jena.util.FileUtils ;

public class uparse extends CmdARQ
{
    protected static final ArgDecl fileArg = new ArgDecl(ArgDecl.HasValue, "file", "update") ;
    protected static final ArgDecl syntaxArg = new ArgDecl(ArgDecl.HasValue, "syntax", "syn") ;
    List<String> requestFiles = null ;
    protected Syntax updateSyntax = Syntax.defaultUpdateSyntax ;
    
    public static void main (String... argv)
    { new uparse(argv).mainRun() ; }
    
    protected uparse(String[] argv)
    {
        super(argv) ;
        super.add(fileArg, "--file=FILE",  "Update commands to parse") ;
        super.add(syntaxArg, "--syntax=name", "Update syntax") ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        requestFiles = getValues(fileArg) ;
        super.processModulesAndArgs() ;
        if ( super.cmdStrictMode )
            updateSyntax = Syntax.syntaxSPARQL_11 ;
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --file=<request file> | <update string>" ; }

    @Override
    protected void exec()
    {
        for ( Iterator<String> iter = requestFiles.listIterator() ; iter.hasNext() ; )
        {
            String filename = iter.next();
            String x = oneFile(filename) ;
            if ( x != null )
                execOne(x) ; 
        }
        
        for ( Iterator<String> iter = super.positionals.listIterator() ; iter.hasNext() ; )
        {
            String x = iter.next();
            x =  indirect(x) ;
            execOne(x) ; 
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
        UpdateRequest req = UpdateFactory.create(updateString, updateSyntax) ;
        //req.output(IndentedWriter.stderr) ;
        System.out.print(req) ;
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
