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

package com.hp.hpl.jena.rdf.model;

/**
    A ModelGetter object can retrieve a Model given a URL for it. If it doesn't
    have such a Model, it returns null.
<p>
    ModelGetters are very weakly constrained. They need not contain any
    models at all; they may forget models they have just delivered; they may
    deliver different models given the same URL. They <i>are</i> constrained to
    deliver an "appropriate" model for the URL, however - whatever they return
    should be strongly related to the RDF which might, at some time or another,
    be retrieved from that URL.
*/
public interface ModelGetter
    {
    /**
        Answer a Model whose content is that associated with the URL, if possible,
        and otherwise answer null.
    */
    public Model getModel( String URL );
    
    /**
        Answer a model appropriate for <code>URL</code>, If none is to hand,
        and it's possible to create one, create it and load it using <code>loadIfAbsent</code>.
        Otherwise throw CannotCreateException. This method never returns null.
    */
    public Model getModel( String URL, ModelReader loadIfAbsent );
    }
