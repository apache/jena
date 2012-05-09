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

//Keywords for JSON:
//Taken from:
//From http://www.w3.org/TR/sparql11-results-json/ (Oct 2011)

public class JSONResultsKW
{
    public static String kHead          = "head" ;
    public static String kVars          = "vars" ;
    public static String kLink          = "link" ;
    public static String kResults       = "results" ;
    public static String kBindings      = "bindings" ;
    public static String kType          = "type" ;
    public static String kUri           = "uri"  ;
    public static String kValue         = "value" ;
    public static String kLiteral       = "literal" ;
    public static String kTypedLiteral  = "typed-literal" ;
    public static String kXmlLang       = "xml:lang" ;
    public static String kDatatype      = "datatype" ;
    public static String kBnode         = "bnode" ;
    public static String kBoolean       = "boolean" ;
}

