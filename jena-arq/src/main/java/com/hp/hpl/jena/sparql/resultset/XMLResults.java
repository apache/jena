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

package com.hp.hpl.jena.sparql.resultset;

import com.hp.hpl.jena.sparql.ARQConstants ;


public interface XMLResults
{
    public static final int INDENT = 2 ;

    public static final String baseNamespace   = ARQConstants.srxPrefix ;
    public static final String xsBaseURI       = ARQConstants.XML_SCHEMA_NS ;
    
    public static final String dfAttrVarName   = "name" ;
    public static final String dfAttrDatatype  = "datatype" ;
    
    public static final String dfNamespace  = baseNamespace ;
    public static final String dfRootTag    = "sparql" ;
    public static final String dfHead       = "head" ;
    public static final String dfVariable   = "variable" ;
    public static final String dfLink       = "link" ;
    public static final String dfResults    = "results" ;
    public static final String dfSolution   = "result" ;
    public static final String dfBinding    = "binding" ;
    
    public static final String dfBNode      = "bnode" ;
    public static final String dfURI        = "uri" ;
    public static final String dfLiteral    = "literal" ;
    
    public static final String dfUnbound    = "unbound" ;

    public static final String dfBoolean    = "boolean" ;
}
