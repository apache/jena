/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 *
 * Util.java
 *
 * Created on 01 August 2000, 16:31
 */

package com.hp.hpl.jena.rdf.model.impl;
import java.util.regex.*;

import org.apache.xerces.util.XMLChar;

import com.hp.hpl.jena.shared.*;

/** Some utility functions.
 */
public class Util extends Object {

    /** Given an absolute URI, determine the split point between the namespace part
     * and the localname part.
     * If there is no valid localname part then the length of the
     * string is returned.
     * The algorithm tries to find the longest NCName at the end
     * of the uri, not immediately preceeded by the first colon
     * in the string.
     * @param uri
     * @return the index of the first character of the localname
     */
    public static int splitNamespace(String uri) {
        
        // XML Namespaces 1.0:
        // A qname name is NCName ':' NCName
        // NCName             ::=      NCNameStartChar NCNameChar*
        // NCNameChar         ::=      NameChar - ':'
        // NCNameStartChar    ::=      Letter | '_'
        // 
        // XML 1.0
        // NameStartChar      ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] |
        //                        [#xD8-#xF6] | [#xF8-#x2FF] |
        //                        [#x370-#x37D] | [#x37F-#x1FFF] |
        //                        [#x200C-#x200D] | [#x2070-#x218F] |
        //                        [#x2C00-#x2FEF] | [#x3001-#xD7FF] |
        //                        [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
        // NameChar           ::= NameStartChar | "-" | "." | [0-9] | #xB7 |
        //                        [#x0300-#x036F] | [#x203F-#x2040]
        // Name               ::= NameStartChar (NameChar)*
        
        char ch;
        int lg = uri.length();
        if (lg == 0)
            return 0;
        int i = lg-1 ;
        for ( ; i >= 1 ; i--) {
            ch = uri.charAt(i);
            if (notNameChar(ch)) break;
        }
        
        int j = i + 1 ;

        if ( j >= lg )
            return lg ;
        
        // Check we haven't split up a %-encoding.
        if ( j >= 2 && uri.charAt(j-2) == '%' )
            j = j+1 ;
        if ( j >= 1 && uri.charAt(j-1) == '%' )
            j = j+2 ;
        
        // Have found the leftmost NCNameChar from the
        // end of the URI string.
        // Now scan forward for an NCNameStartChar
        // The split must start with NCNameStart.
        for (; j < lg; j++) {
            ch = uri.charAt(j);
//            if (XMLChar.isNCNameStart(ch))
//                break ;
            if (XMLChar.isNCNameStart(ch))
            {
                // "mailto:" is special.
                // Keep part after mailto: at least one charcater.
                // Do a quick test before calling .startsWith
                // OLD: if ( uri.charAt(j - 1) == ':' && uri.lastIndexOf(':', j - 2) == -1)
                if ( j == 7 && uri.startsWith("mailto:"))
                    continue; // split "mailto:me" as "mailto:m" and "e" !
                else
                    break;
            }
        }
        return j;
    }

    /**
	    answer true iff this is not a legal NCName character, ie, is
	    a possible split-point start.
    */
    public static boolean notNameChar( char ch )
        { return !XMLChar.isNCName( ch ); }

    protected static Pattern standardEntities = 
    	   Pattern.compile( "&|<|>|\t|\n|\r|\'|\"" );
    
    public static String substituteStandardEntities( String s )
        {
        if (standardEntities.matcher( s ).find())
            {
            return substituteEntitiesInElementContent( s )
                .replaceAll( "'", "&apos;" )
                .replaceAll( "\t","&#9;" )
                .replaceAll( "\n", "&#xA;" )
                .replaceAll( "\r", "&#xD;" )
                .replaceAll( "\"", "&quot;" )
                ;
            }
        else
            return s;
        }
    
    protected static Pattern entityValueEntities = 
 	   Pattern.compile( "&|%|\'|\"" );
 
   public static String substituteEntitiesInEntityValue( String s )
     {
     if (entityValueEntities.matcher( s ).find())
         {
         return s
             .replaceAll( "&","&amp;" )
             .replaceAll( "'", "&apos;" )
             .replaceAll( "%", "&#37;" )
             .replaceAll( "\"", "&quot;" )
             ;
         }
     else
         return s;
     }
    protected static Pattern elementContentEntities = Pattern.compile( "<|>|&|[\0-\37&&[^\n\t]]|\uFFFF|\uFFFE" );
    /**
        Answer <code>s</code> modified to replace &lt;, &gt;, and &amp; by
        their corresponding entity references. 
        
    <p>
        Implementation note: as a (possibly misguided) performance hack, 
        the obvious cascade of replaceAll calls is replaced by an explicit
        loop that looks for all three special characters at once.
    */
    public static String substituteEntitiesInElementContent( String s ) 
        {
        Matcher m = elementContentEntities.matcher( s );
        if (!m.find())
            return s;
        else
            {
            int start = 0;
            StringBuffer result = new StringBuffer();
            do
                {
                result.append( s.substring( start, m.start() ) );
                char ch = s.charAt( m.start() );
                switch ( ch )
                {
                    case '\r': result.append( "&#xD;" ); break;
                    case '<': result.append( "&lt;" ); break;
                    case '&': result.append( "&amp;" ); break;
                    case '>': result.append( "&gt;" ); break;
                    default: throw new CannotEncodeCharacterException( ch, "XML" );
                }
                start = m.end();
                } while (m.find( start ));
            result.append( s.substring( start ) );
            return result.toString();
            }
        }

    public static String replace(
        String s,
        String oldString,
        String newString) {
        String result = "";
        int length = oldString.length();
        int pos = s.indexOf(oldString);
        int lastPos = 0;
        while (pos >= 0) {
            result = result + s.substring(lastPos, pos) + newString;
            lastPos = pos + length;
            pos = s.indexOf(oldString, lastPos);
        }
        return result + s.substring(lastPos, s.length());
    }

    /** Call System.getProperty and suppresses SecurityException, (simply returns null).
     *@return The property value, or null if none or there is a SecurityException.
     */
    public static String XgetProperty(String p) {
        return XgetProperty( p, null );
    }
    /** Call System.getProperty and suppresses SecurityException, (simply returns null).
     *@return The property value, or null if none or there is a SecurityException.
     */
    public static String XgetProperty(String p, String def) {
        try {
            return System.getProperty(p, def);
        } catch (SecurityException e) {
            return def;
        }
    }

}
