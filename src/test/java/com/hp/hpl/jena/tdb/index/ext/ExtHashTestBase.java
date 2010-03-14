/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.ext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openjena.atlas.lib.ListUtils.asList ;
import static org.openjena.atlas.lib.ListUtils.unique ;
import static org.openjena.atlas.lib.RandomLib.random ;
import static org.openjena.atlas.test.Gen.permute ;
import static org.openjena.atlas.test.Gen.rand ;
import static org.openjena.atlas.test.Gen.strings ;

import java.util.Iterator;
import java.util.List;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.test.ExecGenerator ;
import org.openjena.atlas.test.RepeatExecution ;



import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.ext.ExtHash;

public class ExtHashTestBase
{
    public static final RecordFactory factory = new RecordFactory(4, 4) ;
    
    public static void randTests(int maxValue, int maxNumKeys, int iterations, boolean showProgess)
    {
        ExtHashTest test = new ExtHashTest(maxValue, maxNumKeys) ;
        RepeatExecution.repeatExecutions(test, iterations, showProgess) ;
    }
    
    static class ExtHashTest implements ExecGenerator
    {
        int maxNumKeys ;
        int maxValue ;
        ExtHashTest(int maxValue, int maxNumKeys)
        {
            if ( maxValue <= maxNumKeys )
                throw new IllegalArgumentException("ExtHashTest: Max value less than number of keys") ;
            this.maxValue = maxValue ; 
            this.maxNumKeys = maxNumKeys ;
        }
        
        //@Override
        public void executeOneTest()
        {
            int numKeys = random.nextInt(maxNumKeys)+1 ;
            randTest(maxValue, numKeys) ;
        }
    }

    public static void randTest(int maxValue, int numKeys)
    {
//      if ( numKeys >= 3000 )
//      System.err.printf("Warning: too many keys\n") ;

        int[] r1 = rand(numKeys, 0, maxValue) ;
        int[] r2 = permute(r1, 4*numKeys) ;
        runTest(r1, r2) ;
    }
        
    public static void runTest(int[] r1, int[] r2)
    {
        try {
            ExtHash extHash = create(r1) ;
            check(extHash, r1) ;
            delete(extHash, r2) ;
            check(extHash) ;
        } catch (RuntimeException ex)
        {
            System.err.println() ;
            System.err.printf("int[] r1 = {%s} ;\n", strings(r1)) ;
            System.err.printf("int[] r2 = {%s}; \n", strings(r2)) ;
            throw ex ;
        }
    }

    public static ExtHash make()
    {
        return ExtHash.createMem(factory, 128) ;
    }
    
    public static Record intToRecord(int v)
    {
        byte[] key = Bytes.packInt(v) ;
        byte[] val = Bytes.packInt(v+100) ;
        return factory.create(key, val) ;
    }
    
    public static Record intToRecordKey(int v)
    {
        byte[] key = Bytes.packInt(v) ;
        return factory.create(key) ;
    }
    
    public static ExtHash create(int...recs)
    {
        ExtHash extHash = make() ;
        for ( int i : recs )
        {
            Record r = intToRecord(i) ;
            extHash.add(r) ;
            
            if ( false ) extHash.dump() ;
        }
        return extHash ;
    }

    public static ExtHash delete(ExtHash extHash, int...recs)
    {
        for ( int i : recs )
        {
            Record r = intToRecord(i) ;
            extHash.delete(r) ;
        }
        return extHash ;
    }

    
    public static void check(ExtHash extHash, int...recs)
    {
        extHash.check();
        for ( int i : recs )
        {
            Record r = intToRecordKey(i) ;
            assertNotNull(extHash.find(r)) ;
        }
        List<Integer> y = unique(asList(recs)) ;
        int x = (int)extHash.size() ;
        if ( x < 0 )
            x = (int)extHash.sessionTripleCount() ;
        assertEquals(y.size(), x); 
    }

    
    public static void check(Iterator<Integer> iter, int...recs)
    {
        for ( int i : recs )
        {
            assertTrue("Iterator shorter than test answers", iter.hasNext()) ;
            int j = iter.next() ;
            assertEquals(i,j) ;
        }
        assertFalse("Iterator longer than test answers", iter.hasNext()) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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