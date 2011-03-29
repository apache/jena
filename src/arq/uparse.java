/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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