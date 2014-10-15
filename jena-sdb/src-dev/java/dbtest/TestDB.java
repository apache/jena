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
    static private final String japaneseBase          = "日本" ;    // Japanese
    
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
