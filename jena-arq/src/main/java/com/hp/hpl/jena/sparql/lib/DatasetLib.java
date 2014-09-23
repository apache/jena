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

package com.hp.hpl.jena.sparql.lib;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.sse.writers.WriterGraph ;
import com.hp.hpl.jena.sparql.util.IsoMatcher ;

public class DatasetLib
{
    /** Write, in SSE format (a debugging format).
     */
    @Deprecated
    public static void dump(DatasetGraph dataset)
    {
        WriterGraph.output(IndentedWriter.stdout, dataset, null) ;
        IndentedWriter.stdout.flush();
    }
    
    /**
     * Return true if the datasets are isomorphic - same names for graphs, graphs isomorphic.
     * @deprecated Use {@linkplain IsoMatcher#isomorphic(DatasetGraph, DatasetGraph)}
     */
    @Deprecated
    public static boolean isomorphic(Dataset dataset1, Dataset dataset2)
    {
        return isomorphic(dataset1.asDatasetGraph(), dataset2.asDatasetGraph()) ;
    }
    
    /**
     * Return true if the datasets are isomorphic - same names for graphs, graphs isomorphic. 
     * @deprecated Use {@linkplain IsoMatcher#isomorphic(DatasetGraph, DatasetGraph)}
     */
    @Deprecated
    public static boolean isomorphic(DatasetGraph dataset1, DatasetGraph dataset2)
    {
        return IsoMatcher.isomorphic(dataset1, dataset2) ;
    }
    
    // A DatasetGraph that creates memory graphs on mention 
    /** 
     * @deprecated Use {@linkplain DatasetGraphFactory#createMem()}
     */
    @Deprecated
    public static DatasetGraph createDatasetGraphMem()
    {
        return DatasetGraphFactory.createMem() ;
    }
}
