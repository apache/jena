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

package com.hp.hpl.jena.tdb.index;

import org.apache.jena.atlas.lib.RandomLib ;
import org.apache.jena.atlas.test.ExecGenerator ;

public class IndexTestGenerator implements ExecGenerator
{
    int maxNumKeys ;
    int maxValue ;
    IndexMaker maker ;
    
    public IndexTestGenerator(IndexMaker maker, int maxValue, int maxNumKeys)
    {
        if ( maxValue <= maxNumKeys )
            throw new IllegalArgumentException("RangeIndexTestGenerator: Max value less than number of keys") ;
        this.maker = maker ;
        this.maxValue = maxValue ; 
        this.maxNumKeys = maxNumKeys ;
    }
    
    @Override
    public void executeOneTest()
    {
        int numKeys = RandomLib.random.nextInt(maxNumKeys)+1 ;
        IndexTestLib.randTest(maker.makeIndex(), maxValue, numKeys) ;
    }
}
