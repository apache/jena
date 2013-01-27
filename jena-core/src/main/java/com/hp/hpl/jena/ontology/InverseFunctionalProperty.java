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

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////


/**
 * <p>
 * Interface encapsulating the class of properties that are inverse functional:
 * that is, properties in which a given given range value has a unique value in
 * the domain (effectively, a key). In OWL DL and OWL Lite, an inverse functional
 * property cannot be a datatype property, whereas OWL Full does permit this
 * (see the relevant section of
 * <a href="http://www.w3.org/TR/2004/REC-owl-ref-20040210/#InverseFunctionalProperty-def">the
 * OWL reference</a> for details). We conservatively model this in Jena by
 * having this interface extend {@link ObjectProperty}. Users who wish to represent
 * inverse functional datatype properties in OWL Full may have to switch
 * off strict checking in <code>OntModel</code> (see {@link OntModel#setStrictMode(boolean)}.
 * </p>
 */
public interface InverseFunctionalProperty
    extends ObjectProperty
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


}
