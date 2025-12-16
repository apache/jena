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

package org.apache.jena.sparql.function.library.cdt;

import org.apache.jena.cdt.CDT;
import org.apache.jena.sparql.function.FunctionRegistry;

public class CDTLiteralFunctions {
    public static void register( final FunctionRegistry functionRegistry ) {
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "concat",         ConcatFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "contains",       ContainsFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "containsKey",    ContainsKeyFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "containsTerm",   ContainsTermFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "get",            GetFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "head",           HeadFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "keys",           KeysFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "List",           ListFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "Map",            MapFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "merge",          MergeFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "put",            PutFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "remove",         RemoveFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "reverse",        ReverseFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "size",           SizeFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "subseq",         SubSeqFct.class );
        functionRegistry.put( CDT.CDTFunctionLibraryURI + "tail",           TailFct.class );
    }
}
