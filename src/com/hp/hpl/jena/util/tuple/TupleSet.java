/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */
 
package com.hp.hpl.jena.util.tuple ;

import java.io.* ;
import java.util.* ;
import com.hp.hpl.jena.util.Log ;

/**
 * @author  Andy Seaborne
 * @version $Id: TupleSet.java,v 1.2 2003-03-10 09:50:43 andy_seaborne Exp $
 */

public class TupleSet implements Iterator
{
    BufferedReader in ;
    public String line = null ;
    public int lineNumber = 0 ;

    static final char COMMENTCHAR = '#' ;
    List current = null ;
    boolean finished = false ;

    /** Creates new TupleSet */
    public TupleSet(Reader r)
    {
        if ( ! ( r instanceof BufferedReader ) )
            in = new BufferedReader(r) ;
        else
            in = (BufferedReader)r;
    }

    public boolean hasNext()
    {
        if ( finished ) return false ;

        if ( current == null )
            current = tuple() ;
        return current != null ;
    }

    public Object next()
    {
        if ( hasNext() )
        {
            List x = current ;
            current = null ;
            return x ;
        }
        else
            return null ;
    }


    public void remove()
    {
        throw new java.lang.UnsupportedOperationException("TupleSet.remove") ;
    }

    private List tuple()
    {

        try {
            lineNumber ++ ;
            line = in.readLine() ;
        } catch (IOException e) {}

        if ( line == null )
        {
            finished = true ;
            return null ;
        }

        //System.out.println("Line: "+line) ;
        List tuple = new ArrayList() ;
        int i = 0 ;
        int j = 0 ;
        boolean errorFound = false ;

     tupleLoop:
        for (;;)
        {
            // Move to beginning of next item.
            i = skipwhitespace(line, j) ;

            if ( i < 0 )
                break ;

            int iStart = -2 ;       // Points to the beginning of the item as found
            int jStart = -2 ;       // Points to the item without quotes
            int iFinish = -2 ;      // Points after the end of the item as found
            int jFinish = -2 ;      // Points after the end of the item without quotes
            int dtStart = -2 ;      // Points to start of datatype (after < quote)
            int dtFinish = -2 ;     // Points to end of datatype
            int type = TupleItem.UNKNOWN;

            switch (line.charAt(i))
            {
                case COMMENTCHAR:
                    break tupleLoop ;
                case '<':
                    type = TupleItem.URI ;
                    iStart = i ;
                    jStart = i+1 ;
                    int newPosn = parseURI(i, line) ;
                    if (newPosn < 0)
                    {
                        errorFound = true;
                        break tupleLoop;
                    }
                    j = newPosn ;
                    
                    iFinish = j+1 ;
                    jFinish = j ;
                    break ;
                case '"':
                    type = TupleItem.STRING ;
                    iStart = i ;
                    jStart = i+1 ;
                    boolean inEscape = false ;
                    for ( j = i+1 ; j < line.length() ; j++ )
                    {
                        char ch = line.charAt(j) ;
                        if ( inEscape )
                        {
                        	// ToDo: escape
                            inEscape = false ;
                            continue ;
                        }
                        // Not an escape
                        if ( ch == '"'  )
                            break ;

                        if ( ch == '\\' )
                            inEscape = true ;
                        if ( ch == '\n' || ch == '\r' )
                        {
                            errorFound = true ;
                            break tupleLoop;
                            
                        }
                    }
                    
                    // Malformed
                    if ( j == line.length() )
                    {
                        errorFound = true ;
                        break tupleLoop;
                    }
                    
                    iFinish = j+1 ;
                    jFinish = j ;
                    // RDF literals may be followed by their type.
                         
                    if ( j < line.length()-3 
                         && line.charAt(j+1) == '^'
                         && line.charAt(j+2) == '^'
                         && line.charAt(j+3) == '<' )
                    {
                        dtFinish = parseURI(j+3, line) ;
                        dtStart = j+4 ;
                        if (dtFinish < 0)
                        {
                            errorFound = true;
                            break tupleLoop;
                        }
                        j = dtFinish+1 ;
                        //String dt = line.substring(dtStart, dtFinish) ;
                        //System.out.println("I see a datatype:"+dt) ;
                    }
                    
                    break ;
                case '_':
                    type = TupleItem.ANON ;
                    iStart = i ;
                    for ( j = i+1 ; j < line.length() ; j++ )
                    {
                        char ch = line.charAt(j) ;
                        if ( ch == ' '  || ch == '\t' || ch == '.' )
                            break ;
                        if ( ! Character.isLetterOrDigit(ch) && ! (ch == '_') && ! (ch == ':') )
                        {
                            errorFound = true ;
                            break tupleLoop ;
                        }
                    }
                    iFinish = j ;
                    jStart = iStart ;
                    jFinish = iFinish ;
                    break ;
                case '.':
                case '\n':
                case '\r':
                    return tuple ;
                default:
                    type = TupleItem.UNQUOTED ;
                    iStart = i ;
                    jStart = i ;
                    for ( j = i+1 ; j < line.length() ; j++ )
                    {
                        char ch = line.charAt(j) ;
                        if ( ch == ' '  || ch == '\t' || ch == '.' )
                            break ;

                        //if ( ! Character.isLetterOrDigit(line.charAt(i)) )
                        //{
                        //    errorFound = true ;
                        //    break tupleLoop;
                        //}
                    }
                    // Malformed
                    if ( j == line.length()+1 )
                    {
                        errorFound = true ;
                        break tupleLoop;
                    }
                    iFinish = j ;
                    jFinish = j ;
                    break ;
            }
            String item = line.substring(jStart, jFinish) ;
            String literal = line.substring(iStart, iFinish) ;
            String dt = null ;
            if ( dtStart > 0 )
                dt = line.substring(dtStart, dtFinish) ;
            
            tuple.add(new TupleItem(item, literal, type, dt)) ;
            j++ ;
            // End of item.
        }
        //End of this line.
        if ( errorFound )
        {
            Log.severe("Error", "TupleSet", "reader") ;
            Log.severe(line) ;
            
            String s = "" ;
            int k = 0 ;
            for ( ; k < i ; k++ ) s = s+" " ;
            s = s+"^" ;
            for ( ; k < j-1 ; k++ ) s=s+" " ;
            s = s+"^" ;
            Log.severe(s) ;
            return null ;
        }

        if ( tuple.size() == 0 )
        {
            // Nothing found : loop by tail recursion
            return tuple() ;
        }
        return tuple ;
    }

    private int skipwhitespace(String s, int i)
    {
        for ( ; i < s.length() ; i++ )
        {
            char ch = s.charAt(i) ;
            // Horizonal whitespace
            if ( ch != ' ' && ch != '\t' )
                return i ;
        }
        return -1 ;
    }

    private int parseURI(int i, String line)
    {
        int j;
        for (j = i + 1; j < line.length(); j++)
        {
            char ch = line.charAt(j);
            if (ch == '>')
                break;
            if (ch == '\n' || ch == '\r')
                return -1;
        }
        // Malformed
        if (j == line.length())
            return -2;
        return j ;
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001
 *  All rights reserved.
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
