/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.lang.Parser;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class ModQueryOut implements ArgModuleGeneral
{
    protected final ArgDecl queryOutputSyntaxDecl  = new ArgDecl(ArgDecl.HasValue, "out", "format") ;
    protected final ArgDecl queryNumberDecl        = new ArgDecl(ArgDecl.HasValue, "num", "number") ;
    protected final ArgDecl queryPlainDecl         = new ArgDecl(ArgDecl.NoValue, "plain") ;

    private Syntax outputSyntax = Syntax.syntaxSPARQL ;
    private boolean lineNumbers = true ;
    
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.getUsage().startCategory("Output") ;
        cmdLine.add(queryOutputSyntaxDecl,
                    "--out, --format",
                    "Output syntax") ;
        cmdLine.add(queryNumberDecl,
                    "--num [on|off]",
                    "Numbers") ;
        cmdLine.add(queryPlainDecl,
                    "--plain",
                    "Plain output") ;
    }

    public void processArgs(CmdArgModule cmdline) throws IllegalArgumentException
    {
        if ( cmdline.contains(queryOutputSyntaxDecl) )
        {
            // short name
            String s = cmdline.getValue(queryOutputSyntaxDecl) ;
            Syntax syn = Syntax.lookup(s) ;
            if ( syn == null )
                cmdline.cmdError("Unrecognized syntax: "+s) ;
            outputSyntax = syn ; 
        }        
        
        if ( cmdline.contains(queryNumberDecl) )
            lineNumbers = cmdline.getValue(queryNumberDecl).equalsIgnoreCase("on") ;
        
        if ( cmdline.contains(queryPlainDecl) )
            lineNumbers = false ;
    }
    
    public Syntax getOutputSyntax()
    {
        return outputSyntax ;
    }

    public void output(Query query)
    {
        IndentedWriter w = new IndentedWriter(System.out, lineNumbers) ;
        query.serialize(w, outputSyntax) ;
        w.flush() ;
    }
    
    public void checkParse(Query query)
    {
        if ( ! Parser.canParse(outputSyntax) )
            return ;
        
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        query.serialize(buff, outputSyntax) ;
        
        String tmp = buff.toString() ;
        
        Query query2 = null ;
        try {
            String baseURI = null ;
            if ( ! query.explicitlySetBaseURI() )
                // Not in query - use the same one (e.g. file read from) .  
                baseURI = query.getBaseURI() ;
            
            query2 = QueryFactory.create(tmp, baseURI, outputSyntax) ;
            
            if ( query2 == null )
                return ;
        } catch (UnsupportedOperationException ex)
        {
            // No parser after all.
            return ;
        }
        catch (QueryException ex)
        {
            System.out.println() ;
            System.out.println("**** Check failed : could not parse output query:: ") ;
            System.out.println("**** "+ex.getMessage()) ;
            return ;
        }
        
        if ( query.hashCode() != query2.hashCode() )
        {
            System.out.println() ;
            System.out.println("**** Check failed : reparsed query hashCode does not equal parsed input query") ;
            //return ;
        }
        
        if ( ! query.equals(query2) ) 
        {
            System.out.println() ;
            System.out.println("**** Check failed : reparsed output does not equal parsed input") ;
            return ;
        }
    }
    
    
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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