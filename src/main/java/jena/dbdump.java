/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package jena;
import static jena.cmdline.CmdLineUtils.setLog4jConfiguration;
import jena.cmdline.* ;
import jena.util.DBcmd;

 /** Write out the data from a database.
  *  Currently broken <code>:-(</code>
  * 
  *  <p>
  *  Usage:<pre>
  *  jena.dbdump [db spec]
  *  where [db spec] is:
  *    --spec file        Contains an RDF description of the model 
  *    --db JDBC_url --dbUser userId --dbPassword password --dbType [--model modelName]  
  *  </pre>
  *  </p>

  * 
  * @author Andy Seaborne
  * @version $Id: dbdump.java,v 1.1 2009-06-29 08:55:51 castagna Exp $
  */ 
 
public class dbdump extends DBcmd
{
    public static final String[] usage = new String[]
    { 
        "dbdump [db_description] [--model name] [--format syntax]" ,
        "  where db_description is" ,
        "    --db JDBC URL --dbType type" ,
        "    --dbUser user --dbPassword password" 
     } ;

    static ArgDecl argDeclFormat = new ArgDecl(true, "format","fmt") ;

    static {
    	setLog4jConfiguration() ;
    }

    public static void main(String[] args)
    {
        dbdump db = new dbdump();
        db.setUsage(usage) ;
        db.getCommandLine().add(argDeclFormat) ;
        
        // add any new args
        db.init(args);
        // do any additional test here

        // Action!
        db.exec();
    }
    
    String filename = null ;

    public dbdump()
    {
        super("dbdump", false);
    }

    @Override
    protected void exec0()
    {
        // This is a streaming syntax.
        String syntax = "N-TRIPLES" ;
        if ( getCommandLine().contains(argDeclFormat) )
            syntax = getCommandLine().getArg(argDeclFormat).getValue() ;
        if ( debug )
            System.out.println("Debug: syntax is "+syntax) ;
        
        try {
            getRDBModel().write(System.out, syntax) ;
        } catch (Exception ex)
        {
            System.err.println("Exception: "+ex+" :: "+ex.getMessage()) ;
            ex.printStackTrace(System.out) ;
        }
        
    }

    @Override
    protected boolean exec1(String arg) { return false ; }
}
 


/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
