/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * References:
 * <ul>
 *  <li><a href="http://en.wikipedia.org/wiki/Roman_numbers">Wikipedia on Roman Numerals</a></li>
 *  <li><a href="http://www.therobs.com/uman/roman.shtml">Therobs Lex &amp; Yacc Example: Roman Numerals</a>
 *  which is were the idea of working right to left, instead of looking ahead, originated for me.</li>
 * </ul>

 * 
 * @author Andy Seaborne
 */

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
        lex = lex.toUpperCase() ;
        // Excludes IIII
        Matcher m = pattern.matcher(lex);
        return m.matches() ;
    }
    
    public static int parse(String lex) { return r2i(lex) ; }
    // It is easier working right to left!
    public static int r2i(String lex)
    {
        lex = lex.toUpperCase() ;

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


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
