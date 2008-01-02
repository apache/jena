/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dbtest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;

/** This program tests a database setup for use with Jena - it tests the JDBC
 * connection and the databse setup for internationalized usage by Jena.
 * It does notrun the RDF tests. 
 * 
 * @author Andy Seaborne
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
            String k = (String)iter.next() ;
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
        System.out.println("    one of HSQL, MySQL, PostgreSQL, Derby, Oracle, MS-SQL") ;
        System.out.println("  URL is the JDBC URL to connect to the database") ;
        System.out.println("  USER and PASSWORD are the access details.") ;
        System.out.println("Requires table ceration rights on the database") ; 
    }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */