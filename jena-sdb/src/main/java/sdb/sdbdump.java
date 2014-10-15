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

import java.util.List;

import sdb.cmd.CmdArgsDB;
import sdb.cmd.ModGraph;
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDB ;
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
  */ 
 
public class sdbdump extends CmdArgsDB
{
    public static final String usage = "sdbdump --sdb <SPEC> [--out syntax]" ;

    private static ModGraph modGraph = new ModGraph() ;
    static ArgDecl argDeclSyntax = new ArgDecl(true, "out") ;

    public static void main(String ... argv)
    {
        SDB.init();
        new sdbdump(argv).mainRun() ;
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
    
    @Override
    protected void execCmd(List<String> args)
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
