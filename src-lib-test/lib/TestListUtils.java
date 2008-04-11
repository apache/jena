/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static lib.ListUtils.* ;

import org.junit.Test;
import test.BaseTest;

public class TestListUtils extends BaseTest
{
    @Test public void list01() 
    {
        List<Integer> x = Arrays.asList(1,2,3) ;
        x = unique(x) ;
        assertEquals(3, x.size()) ;
        test(x, 1,2,3) ;
    }

    @Test public void list02() 
    {
        List<Integer> x = Arrays.asList(1,2,3,1,3,2) ;
        x = unique(x) ;
        assertEquals(3, x.size()) ;
        test(x, 1,2,3) ;
    }

    @Test public void list03() 
    {
        List<Integer> x = new ArrayList<Integer>() ;
        x = unique(x) ;
        assertEquals(0, x.size()) ;
        test(x) ;
    }

    @Test public void list04() 
    {
        List<Integer> x = Arrays.asList(99) ;
        x = unique(x) ;
        assertEquals(1, x.size()) ;
        test(x, 99) ;
    }

    @Test public void list05() 
    {
        List<Integer> x = Arrays.asList(1,1,2,3,1,1,3) ;
        x = unique(x) ;
        assertEquals(3, x.size()) ;
        test(x, 1,2,3) ;
    }

    private void test(List<Integer> x, int... args)
    {
        assertEquals(args.length, x.size()) ;
        
        for ( int i = 0; i < args.length ; i++ )
            assertEquals(args[i], x.get(i).intValue()) ;
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