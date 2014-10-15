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

package sdb;

import java.util.List ;

import sdb.cmd.CmdArgsDB ;
import arq.cmdline.ArgDecl ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sdb.store.StoreConfig ;
import com.hp.hpl.jena.sparql.util.Utils ;

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
  */ 
 
public class sdbinfo extends CmdArgsDB
{
    static ArgDecl argDeclSyntax = new ArgDecl(true, "out") ;
    String format = "N3" ; 


    public static void main(String ... argv)
    {
        SDB.init();
        new sdbinfo(argv).mainRun() ;
    }
    
    protected sdbinfo(String... args)
    {
        super(args);
        add(argDeclSyntax) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return Utils.className(this)+" --sdb <SPEC>" ; }

    @Override
    protected void processModulesAndArgs()
    {
        if ( contains(argDeclSyntax) )
            format = getValue(argDeclSyntax) ;
        if ( getNumPositional() > 0 )
            cmdError("No positional arguments allowed", true) ;
    }
    
    @Override
    protected void execCmd(List<String> args)
    {
        StoreConfig sConf = getStore().getConfiguration() ;
        if ( sConf == null )
        {
            System.out.println("Configuration is null") ;
            return ;
        }
        Model m = sConf.getModel() ;
        if ( m == null )
            System.out.println("No configuration model") ;
        else            
            m.write(System.out, format) ;
        
        List<String> tableNames = getModStore().getConnection().getTableNames() ;
        for ( String tableName : tableNames )
        {
            System.out.println("Table: "+tableName) ;
        }
        
    }
}
