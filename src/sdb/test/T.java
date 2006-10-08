/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.test;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
//@Suite.SuiteClasses({ TestI18N.class, })
public class T extends TestI18N 
{
    static private final String asciiBase             = "abc" ;
    static private final String westernEuropeanBase   = "éíﬂ" ;
    static private final String greekBase             = "αβγ" ;
    static private final String hewbrewBase           = "אבג" ;
    static private final String arabicBase            = "ءآأ";
    static private final String symbolsBase           = "☺☻♪♫" ;
    // TODO : Add Chinese, Japanese and Korean as \ u escapes
    
    public T(String name, String baseString, Connection jdbc, Params params, boolean verbose)
    {
        super(name, baseString, jdbc, params, verbose) ;
    }

    // A bizaar way of calling the contructor to make "tests".
    // JUnit4 is class-based, unlike Junit3 where there was a "test" instance
    // underneath the reflection code that found "testXXX" methods.
    
    // Could use @BeforeClass to pull the arguments from a helper. 
    
    static Connection test_jdbc = null ;
    static Params test_params = null ;
    
    // Call this before setting this test class off
    public static void set(Connection jdbc, Params params) 
    {
        test_jdbc = jdbc ;
        test_params = params ;
    }
    
    @Parameters
    public static Collection data()
    {
        List<Object[]> x = new ArrayList<Object[]>() ;
        x.add(new Object[]{
            "ASCII", asciiBase,
            test_jdbc, test_params, false}) ;
        x.add(new Object[]{
            "Accented Latin", westernEuropeanBase,
            test_jdbc, test_params, false}) ;
        x.add(new Object[]{
            "Greek", greekBase,
            test_jdbc, test_params, false}) ;
        x.add(new Object[]{
            "Arabic", arabicBase,
            test_jdbc, test_params, false}) ;
        x.add(new Object[]{
            "Hewbrew", hewbrewBase,
            test_jdbc, test_params, false}) ;
        x.add(new Object[]{
            "Symbols", symbolsBase,
            test_jdbc, test_params, false}) ;
        return x ;
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