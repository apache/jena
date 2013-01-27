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

package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.rdf.model.*;

/**
 * The interface through which a reasoner (inference engine) can be
 * instantiated. Instances of this are registered with the global
 * ReasonerRegistry.
 */
public interface ReasonerFactory {

    /**
     * Constructor method that builds an instance of the associated Reasoner
     * @param configuration a set of arbitrary configuration information to be 
     * passed the reasoner, encoded as RDF properties of a base configuration resource,
     * can be null in no custom configuration is required.
     */
    public Reasoner create(Resource configuration);

    /**
     * Return a description of the capabilities of this reasoner encoded in
     * RDF. These capabilities may be static or may depend on configuration
     * information supplied at construction time. May be null if there are
     * no useful capabilities registered.
     */
    public Model getCapabilities();
    
    /**
     * Return the URI labelling this type of reasoner
     */
    public String getURI();
}
