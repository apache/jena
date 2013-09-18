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

package com.hp.hpl.jena.sparql.util;

import java.util.Locale ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

/**
 * References:
 * <ul>
 *  <li><a href="http://en.wikipedia.org/wiki/Roman_numbers">Wikipedia on Roman Numerals</a></li>
 *  <li><a href="http://www.therobs.com/uman/roman.shtml">Therobs Lex &amp; Yacc Example: Roman Numerals</a>
 *  which is were the idea of working right to left, instead of looking ahead, originated for me.</li>
 * </ul> */

public class RomanNumeral
{
    public static void main(String[] args)
    {
        roman("I") ;
        roman("IV") ;
        roman("IIII") ;
        roman("IIIII") ;
        roman("IIIIV") ;
        roman("XL") ;
        roman("XD") ;
        roman("XMIX") ;
        roman("MIM") ;
        roman("MCMXCIX") ;
    }
    public static void roman(String lex)
    {
        int i = r2i(lex) ;
        System.out.print(lex+" ==> "+i+" ==> "+i2r(i)+" ==> "+r2i(i2r(i)) ) ;
        System.out.print("  Valid: "+isValid(lex)) ;
        System.out.println() ;
    }

    int intValue ;
    
    public RomanNumeral(String lexicalForm)
    { 
        if ( ! isValid(lexicalForm) )
            throw new NumberFormatException("Invalid Roman Numeral: "+lexicalForm) ;
        intValue = r2i(lexicalForm) ;
    }

    public RomanNumeral(int i)
    {
        if ( i <= 0 )
            throw new NumberFormatException("Roman numerals are 1-3999 ("+i+")") ;
        if ( i > 3999 )
            throw new NumberFormatException("Roman numerals are 1-3999 ("+i+")") ;
        intValue = i ;
    }
    
    @Override
    public String toString() { return i2r(intValue) ; }
    
    public int intValue() { return intValue ; }
    
    
    //String pat = "M*(CM|DC{0,3}|CD|C{0,3})(XC|LX{0,3}|XL|X{0,3})(IX|VI{0,3}|IV|I{0,3})" ;
    // Added I{0,4}
    static String numeralPattern = "M*(CM|DC{0,3}|CD|C{0,3})(XC|LX{0,3}|XL|X{0,3})(IX|VI{0,3}|IV|I{0,4})" ;
    static Pattern pattern = Pattern.compile(numeralPattern) ;
    
    public static boolean isValid(String lex)
    {
        lex = lex.toUpperCase(Locale.ENGLISH) ;
        // Excludes IIII
        Matcher m = pattern.matcher(lex);
        return m.matches() ;
    }
    
    public static int parse(String lex) { return r2i(lex) ; }
    // It is easier working right to left!
    public static int r2i(String lex)
    {
        lex = lex.toUpperCase(Locale.ROOT) ;

        // This is overly permissive.
        // 1 - allows multiple reducing values
        // 2 - allows reducing values that are not 10^x in front of 5*10^x or 10^(x+1)
        // Use the validator.
        int current = 0 ;
        int v = 0 ;
        for ( int i = lex.length()-1 ; i >= 0 ; i-- )
        {
            char ch = lex.charAt(i) ;
            int x = charToNum(ch) ;
            if ( x < current )
                v = v-x ;
            else
            {
                v = v+x ;
                current = x ;
            }
        }
        return v ;
    }

    public static String asRomanNumerals(int i) { return i2r(i) ; }
    public static String i2r(int i)
    {
        if ( i <= 0 )
            throw new NumberFormatException("Roman numerals are 1-3999 ("+i+")") ;
        if ( i > 3999 )
            throw new NumberFormatException("Roman numerals are 1-3999 ("+i+")") ;
        StringBuffer sbuff = new StringBuffer() ;
        
        i = i2r(sbuff, i, "M", 1000, "CM", 900, "D", 500, "CD", 400 ) ;
        i = i2r(sbuff, i, "C", 100,  "XC", 90,  "L", 50,  "XL", 40 ) ;
        i = i2r(sbuff, i, "X", 10,   "IX", 9,   "V", 5,   "IV", 4) ;
        
        while ( i >= 1 )
        {
            sbuff.append("I") ;
            i -= 1 ;
        }
        return sbuff.toString() ;
            
        
    }
    
    private static int i2r(StringBuffer sbuff, int i,
                           String tens,  int iTens, 
                           String nines, int iNines,
                           String fives, int iFives,
                           String fours, int iFours)
    {
        while ( i >= iTens )
        {
            sbuff.append(tens) ;
            i -= iTens ;
        }
        
        if ( i >= iNines )
        {
            sbuff.append(nines) ;
            i -= iNines;
        }

        if ( i >= iFives )
        {
            sbuff.append(fives) ;
            i -= iFives ;
        }
        if ( i >= iFours )
        {
            sbuff.append(fours) ;
            i -= iFours ;
        }
        return i ;
    }
   
    // Only subtract ten's C,X,I
    // Only allow one of them
    // One do 10^x from 10^(x+1)
    // CM, CD, XC, XL, IX, IV
    
    static private int charToNum(char ch)
    {
        if ( ch == 0 ) return 0 ;
        for ( int i = 0 ; i < RValue.table.length ; i++ )
        {
            if ( RValue.table[i].lex == ch )
                return RValue.table[i].val ;
        }
        return 0 ;
    }
}

class RValue
{
    static RValue[] table =
        new RValue[] { new RValue('M', 1000) ,
                       new RValue('D', 500) ,
                       new RValue('C', 100) ,
                       new RValue('L', 50) ,        
                       new RValue('X', 10) ,
                       new RValue('V', 5) ,
                       new RValue('I', 1) } ;

    char lex ; int val ;
    RValue(char s, int v) { lex = s ; val = v ; } 

}
