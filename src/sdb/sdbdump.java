/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package sdb;

import java.util.List;

import sdb.cmd.CmdArgsDB;
import sdb.cmd.ModGraph;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.util.Utils;

 /** Write out the data from an SDB model.  Only works for small models
  * because of JDBC limitations in default configurations. 
  * 
  *  <p>
  *  Usage:<pre>
  *  sdbdump [db spec]
  *  where [db spec] is:
  *    --spec file        Contains an RDF description of the model 
  *    --db JDBC_url --dbUser userId --dbPassword password --dbType [--model modelName]  
  *  </pre>
  *  </p>
  * 
  * @author Andy Seaborne
  * @version $Id: sdbdump.java,v 1.14 2006/04/22 19:51:11 andy_seaborne Exp $
  */ 
 
public class sdbdump extends CmdArgsDB
{
    public static final String usage = "sdbdump --sdb <SPEC> [--out syntax]" ;

    private static ModGraph modGraph = new ModGraph() ;
    static ArgDecl argDeclSyntax = new ArgDecl(true, "out") ;

    public static void main(String ... argv)
    {
        new sdbdump(argv).main() ;
    }

    protected sdbdump(String ... args)
    {
        super(args);
        addModule(modGraph) ;
        add(argDeclSyntax) ;
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return Utils.className(this)+" --sdb <SPEC> [--out syntax]" ; }

    @Override
    protected void processModulesAndArgs()
    {
        if ( getNumPositional() > 0 )
            cmdError("No positional arguments allowed", true) ;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void execCmd(List args)
    {
        // This is a streamable syntax.
        String syntax = "N-TRIPLES" ;
        if ( contains(argDeclSyntax) )
            syntax = getArg(argDeclSyntax).getValue() ;
        if ( isDebug() )
            System.out.println("Debug: syntax is "+syntax) ;
        
        try {
            Model model = modGraph.getModel(getStore()) ;
            model.write(System.out, syntax) ;
        } catch (Exception ex)
        {
            System.err.println("Exception: "+ex+" :: "+ex.getMessage()) ;
            ex.printStackTrace(System.err) ;
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
