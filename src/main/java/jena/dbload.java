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
import jena.util.DBcmd;

import com.hp.hpl.jena.util.FileUtils; 
 
 /** Load data files into a Jena model in a database.
  * 
  *  <p>
  *  Usage:<pre>
  *  jena.dbload [db spec] file [file ...]
  *  where [db spec] is:
  *    --db JDBC_url --dbUser userId --dbPassword password --dbType [--model modelName]  
  *  </pre>
  *  The syntax of a file is determimed by its extension (.n3, .nt) and defaults to RDF/XML. 
  *  </p>
  * 
  * @author Andy Seaborne
  * @version $Id: dbload.java,v 1.1 2009-06-29 08:55:51 castagna Exp $
  */ 
 
public class dbload extends DBcmd
{
    public static final String[] usage = new String[]
    { 
        "dbload [db_description] [--model name] file" ,
        "  where db_description is" ,
        "    --db JDBC URL --dbType type" ,
        "    --dbUser user --dbPassword password" 
     } ;
    
    static {
    	setLog4jConfiguration() ;
    }

    public static void main(String[] args)
    {
        dbload db = new dbload();
        db.setUsage(usage) ;
        
        // add any new args
        db.init(args);
        // do any additional test here

        // Action!
        db.exec();
    }
    
    String filename = null ;

    public dbload()
    {
        super("dbload", true);
    }

    @Override
    protected void exec0() { return ; }

    @Override
    protected boolean exec1(String arg)
    {
        if ( verbose )
            System.out.println("Start load: "+arg) ;
        // Crude but convenient
        if ( arg.indexOf(':') == -1 )
            arg = "file:"+arg ;

        String lang = FileUtils.guessLang(arg) ;
        getRDBModel().read(arg, lang) ;
        return true ;
    }
}
