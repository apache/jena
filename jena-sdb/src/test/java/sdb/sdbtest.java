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

import junit.framework.TestSuite ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sdb.SDB ;
import org.apache.jena.sdb.test.junit.QueryTestSDBFactory ;
import sdb.cmd.CmdArgsDB ;
 
 /** Run a test suite
  * 
  *  <p>
  *  Usage:<pre>
  *  sdb.sdbtest [db spec] [ manifest ]
  *  </pre>
  *  </p>
  */ 
 
public class sdbtest extends CmdArgsDB
{
    public static final String usage = "sdbtest --sdb <SPEC> manifest" ;
    
    public static void main (String... argv)
    {
        new sdbtest(argv).mainRun() ;
    }
    
    String filename = null ;

    protected sdbtest(String... args)
    {
        super(args);
    }

    @Override
    protected String getCommandName() { return Lib.className(this) ; }
    
    @Override
    protected String getSummary()  { return Lib.className(this)+" <SPEC> manifest" ; }
    
    @Override
    protected void processModulesAndArgs() {
        if ( getPositional().size() == 0 )
            cmdError("No manifest to run");
    }

    @Override
    protected void execCmd(List<String> positionalArgs) {
        for ( String x : positionalArgs ) {
            execOneManifest(x);
        }
    }
    
    private void execOneManifest(String manifest)
    { 
        if ( isVerbose() )
        {
            //SchemaBase.printBlock = true ;
            //SchemaBase.printAbstractSQL = true ;
            System.out.println("Manifest: "+manifest) ;
        }
        
        TestSuite ts = new TestSuite() ;
        ts.addTest(QueryTestSDBFactory.make(super.getStoreDesc(), manifest, null)) ;
        
        if ( true )
            // PostgreSQL gets upset with comments in comments??
            ARQ.getContext().setFalse(SDB.annotateGeneratedSQL) ;
        
        org.apache.jena.sdb.test.junit2.SimpleTestRunner.runAndReport(ts) ;
    }
}
