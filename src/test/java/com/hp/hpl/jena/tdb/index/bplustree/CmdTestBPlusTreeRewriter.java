/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import org.openjena.atlas.lib.RandomLib ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class CmdTestBPlusTreeRewriter
{
    static { Log.setLog4j() ; }
    
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
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