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

package com.hp.hpl.jena.tdb.index.bplustree;

import org.apache.jena.atlas.lib.RandomLib ;
import org.apache.jena.atlas.logging.LogCtl ;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class CmdTestBPlusTreeRewriter
{
    static { LogCtl.setLog4j() ; }
    
    public static void main(String...argv)
    {
        // Usage: maxOrder maxSize NumTests
        if ( argv.length != 3 )
        {
            System.err.println("Usage: "+Utils.classShortName(CmdTestBPlusTreeRewriter.class)+" maxOrder maxSize NumTests") ;
            System.exit(1) ;
        }

        
        SystemTDB.NullOut = true ;
        boolean debug = false ;

        int MaxOrder    = -1 ;
        int MinOrder    = 2 ;
        int MinSize     = 0 ;
        int MaxSize     = -1 ;
        int NumTest     = -1 ;

        try { MaxOrder = Integer.parseInt(argv[0]) ; }
        catch (NumberFormatException ex)
        { System.err.println("Bad number for MaxOrder") ; System.exit(1) ; }

        try { MaxSize = Integer.parseInt(argv[1]) ; }
        catch (NumberFormatException ex)
        { System.err.println("Bad number for MaxSize") ; System.exit(1) ; }

        try { NumTest = Integer.parseInt(argv[2]) ; }
        catch (NumberFormatException ex)
        { System.err.println("Bad number for NumTest") ; System.exit(1) ; }
        
//        int MaxOrder    = 10 ;
//        int MinOrder    = 2 ;
//        int MinSize     = 0 ;
//        int MaxSize     = 1000 ;
//        int NumTest     = 10000 ; //10*1000 ;
        int KeySize     = 4 ;
        int ValueSize   = 8 ;
        
        RecordFactory recordFactory = new RecordFactory(KeySize,ValueSize) ;

        int successes   = 0 ;
        int failures    = 0 ;

        int[] orders = null ;
        int[] sizes =  null ; 
        
        if ( false )
        {
            // Specific test case.
            orders = new int[]{2} ;
            sizes =  new int[]{20} ;
            NumTest = sizes.length ;
            SystemTDB.NullOut = true ;
            debug = true ;
            BPlusTreeRewriter.debug = true;
        }   
        else
        {
            orders = new int[NumTest] ;
            sizes =  new int[NumTest] ;
            for ( int i = 0 ; i < orders.length ; i++ )
            {
                int order = ( MinOrder == MaxOrder ) ? MinOrder :  MinOrder+RandomLib.random.nextInt(MaxOrder-MinOrder) ;
                int size = ( MinSize == MaxSize ) ? MinSize :      MinSize+RandomLib.random.nextInt(MaxSize-MinSize) ;
                orders[i] = order ;
                sizes[i] = size ;
            }
        }

        int numOnLine = 50 ;
        int testsPerTick = 500 ;
        int testCount = 1 ;
        for ( testCount = 1 ; testCount <= orders.length ; testCount++ )
        {
            if ( testCount % testsPerTick == 0 )
                System.out.print(".") ;
            if ( testCount % (testsPerTick*numOnLine) == 0 )
                System.out.println();
            
            int idx = testCount - 1 ;
            int order = orders[idx] ;
            int size = sizes[idx] ;
            try { 
                TestBPlusTreeRewriter.runOneTest(order, size, recordFactory, debug) ;
                successes ++ ;
            } catch (RuntimeException ex)
            {
                System.err.printf("-- Fail: (order=%d, size=%d)\n", order, size) ;
                ex.printStackTrace(System.err) ;
                System.err.printf("--------------------------\n") ;
                failures ++ ;
            }
        }
        
        if ( testCount % (testsPerTick*numOnLine) != 0 )
            System.out.println();
            
        System.err.flush() ;
        System.out.flush() ;
        System.out.printf("DONE : %,d tests : Success=%,d, Failures=%,d\n", NumTest, successes, failures);
    }
}
