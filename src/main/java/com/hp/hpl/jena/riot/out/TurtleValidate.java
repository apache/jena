/*
 * (c) Copyright 2009 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.out;

import java.text.CharacterIterator ;
import java.text.StringCharacterIterator ;

import com.hp.hpl.jena.riot.RiotException ;

/** Validation of Turtle terms */
public class TurtleValidate
{
    /* Not SPARQL, where internal (not first or last) dots in the local part are legal.
     * 
     */
    
    // Checks of prefixed names
    // These tests must agree, or be more restrictive, than the parser. 
    protected static boolean checkValidPrefixedName(String ns, String local)
    {
        return checkValidPrefixPart(ns) && checkValidNamePart(local) ;
    }
    
    /* http://www.w3.org/TeamSubmission/turtle/#sec-grammar-grammar
     * [27]    qname           ::=     prefixName? ':' name?
     * [30]    nameStartChar   ::=     [A-Z] | "_" | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
     * [31]    nameChar        ::=     nameStartChar | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
     * [32]    name            ::=     nameStartChar nameChar*
     * [33]    prefixName      ::=     ( nameStartChar - '_' ) nameChar*
     */
    
    protected static boolean checkValidPrefixPart(String s)
    {
        if ( s.length() == 0 )
            return true;
        CharacterIterator cIter = new StringCharacterIterator(s) ;
        char ch = cIter.first() ;
        if ( ! checkNameStartChar(ch) )
            return false ;
        if ( ch == '_' )    // Can't start with _ (bnodes labels handled separately) 
            return false ;
        return checkNameTail(cIter) ;
    }

  private static boolean checkValidPrefixName(String prefixedName)
  {
      // Split it to get the parts.
      int i = prefixedName.indexOf(':') ;
      if ( i < 0 )
          throw new RiotException("Broken short form -- "+prefixedName) ;
      String p = prefixedName.substring(0,i) ;
      String x = prefixedName.substring(i+1) ; 
      // Check legality
      if ( checkValidPrefixedName(p, x) )
          return true ;
      return false ;
  }
    
    protected static boolean checkValidNamePart(String s)
    {
        if ( s.length() == 0 )
            return true; 
        CharacterIterator cIter = new StringCharacterIterator(s) ;
        char ch = cIter.first() ;
        if ( ! checkNameStartChar(ch) )
            return false ;
        return checkNameTail(cIter) ;
    }
    
//  private static boolean checkValidLocalname(String localname)
//  {
//      if ( localname.length() == 0 )
//          return true ;
//      
//      for ( int idx = 0 ; idx < localname.length() ; idx++ )
//      {
//          char ch = localname.charAt(idx) ;
//          if ( ! validPNameChar(ch) )
//              return false ;
//      }
//      
//      // Test start and end - at least one character in the name.
//      
//      if ( localname.endsWith(".") )
//          return false ;
//      if ( localname.startsWith(".") )
//          return false ;
//      
//      return true ;
//  }
//  
//  private static boolean validPNameChar(char ch)
//  {
//      if ( Character.isLetterOrDigit(ch) ) return true ;
//      if ( ch == '.' )    return true ;
//      if ( ch == '-' )    return true ;
//      if ( ch == '_' )    return true ;
//      return false ;
//  }

    
    private static boolean checkNameTail(CharacterIterator cIter)
    {
        // Assumes cIter.first already called but nothing else.
        // Skip first char.
        char ch = cIter.next() ;
        for ( ; ch != java.text.CharacterIterator.DONE ; ch = cIter.next() )
        {
            if ( ! checkNameChar(ch) )
                return false ;
        } 
        return true ;
    }

    protected static boolean checkNameStartChar(char ch)
    {
        if ( Character.isLetter(ch) )
            return true ;
        if ( ch == '_' )
            return true ;
        return false ;
    }

    // Dotted parts for SPARQL?
    protected static boolean checkNameChar(char ch)
    {
        if ( Character.isLetterOrDigit(ch) )
            return true ;
        if ( ch == '_' )
            return true ;
        if ( ch == '-' )
            return true ;
        return false ;
    }

    
    

}

/*
 * (c) Copyright 2009 Talis Information Ltd.
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