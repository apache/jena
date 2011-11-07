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

import com.hp.hpl.jena.util.iterator.ClosableIterator;

 
/** List the models available in a database
 *   <p>
 *  Usage:<pre>
 *  jena.dblist [db spec]
 *  where [db spec] is:
 *    --spec file        Contains an RDF description of the model 
 *    --db JDBC_url --dbUser userId --dbPassword password --dbType
 *  </pre>
 *  Ignores any <code>--model modelName</code>.
 *  </p>
 * 
 * @author Andy Seaborne
 * @version $Id: dblist.java,v 1.1 2009-06-29 08:55:51 castagna Exp $
 */ 
 
public class dblist extends DBcmd
{
    public static final String[] usage = new String[]
                                                    { 
            "dblist [db_description] [--model name]" ,
            "  where db_description is" ,
            "    --db JDBC URL --dbType type" ,
            "    --dbUser user --dbPassword password" 
    } ;
    
    static {
    	setLog4jConfiguration() ;
    }
    
    public static void main(String[] args)
    {
        dblist db = new dblist();
        db.setUsage(usage) ;
        db.init(args);
        db.exec();
    }

    public dblist()
    {
        super("dblist", false) ;
    }

    static String defaultModelName = "DEFAULT" ;
    
    @Override
    protected void exec0()
    {
//        if ( getConnection().containsDefaultModel() )
//        {
//            System.out.println("Model: <<default model>>") ;
//            properties(null) ;
//        }
        
        ClosableIterator<String> iter = getConnection().getAllModelNames() ;
        try {
            for ( ; iter.hasNext() ; )
            {
                String name = iter.next() ; 
                System.out.println("Model: "+name) ;
                properties(name) ;
            }
        }
        finally
        {
            iter.close() ;
        }
      
    }
    
    @Override
    protected boolean exec1(String arg) { return true ; } 

    private void properties(String name)
    {
        if ( true )
            return ;        
//        ModelRDB m = ModelRDB.open(getConnection(), name) ;
//        Model props = m.getModelProperties() ;
//        props.setNsPrefix("db", "http://jena.hpl.hp.com/2003/04/DB#") ;
//        props.write(System.out, "N3") ;
//        props.close() ;
    }

}
