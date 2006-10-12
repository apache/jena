/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.test;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
//@Suite.SuiteClasses({ TestI18N.class, })
public class TestI18N extends TestStringBase 
{
//    static private final String emptyBase             = "" ;
//    static private final String whitespaceBase        = "   " ;
    static private final String asciiBase             = "abc" ;
    static private final String latinBase             = "Àéíÿ" ;      // "ﬂ" "ỹ" fails.
    static private final String greekBase             = "αβγ" ;
    static private final String hewbrewBase           = "אבג" ;
    static private final String arabicBase            = "ءآأ";
    static private final String symbolsBase           = "☺☻♪♫" ;
    // TODO : Add Chinese, Japanese and Korean as \ u escapes
    
    public TestI18N(String name, String baseString)
    {
        super(name, baseString, Env.test_jdbc, Env.test_params, Env.verbose) ;
    }
    
    // A bizaar way of calling the contructor to make "tests".
    // JUnit4 is class-based, unlike Junit3 where there was a "test" instance
    // underneath the reflection code that found "testXXX" methods.
    
    // Could use @BeforeClass to pull the arguments from a helper. 
    
    //@RunWith(Suite.class)
    //@SuiteClasses({ATest.class, BTest.class, CTest.class})
    //    ==> @Parameterized
    //public class MyTests() {
    //   @BeforeClass public void setupDatabaseBeforeEverything() { ... }
    //   @AfterClass public void teardownDatabaseAfterEverything() { ... }
    //} 
    
    @BeforeClass
    static public void check()
    {
        if ( Env.test_jdbc == null )
            System.err.println("JDBC connection is null") ;
        if ( Env.test_params == null )
            System.err.println("Test parameters are null") ;
    }
    
    @Parameters
    public static Collection data()
    {
        List<Object[]> x = new ArrayList<Object[]>() ;
        
        x.add(new Object[]{ "ASCII", asciiBase } ) ;
//
//        x.add(new Object[]{ "Accented Latin", latinBase } ) ;

        x.add(new Object[]{ "Greek", greekBase } ) ; 

//        x.add(new Object[]{"Arabic", arabicBase } ) ;
//
//        x.add(new Object[]{ "Hewbrew", hewbrewBase } ) ;
//
//        x.add(new Object[]{ "Symbols", symbolsBase} ) ;
            
        return x ;
    }
    
    private static String longString(String base,  int len)
    {
        if ( base.length() == 0 )
            return base ;
        
        StringBuilder value = new StringBuilder() ; 
        for ( int i = 0 ; i < len ; i++ )
        {
            value.append(base) ;
            if ( value.length() > len )
                break ;
        }
        // Trim.
        if ( value.length() > len )
            value = value.delete(len, value.length()) ;
        
        return value.toString() ;
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