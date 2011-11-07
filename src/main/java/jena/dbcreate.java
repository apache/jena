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

import com.hp.hpl.jena.rdf.model.* ;
//import com.hp.hpl.jena.db.* ;
//import com.hp.hpl.jena.util.iterator.* ;
//import java.util.* ;
 
 /** Create one Jena RDF model in a database.
  *  <p>
  *  Usage:<pre>
  *  jena.dbcreate [db spec]
  *  where [db spec] is:
  *    --spec file        Contains an RDF description of the model 
  *    --db JDBC_url --dbUser userId --dbPassword password --dbType [--model modelName]  
  *  </pre>
  *  </p>
  * 
  * @author Andy Seaborne
  * @version $Id: dbcreate.java,v 1.1 2009-06-29 08:55:51 castagna Exp $
  */ 
 
public class dbcreate extends DBcmd
{
    public static final String[] usage = new String[]
    { 
        "dbcreate [db_description] [--model name]" ,
        "  where db_description is" ,
        "    --db JDBC URL --dbType type" ,
        "    --dbUser user --dbPassword password",
        "    --driver classname"
     } ;

    static {
    	setLog4jConfiguration() ;
    }

    public static void main(String[] args)
    {
        dbcreate db = new dbcreate();
        db.setUsage(usage) ;
        db.init(args);
        db.exec();
    }

    public dbcreate()
    {
        super("dbcreate", false);
    }

    @Override
    protected void exec0()
    {
        Model m = null;

        ModelMaker maker = ModelFactory.createModelRDBMaker(getConnection());
        if (super.argModelName == null)
        {
            System.out.println("Create default model");
            m = maker.createDefaultModel();  
        }  
        else
        {
            System.out.println("Create named model: " + argModelName);
            m = maker.createModel(argModelName);
        }
        
    }
    
    @Override
    protected boolean exec1(String arg) { return false ; } 
}
