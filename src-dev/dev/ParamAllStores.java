/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.iterator.Iter;
import com.hp.hpl.jena.sdb.iterator.Transform;
import com.hp.hpl.jena.sdb.shared.StoreList;
import com.hp.hpl.jena.sdb.util.Pair;


@RunWith(Parameterized.class)
public class ParamAllStores
{
    static String storeList         = "testing/store-list.ttl" ;
    static String storeListSimple   = "testing/store-list-simple.ttl" ;
    
    static Transform<Pair<String, Store>, Object[]> fix = new Transform<Pair<String, Store>, Object[]>()
    {
        public Object[] convert(Pair<String, Store> item)
        {
            return new Object[]{item} ; 
        }
        
    } ;

    
    // Collection of arrays.
    @Parameters public static Collection<Object[]> data()
    {
        List<Pair<String, Store>> x = new ArrayList<Pair<String, Store>>() ;

        Iter<Pair<String, Store>> s = Iter.iter(x) ; 
        s = s.append(stores(storeList)) ;
        s = s.append(stores(storeListSimple)) ;
        Iter<Object[]> z = s.map(fix) ;
        return z.toList() ; 
        
    }
    private static Iter<Pair<String, Store>> stores(String storeList)
    {
        return Iter.iter(StoreList.stores(storeList)) ;
    }

    public ParamAllStores(Pair<String, Store> x)
    {
        //System.out.println("Store = "+x.car()) ;
        System.out.println("Name = "+x.cdr()) ;
    }

    @Test public void foo1() {} 
    @Test public void foo2() {}

}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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