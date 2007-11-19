/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dbtest;

import java.sql.Connection;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestDB extends TestCase
{
    static private final String asciiBase             = "abc" ;
    static private final String latinBase             = "Àéíÿ" ;
    static private final String latinExtraBase        = "ỹﬁﬂ" ;  // fi-ligature, fl-ligature
    static private final String greekBase             = "αβγ" ;
    static private final String hewbrewBase           = "אבג" ;
    static private final String arabicBase            = "ءآأ";
    static private final String symbolsBase           = "☺☻♪♫" ;
    static private final String chineseBase           = "孫子兵法" ; // The Art of War 
    static private final String japaneseBase          = "日本" ;    // Japan
    
    static public TestSuite suite(Connection jdbc, Params params)
    {
        TestSuite ts = new TestSuite() ;

        ts.addTest(new TestShortText(jdbc, "ASCII", asciiBase, params)) ;
        ts.addTest(new TestShortText(jdbc, "Accented Latin", latinBase, params)) ;
        ts.addTest(new TestShortText(jdbc, "Extra Latin", latinExtraBase, params)) ;
        ts.addTest(new TestShortText(jdbc, "Greek", greekBase, params)) ;
        ts.addTest(new TestShortText(jdbc, "Arabic", arabicBase, params)) ;
        ts.addTest(new TestShortText(jdbc, "Hewbrew", hewbrewBase, params)) ;
        ts.addTest(new TestShortText(jdbc, "Symbols", symbolsBase, params)) ;
        ts.addTest(new TestShortText(jdbc, "Chinese", chineseBase, params)) ;
        ts.addTest(new TestShortText(jdbc, "Japanese", japaneseBase, params)) ;

        boolean longBinary = params.get(ParamsVocab.TestLongBinary).equalsIgnoreCase("true") ;
        boolean longText = params.get(ParamsVocab.TestLongText).equalsIgnoreCase("true") ;
        
        if ( longBinary )
        {
            ts.addTest(new TestBinary(jdbc, "ASCII", asciiBase, params)) ;
            ts.addTest(new TestBinary(jdbc, "Accented Latin", latinBase, params)) ;
            ts.addTest(new TestBinary(jdbc, "Extra Latin", latinExtraBase, params)) ;
            ts.addTest(new TestBinary(jdbc, "Greek", greekBase, params)) ;
            ts.addTest(new TestBinary(jdbc, "Arabic", arabicBase, params)) ;
            ts.addTest(new TestBinary(jdbc, "Hewbrew", hewbrewBase, params)) ;
            ts.addTest(new TestBinary(jdbc, "Symbols", symbolsBase, params)) ;
            ts.addTest(new TestBinary(jdbc, "Chinese", chineseBase, params)) ;
            ts.addTest(new TestBinary(jdbc, "Japanese", japaneseBase, params)) ;
        }
        
        if ( longText )
        {
            ts.addTest(new TestLongText(jdbc, "ASCII", asciiBase, params)) ;
            ts.addTest(new TestLongText(jdbc, "Accented Latin", latinBase, params)) ;
            ts.addTest(new TestLongText(jdbc, "Extra Latin", latinExtraBase, params)) ;
            ts.addTest(new TestLongText(jdbc, "Greek", greekBase, params)) ;
            ts.addTest(new TestLongText(jdbc, "Arabic", arabicBase, params)) ;
            ts.addTest(new TestLongText(jdbc, "Hewbrew", hewbrewBase, params)) ;
            ts.addTest(new TestLongText(jdbc, "Symbols", symbolsBase, params)) ;
            ts.addTest(new TestLongText(jdbc, "Chinese", chineseBase, params)) ;
            ts.addTest(new TestLongText(jdbc, "Japanese", japaneseBase, params)) ;
        }
        
        if ( ! longBinary && ! longText )
            System.err.println("Warning: not testing long binary nor long text fields") ;
        
        return ts ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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