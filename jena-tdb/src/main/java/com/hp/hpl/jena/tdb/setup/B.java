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

package com.hp.hpl.jena.tdb.setup;

import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
//import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;

public class B {
    // Build things.
    
    //RecordFactory recordFactory = new RecordFactory(SizeOfNodeId*colMap.length(),0) ;
    
    public static RangeIndex buildRangeIndex(FileSet fileset, RecordFactory recordFactory) {
        BlockMgrBuilder nodeBld = new Builder.BlockMgrBuilderStd() ;
        BlockMgrBuilder leavesBld = new Builder.BlockMgrBuilderStd() ;
        RangeIndexBuilder builder = new Builder.RangeIndexBuilderStd(nodeBld, leavesBld) ;
        return builder.buildRangeIndex(fileset, recordFactory) ; 
    }
    
    public static Index buildIndex(FileSet fileset, RecordFactory recordFactory) {
        BlockMgrBuilder nodeBld = new Builder.BlockMgrBuilderStd() ;
        BlockMgrBuilder leavesBld = new Builder.BlockMgrBuilderStd() ;
        IndexBuilder builder = new Builder.IndexBuilderStd(nodeBld, leavesBld) ;
        return builder.buildIndex(fileset, recordFactory) ; 
    }
    
    
    
}

