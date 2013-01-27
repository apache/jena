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

import com.hp.hpl.jena.util.iterator.*;

/** An iterator which returns RDF Resources.
 *
 * <p>RDF iterators are standard Java iterators, except that they
 *    have an extra method that returns a specifically typed object,
 *    in this case RDF Resource, and have a <CODE>close()</CODE> method
 *    that should be called to free resources if the caller does not
 *    complete the iteration.</p>
 */
public interface ResIterator extends ExtendedIterator<Resource> 
    {
    public Resource nextResource();
    }
