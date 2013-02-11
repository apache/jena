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
import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDB ;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.store.StoreBaseHSQL;
import com.hp.hpl.jena.sparql.util.Utils;
 
 /** Delete a model in an SDB database.
  * 
  *  <p>
  *  Usage:<pre>
  *    sdbdelete [db spec] default | graph iri [...]
  *  </pre>
  *  </p>
  */ 
 
public class sdbdelete extends CmdArgsDB
{
    private static final String usage = "sdbdelete --sdb <SPEC> default | <IRI> ..." ;
    
    private static ArgDecl argDeclConfirm  = new ArgDecl(false,  "confirm", "force") ;
    
    public static void main(String... argv)
    {
        SDB.init();
        new sdbdelete(argv).mainRun() ;
    }
    
    String filename = null ;

    public sdbdelete(String... args)
    {
        super(args);
        add(argDeclConfirm) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> default | <IRI> ..."; }
    
    @Override
    protected void processModulesAndArgs()
    {
        if ( getNumPositional() == 0 )
            cmdError("Need IRIs of graphs to delete", true) ;
    }
    
    @Override
    protected void execCmd(List<String> args)
    {
        //if ( contains(argDeclTruncate) )
        //    getStore().getTableFormatter().truncate() ;
        for ( String x : args )
            removeOne(x) ;
        StoreBaseHSQL.close(getStore()) ;
    }
    
    private void removeOne(String IRI)
    {
        boolean removeDefault = "default".equals(IRI);

        if (isVerbose()) {
            if (removeDefault) System.out.println("Removing default graph");
            else System.out.println("Removing graph named <" + IRI + ">");
        }

        Model model = (removeDefault) ?
            SDBFactory.connectDefaultModel(getStore()) :
            SDBFactory.connectNamedModel(getStore(), IRI) ;

        model.removeAll();
    }
}
