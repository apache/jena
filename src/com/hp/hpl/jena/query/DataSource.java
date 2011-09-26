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

package com.hp.hpl.jena.query;

//import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Model ;

/** A DataSource is a Dataset that has modification operations. */

public interface DataSource extends Dataset
{
    /** Set the background graph.  Can be set to null for none.  */
    public void  setDefaultModel(Model model) ;

    /** Set a named graph. */
    public void  addNamedModel(String uri, Model model) throws LabelExistsException ;

    /** Remove a named graph. */
    public void  removeNamedModel(String uri) ;

    /** Change a named graph for another using the same name */
    public void  replaceNamedModel(String uri, Model model) ;
}
