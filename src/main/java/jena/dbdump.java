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
