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

package org.apache.jena.sdb.test.junit;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;
import org.junit.runners.Parameterized.Parameters ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.sdb.StoreDesc ;
import org.apache.jena.sdb.test.SDBTestSetup ;
import org.apache.jena.sdb.util.Pair ;

//@RunWith(Parameterized.class)
public abstract class ParamAllStoreDesc
{

    // Build once and return the same for parametrized types each time.
    // Connections are slow to create.
    static Collection<Object[]> data = null ;
    static 
    {
        List<Pair<String, StoreDesc>> x = new ArrayList<Pair<String, StoreDesc>>() ;
        x.addAll(StoreList.storeDesc(SDBTestSetup.storeList)) ;
        x.addAll(StoreList.storeDesc(SDBTestSetup.storeListSimple)) ;
        data = Iter.iter(x).map(p -> new Object[]{p.car(), p.cdr()}).toList() ;
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
