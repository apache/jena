/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.iterator.Iter;
import com.hp.hpl.jena.sdb.iterator.Transform;
import com.hp.hpl.jena.sdb.shared.StoreList;
import com.hp.hpl.jena.sdb.test.SDBTestSetup;
import com.hp.hpl.jena.sdb.util.Pair;

@RunWith(Parameterized.class)
public abstract class ParamAllStoreDesc
{
    // Make into Object[]{String,Store} lists just for JUnit. 
    static Transform<Pair<String, StoreDesc>, Object[]> fix = new Transform<Pair<String, StoreDesc>, Object[]>()
    {
        public Object[] convert(Pair<String, StoreDesc> item)
        { return new Object[]{item.car(), item.cdr()} ; }
    } ;

    // Build once and return the same for parametrized types each time.
    // Connections are slow to create.
    static Collection<Object[]> data = null ;
    static 
    {
        List<Pair<String, StoreDesc>> x = new ArrayList<Pair<String, StoreDesc>>() ;
        x.addAll(StoreList.storeDesc(SDBTestSetup.storeList)) ;
        x.addAll(StoreList.storeDesc(SDBTestSetup.storeListSimple)) ;
        data = Iter.iter(x).map(fix).toList() ;
    }
    
    // ----
    
    // Each Object[] becomes the arguments to the class constructor (with reflection)
    // Reflection is not sensitive to generic parameterization (it's type erasure) 
    @Parameters public static Collection<Object[]> data() { return data ; }
    
    protected final String name ;
    protected final StoreDesc storeDesc ;
    
    public ParamAllStoreDesc(String name, StoreDesc storeDesc)
    {
        this.name = name ;
        this.storeDesc = storeDesc ;
    }
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