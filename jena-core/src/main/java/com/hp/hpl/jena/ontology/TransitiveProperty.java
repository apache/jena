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
 * Interface that denotes a property that is transitive i&#046;e&#046; one
 * in which if <code><em>x</em>&nbsp;p&nbsp;<em>y</em></code> holds,
 * and <code><em>y</em>&nbsp;p&nbsp;<em>z</em></code>
 * holds, then <code><em>x</em>&nbsp;p&nbsp;<em>z</em></code> must also hold.
 * </p>
 */
public interface TransitiveProperty
    extends ObjectProperty
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


}
