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

package com.hp.hpl.jena.sparql.resultset;

public class JSONResults
{
    public static final int INDENT = 2 ;
        
    public static final String dfHead           = "head" ;
    public static final String dfVars           = "vars" ;
    public static final String dfLink           = "link" ;
    public static final String dfResults        = "results" ;
    public static final String dfBindings       = "bindings" ;
    public static final String dfBoolean        = "boolean" ;
    
    // Not part of SPARQl results formats any more. 
//    public static final String dfOrdered    = "ordered" ;
//    public static final String dfDistinct   = "distinct" ;
    
    public static final String dfType           = "type" ;
    public static final String dfValue          = "value" ;
    public static final String dfDatatype       = "datatype" ;
    public static final String dfLang           = "xml:lang" ;
    
    public static final String dfBNode          = "bnode" ;
    public static final String dfURI            = "uri" ;
    public static final String dfLiteral        = "literal" ;
    public static final String dfTypedLiteral   = "typed-literal" ;  
    public static final String dfUnbound        = "unbound" ;
}
