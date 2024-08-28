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

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.function.FunctionRegistry;

public class CDTLiteralFunctions {
    public static void register( final FunctionRegistry functionRegistry ) {
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "concat",  ConcatFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "contains",     ContainsFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "containsKey",  ContainsKeyFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "containsTerm", ContainsTermFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "get",     GetFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "head",    HeadFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "keys",    KeysFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "List",    ListFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "Map",     MapFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "merge",   MergeFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "put",     PutFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "remove",  RemoveFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "reverse", ReverseFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "size",    SizeFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "subseq",  SubSeqFct.class );
        functionRegistry.put( ARQConstants.CDTFunctionLibraryURI + "tail",    TailFct.class );
    }
}
