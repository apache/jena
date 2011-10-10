/**
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

package test;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.tdb.base.file.BlockAccessMem ;
import com.hp.hpl.jena.tdb.index.RangeIndexMaker ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeMaker ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class BPlusTreeRun extends RunnerRangeIndex
{
    static { Log.setLog4j() ; }
    
    static public void main(String...a)
    {
        new BPlusTreeRun().perform(a) ;
    }
    
    @Override
    protected RangeIndexMaker makeRangeIndexMaker()
    {
        BPlusTreeMaker maker = new BPlusTreeMaker(order, order, trackingBlocks) ;
        
        BPlusTree bpt = (BPlusTree)(maker.makeIndex()) ;
        BPlusTreeParams param = bpt.getParams() ;
        System.out.println(bpt.getParams()) ;
        System.out.println("Block size = "+bpt.getParams().getCalcBlockSize()) ;
        return maker ;
    }

    @Override
    protected void initialize(RunType runType)
    {
        switch (runType)
        {
            case test:
                showProgress = true ;
                //BPlusTreeParams.checkAll() ;
                BPlusTreeParams.CheckingTree = true ;
                BPlusTreeParams.CheckingNode = true ;
                SystemTDB.NullOut = true ;
                BlockAccessMem.SafeMode = true ;
                break ;
            case perf:  
                showProgress = false ;
                BPlusTreeParams.CheckingTree = false ;
                BPlusTreeParams.CheckingNode = false ;
                SystemTDB.NullOut = false ;
                BlockAccessMem.SafeMode = false ;
                break ;
        } 
    }

}
