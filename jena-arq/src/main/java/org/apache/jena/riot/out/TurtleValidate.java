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

package org.apache.jena.riot.out;

import java.text.CharacterIterator ;
import java.text.StringCharacterIterator ;

import org.apache.jena.riot.RiotException ;


/** Validation of Turtle terms */
public class TurtleValidate
{
    // Not SPARQL, where internal (not first or last) dots in the local part are legal.
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
