/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib;

import static lib.ListUtils.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import test.BaseTest;

public class TestSetUtils extends BaseTest
{
    @Test public void set01() 
    {
        Set<Integer> x = set(1,2,3) ;
        test(x,1,2,3) ;
    }

    @Test public void set02() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(1,2,3) ;
        Set<Integer> x3 = SetUtils.intersection(x1, x2) ;
        test(x3, 1,2,3) ;
        x3 = SetUtils.intersection(x2, x1) ;
        test(x3, 1,2,3) ;
    }

    @Test public void set03() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(2,9) ;
        Set<Integer> x3 = SetUtils.intersection(x1, x2) ;
        test(x3, 2) ;
        x3 = SetUtils.intersection(x2, x1) ;
        test(x3, 2) ;
    }

    @Test public void set04() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(6,7,8) ;
        Set<Integer> x3 = SetUtils.intersection(x1, x2) ;
        test(x3) ;
        x3 = SetUtils.intersection(x2, x1) ;
        test(x3) ;
    }

    @Test public void set05() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(1,2,3) ;
        Set<Integer> x3 = SetUtils.union(x1, x2) ;
        test(x3, 1,2,3) ;
        x3 = SetUtils.union(x2, x1) ;
        test(x3, 1,2,3) ;
    }

    @Test public void set06() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(2,9) ;
        Set<Integer> x3 = SetUtils.union(x1, x2) ;
        test(x3, 1,2,3,9) ;
        x3 = SetUtils.union(x2, x1) ;
        test(x3, 1,2,3,9) ;
    }

    @Test public void set07() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set() ;
        Set<Integer> x3 = SetUtils.union(x1, x2) ;
        test(x3,1,2,3) ;
        x3 = SetUtils.union(x2, x1) ;
        test(x3,1,2,3) ;
    }

    @Test public void set08() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set() ;
        Set<Integer> x3 = SetUtils.difference(x1, x2) ;
        test(x3,1,2,3) ;
        x3 = SetUtils.difference(x2, x1) ;
        test(x3) ;
    }

    @Test public void set09() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(3) ;
        Set<Integer> x3 = SetUtils.difference(x1, x2) ;
        test(x3,1,2) ;
        x3 = SetUtils.difference(x2, x1) ;
        test(x3) ;
    }

    @Test public void set10() 
    {
        Set<Integer> x1 = set(1,2,3) ;
        Set<Integer> x2 = set(4,5,6) ;
        Set<Integer> x3 = SetUtils.difference(x1, x2) ;
        test(x3,1,2,3) ;
        x3 = SetUtils.difference(x2, x1) ;
        test(x3,4,5,6) ;
    }
    
    // --------
    
    private static Set<Integer> set(int... values)
    {
        return new HashSet<Integer>(asList(values)) ;
    }

    private void test(Set<Integer> x, int...values)
    {
        List<Integer> y = asList(values) ;
        assertEquals(y.size(), x.size()) ;
        
        for ( int i = 0; i < y.size() ; i++ )
            assertTrue(x.contains(y.get(i))) ; 
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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