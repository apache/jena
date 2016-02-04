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

package org.apache.jena.fuseki.build;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;

/**
 * Map from configuration Resources in an RDF graph to dataset objects.
 * 
 * <p>
 *   Provides a singleton for use in building the Fuseki configuration to
 *   ensure that each dataset description resource in configuration graphs
 *   corresponds to one dataset object when multiple services refer to the
 *   same dataset.
 * </p>
 * 
 *
 */
public class DescriptionToDatasetMap  {
	
	private final static DescriptionToDatasetMap singleton = new DescriptionToDatasetMap();
	
	public static DescriptionToDatasetMap getSingleton() { return singleton ; }
	
	private RefCountingMap<Resource, Dataset> map = new RefCountingMap<Resource,Dataset>();
	
	public void add(Resource node, Dataset ds) { map.add(node, ds) ; }
    public void remove(Resource node) { map.remove( node ); }
    public int refCount(Resource node) { return map.refCount( node ) ;}
	public Dataset get(Resource node) { return map.get( node ) ; }
}
