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

import java.sql.Connection;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;
import sdb.cmd.CmdArgsDB;
import sdb.junit.TextListener2;
import sdb.test.Params;
import sdb.test.ParamsVocab;
import sdb.test.TestI18N;
import sdb.test.TestStringBasic;
import arq.cmd.CmdException;

import com.hp.hpl.jena.sparql.util.Utils;

/** Run some DB tests to check setup */ 

public class DBTest extends CmdArgsDB 
{
    
    public static void main(String [] argv)
    {
        new DBTest(argv).mainAndExit() ;
    }
    
    String filename = null ;

    public DBTest(String[] args)
    {
        super(args);
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> "; }
    
    @Override
    protected void processModulesAndArgs()
    {
        List<String> args = getPositional() ;
        setParams(args) ;
    }
    
    Params params = new Params() ;
    {
        params.put( ParamsVocab.TempTableName,    "FOO") ;

        params.put( ParamsVocab.BinaryType,       "BLOB") ;
        params.put( ParamsVocab.BinaryCol,        "colBinary") ;
        
        params.put( ParamsVocab.VarcharType,      "VARCHAR(200)") ;
        params.put( ParamsVocab.VarcharCol,       "colVarchar") ;
    }
    
    @Override
    protected void execCmd(List<String> args)
    {
        if ( isVerbose() )
        {
            for ( String k : params )
                System.out.printf("%-20s = %-20s\n", k, params.get(k) );
            System.out.println() ;
        }
        
        Connection jdbc = getModStore().getConnection().getSqlConnection() ;
        // Hack to pass to calculated parameters to the test subsystem.

        sdb.test.Env.set(jdbc, params, false) ;
        
        JUnitCore x = new org.junit.runner.JUnitCore() ;
        //RunListener listener = new TextListener2() ;
        RunListener listener = new TextListener2(System.out) ;
        x.addListener(listener) ;
        
        //x.run(sdb.test.AllTests.class) ;
        System.out.println("String basic") ;
        x.run(TestStringBasic.class) ;
        
        System.out.println("String I18N") ;
        x.run(TestI18N.class) ;

        // Better way of having parameters for a class than a @Parameterised test of one thing?
//        Request request = Request.aClass(sdb.test.T.class) ;
//        x.run(request) ;
    }
    
    private void setParams(List<String> args)
    {
        for ( String s : args )
        {
            String[] frags = s.split("=", 2) ;
            if ( frags.length != 2)
                throw new CmdException("Can't split '"+s+"'") ;
            params.put(frags[0], frags[1] ) ;
        }
    }
    
}
