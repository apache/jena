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

package dbtest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;

/** This program tests a database setup for use with Jena - it tests the JDBC
 * connection and the databse setup for internationalized usage by Jena.
 * It does notrun the RDF tests. 
 */

public class dbsetuptest
{
    //Yet to do: Tests for text type long literals (e.g. PostgreSQL)
    static Params params = new Params() ; 
    static Connection jdbc ;  
    
    public static void main(String[] argv)
    {
        if ( argv.length > 0 )
        {
            String s = argv[0] ;
            if ( s.startsWith("-") ) s = s.substring(1) ;
            if ( s.startsWith("-") ) s = s.substring(1) ;
            if ( s.equalsIgnoreCase("h") ||  s.equalsIgnoreCase("help") )
            {
                usage() ;
                return ;
            }
        }
        setParams(argv) ;
        
        System.out.println("JDBC and DB config tests for Jena") ;
        System.out.println("(low-level test of DB and its JDBC configuration - not RDF tests)") ;
        System.out.println() ;
        
        for ( Iterator<String> iter = params.iterator() ; iter.hasNext() ; )
        {
            String k = iter.next() ;
            String v = params.get(k) ;
            System.out.println(k+" = "+v) ;
        }
        
        makeConnection() ;
        System.out.println() ;
        junit.textui.TestRunner.run(TestDB.suite(jdbc, params)) ;
        
        DB.execNoFail(jdbc, "DROP TABLE "+params.get(ParamsVocab.TempTableName)) ;
    }
 
    private static void setParams(String[] args)
    {
        //for ( Iterator iter = args.iterator() ; iter.hasNext() ; )
        for ( int i = 0 ; i < args.length ; i++ )
        {
            //String s = (String)iter.next() ;
            String s = args[i] ;
            String[] frags = s.split("=", 2) ;
            if ( frags.length != 2)
                throw new RuntimeException("Can't split '"+s+"'") ;
            params.put(frags[0], frags[1] ) ;
        }
        
        if ( params.get(ParamsVocab.DBType) == null )
        {
            System.err.println("No DB type given") ;
            System.exit(1) ;
        }
        
        Setup.setParams(params, params.get(ParamsVocab.DBType)) ;
        
    }
    
    private static void makeConnection()
    {
        try {
            Class.forName(params.get(ParamsVocab.Driver)) ;
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
            System.exit(9) ;
        }
        String url = params.get(ParamsVocab.JDBC) ;
        String user = params.get(ParamsVocab.User) ;
        String password = params.get(ParamsVocab.Password) ;
        try
        {
            jdbc = DriverManager.getConnection(url, user, password) ;
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            System.exit(8) ;
        }
    }
    
    public static void usage()
    {
        System.out.println("Usage: db=TYPE jdbc=URL user=USER password=PASSWORD") ;
        System.out.println("  where TYPE is the database type") ;
        System.out.println("    one of HSQL, MySQL, PostgreSQL, Derby, Oracle, MS-SQL, SAP") ;
        System.out.println("  URL is the JDBC URL to connect to the database") ;
        System.out.println("  USER and PASSWORD are the access details.") ;
        System.out.println("Requires table ceration rights on the database") ; 
    }
}
