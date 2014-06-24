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

package com.hp.hpl.jena.n3 ;

import java.io.* ;
import java.util.* ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TupleSet implements Iterator<List<TupleItem>>
{
    BufferedReader in ;
    public String line = null ;
    public int lineNumber = 0 ;

    static final char COMMENTCHAR = '#' ;
    List<TupleItem> current = null ;
    boolean finished = false ;

    protected static Logger logger = LoggerFactory.getLogger( TupleSet.class );
    
    /** Creates new TupleSet */
    public TupleSet(Reader r)
    {
        if ( ! ( r instanceof BufferedReader ) )
            in = new BufferedReader(r) ;
        else
            in = (BufferedReader)r;
    }

    @Override
    public boolean hasNext()
    {
        if ( finished ) return false ;

        if ( current == null )
            current = tuple() ;
        return current != null ;
    }

    @Override
    public List<TupleItem> next()
    {
        if ( hasNext() )
        {
            List<TupleItem> x = current ;
            current = null ;
            return x ;
        }
        else
            return null ;
    }


    @Override
    public void remove()
    {
        throw new java.lang.UnsupportedOperationException("TupleSet.remove") ;
    }

    private List<TupleItem> tuple()
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
        List<TupleItem> tuple = new ArrayList<>() ;
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
            logger.error( "Error in TupleSet.tuple: " + line );
            
            String s = "" ;
            int k = 0 ;
            for ( ; k < i ; k++ ) s = s+" " ;
            s = s+"^" ;
            for ( ; k < j-1 ; k++ ) s=s+" " ;
            s = s+"^" ;
            logger.error( s ) ;
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
