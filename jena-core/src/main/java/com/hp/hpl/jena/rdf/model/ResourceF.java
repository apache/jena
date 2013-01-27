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

/** Create an application specific resource instance.
 *
 * <p>Applications may need to have classes which add behaviour to Resources,
 * e.g. to support the behaviour of container classes.  Factory objects
 * supporting this interface may be used to construct instances of these
 * so called enhanced resources.</p>

 */
@Deprecated public interface ResourceF {
    /** Create new resource instance which extends the behaviour of a supplied
     * resource.
     * @param r The core resource whose behaviour is to be extended.
     * @return the newly created resource.
     */
  Resource createResource(Resource r);
}
