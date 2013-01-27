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

package com.hp.hpl.jena.shared.impl;

/**
 * This class holds global, static, configuration parameters that
 * affect the behaviour of the Jena API. These should not be changed
 * unless you are sure you know what you are doing and even then 
 * should ideally only be changed before any Models are created or processed.
 * <p>
 * These parameters should not be regarded as a stable part of the API. If
 * we find them being used significantly that probably means they should be
 * moved to being model-specific rather than global.
 * </p>
 */
public class JenaParameters {
    
//  =======================================================================
//  Parameters affected handling of typed literals
    
    /** 
     * <p> Set this flag to true to cause typed literals to be
     * validated as they are created. </p>
     * <p>
     * RDF does not require ill-formed typed literals to be rejected from a graph
     * but rather allows them to be included but marked as distinct from
     * all legally formed typed literals. Jena2 reflects this by optionally
     * delaying validation of literals against datatype type constraints until
     * the first access. </p>
     */
     public static boolean enableEagerLiteralValidation = false;

     /**
      * Set this flag to true to allow language-free, plain literals and xsd:strings
      * containing the same character sequence to test as sameAs.
      * <p>
      * RDF plain literals and typed literals of type xsd:string are distinct, not
      * least because plain literals may have a language tag which typed literals
      * may not. However, in the absence of a languge tag it can be convenient
      * for applications if the java objects representing identical character
      * strings in these two ways test as semantically "sameAs" each other.
      * At the time of writing is unclear if such identification would be sanctioned
      * by the RDF working group. </p> 
      */
     public static boolean enablePlainLiteralSameAsString = true;
     
     /**
      * Set this flag to true to allow unknown literal datatypes to be
      * accepted, if false then such literals will throw an exception when
      * first detected. Note that any datatypes unknown datatypes encountered
      * whilst this flag is 'true' will be automatically registered (as a type 
      * whose value and lexical spaces are identical). Subsequently turning off
      * this flag will not unregister those unknown types already encountered.
      * <p>
      * RDF allows any URI to be used to indicate a datatype. Jena2 allows
      * user defined datatypes to be registered but it is sometimes convenient
      * to be able to process models containing unknown datatypes (e.g. when the
      * application does not need to touch the value form of the literal). However,
      * this behaviour means that common errors, such as using the wrong URI for
      * xsd datatypes, may go unnoticed and throw obscure errors late in processing.
      * Hence, the default is the require unknown datatypes to be registered.
      */
     public static boolean enableSilentAcceptanceOfUnknownDatatypes = true;

    /**
     * Set this flag to true to switch on checking of surrounding whitespace
     * in non-string XSD numeric types. In the false (default) setting then
     * leading and trailing white space is silently trimmed when parsing an
     * XSD numberic typed literal.
     */
    public static boolean enableWhitespaceCheckingOfTypedLiterals = false;
    
    /**
     * Set this flag to true (default) to hide certain internal nodes from the output
     * of inference graphs. Some rule sets (notably owl-fb) create blank nodes as 
     * part of their reasoning process. If these match some query they can appear
     * in the results. Such nodes are recorded as "hidden" and if this flag is set
     * all triples involving such hidden nodes will be removed from the output - any
     * indirect consequences will, however, still be visible.  
     */
    public static boolean enableFilteringOfHiddenInfNodes = true;    
    
    /**
     * If this flag is true (default) then attmempts to build an OWL inference
     * graph over another OWL inference graph will log a warning message.
     */
    public static boolean enableOWLRuleOverOWLRuleWarnings = true;
    
    /**
     * If this flag is true (default is false) then bNodes are assigned a
     * simple count local to this JVM. This is ONLY for use in debugging
     * systems exhibiting non-deterministic behaviour due to the 
     * time-dependence of UIDs, not for normal production use. In particular, it
     * breaks the contract that anonIDs should be unique on the same machine: they 
     * will only be unique for this single JVM run.
     */
    public static boolean disableBNodeUIDGeneration = false;

}
