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

package org.apache.jena.riot.rowset.rw;

//Keywords for JSON:
//Taken from:
//From http://www.w3.org/TR/sparql11-results-json/ (Oct 2011)

public class JSONResultsKW
{
    public static final String kHead          = "head" ;
    public static final String kVars          = "vars" ;
    public static final String kLink          = "link" ;
    public static final String kResults       = "results" ;
    public static final String kBindings      = "bindings" ;
    public static final String kType          = "type" ;
    public static final String kUri           = "uri"  ;

    public static final String kValue         = "value" ;
    public static final String kLiteral       = "literal" ;
    public static final String kUnbound       = "undef" ;

    // Legacy: kTypedLiteral
    public static final String kTypedLiteral  = "typed-literal" ;
    public static final String kXmlLang       = "xml:lang" ;
    public static final String kDatatype      = "datatype" ;
    public static final String kBnode         = "bnode" ;
    public static final String kBoolean       = "boolean" ;

    // RDF-star Triple terms
    public static final String kTriple        = "triple" ;
    // Alternative type for RDF-star triple terms.
    public static final String kStatement     = "statement" ;
    public static final String kSubject       = "subject" ;
    public static final String kPredicate     = "predicate" ;
    public static final String kProperty      = "property" ;
    public static final String kObject        = "object" ;
    // RDF-star Triple terms - alternative keywords
    public static final String kSubjectAlt    = "s" ;
    public static final String kPredicateAlt  = "p" ;
    public static final String kObjectAlt     = "o" ;

}

