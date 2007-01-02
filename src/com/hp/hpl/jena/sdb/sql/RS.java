/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;

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
    
    public static void close(ResultSet rs) 
    {
        try {
            Statement s = rs.getStatement() ;
            if ( s != null )
                s.close() ;
            else
                rs.close() ;
        } catch (SQLException ex)
        {
            LogFactory.getLog(RS.class).warn("Problems closing result set : "+ex.getMessage()) ;
        }
        
    }

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

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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