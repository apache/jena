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
    public static Collection<Object[]> data()
    {
        List<Object[]> x = new ArrayList<Object[]>() ;
        
        x.add(new Object[]{ "ASCII", asciiBase } ) ;

        x.add(new Object[]{ "Accented Latin", latinBase } ) ;

        x.add(new Object[]{ "Greek", greekBase } ) ; 

        x.add(new Object[]{"Arabic", arabicBase } ) ;

        x.add(new Object[]{ "Hewbrew", hewbrewBase } ) ;

        x.add(new Object[]{ "Symbols", symbolsBase} ) ;
            
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
