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

package com.hp.hpl.jena.sdb.sql;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** SQL Utilities */

public class RS
{
    // Three pass is a bit excessive but its clear and pass 2 is cheap.
    // 1 - col to strings.
    // 2 - string to widths
    // 3 - output 
    
    
    static String lead = "| " ;
    static String sep = " | " ;
    static String trail = "| " ;
    
    public static void close(ResultSetJDBC rs)
    {
        if ( rs != null )
            rs.close();
    }
    

    public static void close(Statement s )
    {
        try { if ( s != null ) s.close() ; } catch (SQLException ex) {}
    }

//    public static void close(ResultSet rs) 
//    {
//        try {
//            Statement s = rs.getStatement() ;
//            if ( s != null )
//                s.close() ;
//            else
//                rs.close() ;
//        } catch (SQLException ex)
//        {
//            LoggerFactory.getLogger(RS.class).warn("Problems closing result set : "+ex.getMessage()) ;
//        }
//        
//    }

    public static void consume(ResultSet resultSet) throws SQLException
    {
        while ( resultSet.next() )
        {
        }
        resultSet.close() ;
    }
    
    public static void printResultSet(ResultSet resultSet) throws SQLException
    { printResultSet(System.out, resultSet) ; }

    
    public static void printResultSet(PrintStream out, ResultSet resultSet) throws SQLException
    {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        List<List<String>> rows = pass0(resultSet) ;
        
        int[] colWidths = pass1(rsmd, rows) ;
        
        fmtRow(out, rsmd, "+-", "---", '-', "-+", colWidths) ;

        out.print("| ") ;
        for ( int i = 1; i <= rsmd.getColumnCount() ; i++)
        {
            if ( i != 1 )
                out.print(" | ") ;
            fmt(out, rsmd.getColumnLabel(i), colWidths[i]) ;
        }
        out.print(" |") ;
        out.println() ;

        fmtRow(out, rsmd, "| ", " | ", '=', " |", colWidths) ;
        
        for ( List<String> x : rows )
        {
            out.print("| ") ;
            for ( int i = 1; i <= rsmd.getColumnCount() ; i++)
            {
                if ( i != 1 )
                    out.print(sep) ;
                String s = x.get(i) ;
                fmt(out, s, colWidths[i]) ;
            }
            out.print(" |") ;
            out.println() ;
        }
        fmtRow(out, rsmd, "+-", "---", '-', "-+", colWidths) ;

    }

    static void fmtRow(PrintStream out, ResultSetMetaData rsmd, String lead, String sep, char pad, String trail, int []colWidths)
        throws SQLException
    {
        out.print(lead) ;
        for ( int i = 1; i <= rsmd.getColumnCount() ; i++)
        {
            if ( i != 1 )
                out.print(sep) ;
            fmt(out, "", colWidths[i], pad) ;
        }
        out.print(trail) ;
        out.println() ;
        
    }
    
    // Column => String.
    static List<List<String>> pass0(ResultSet resultSet) throws SQLException
    {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int N = rsmd.getColumnCount() ;
        List<List<String>> x = new ArrayList<List<String>>() ; 

        while ( resultSet.next() )
        {
            List<String> z = new ArrayList<String>() ;
            z.add("NULL") ;
            x.add(z) ;
            for ( int i = 1; i <= N ; i++)
            {
                String s = colAsString(resultSet, i) ;
                z.add(s) ;
            }
        }
        return x ;
    }

    
    // Strings to widths.
    static int[] pass1(ResultSetMetaData rsmd, List<List<String>> rows) throws SQLException
    {
        // zero is blank.
        int col[] = new int[rsmd.getColumnCount()+1] ;
        col[0] = -1 ;

        for ( int i = 1; i <= rsmd.getColumnCount() ; i++)
        {
            int w = rsmd.getColumnLabel(i).length() ;
            col[i] = w ;
        }
        
        for ( List<String> x : rows )
        {
            int i = 0 ;
            for ( String z : x )
            {
                if ( z == null )
                    continue ;
                if ( z.length() > col[i] )
                    col[i] = z.length() ;
                i++ ;
            }
        }
        return col ;
    }
    
    // Make an interface sometime
    static String colAsString(ResultSet resultSet, int i) throws SQLException
    {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        String str = null ;

        switch (rsmd.getColumnType(i))
        {
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CHAR:
                str = "\""+resultSet.getString(i)+"\"" ;
                break ;
            case Types.INTEGER:
                //case Types.BIGINT:
                str = Integer.toString(resultSet.getInt(i)) ;
                break ;
            case Types.DOUBLE:
                str = Double.toString(resultSet.getDouble(i)) ;
                break ;
            case Types.FLOAT:
                str = Float.toString(resultSet.getFloat(i)) ;
                break ;
            case Types.BLOB:
                byte[] b2 = resultSet.getBytes(1) ;
                try { str = new String(b2, 0, b2.length, "UTF-8") ; }
                catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }
                break ;
            default:
                str = resultSet.getString(i) ;
        }
        if ( resultSet.wasNull() )
            str = "<null>" ;
        return str ;
    }

//    static void fmt(String s, int w, char pad)
//    { fmt(System.out, s, w, pad) ; }
//    
//    
//    static void fmt(String s, int w)
//    { fmt(System.out, s, w, ' ' ) ; }
    
    static void fmt(PrintStream out, String s, int w)
    { fmt(out, s, w, ' ' ) ; }
    
    static void fmt(PrintStream out, String s, int w, char pad)
    {
        if ( s == null )
            s = "NULL" ;
        for ( int i = 0 ; i < (w-s.length()) ; i++ )
            out.print(pad) ;
        out.print(s) ;
    }

}
