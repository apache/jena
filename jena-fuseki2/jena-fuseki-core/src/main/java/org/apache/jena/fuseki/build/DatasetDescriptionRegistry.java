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

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;

/**
 * Registry of Datasets created from descriptions.
 * 
 * <p>
 *   Provides a registry for use in building the Fuseki configuration to
 *   ensure that each dataset description resource in configuration graphs
 *   corresponds to one dataset object when multiple services refer to the
 *   same dataset.
 * </p>
 * 
 *
 */
public class DatasetDescriptionRegistry  {
	
	private Map<Resource, Dataset> map = new HashMap<>();
	
	public DatasetDescriptionRegistry() {}
	
    public void register(Resource node, Dataset ds) {
        Dataset dsCurrent = map.get(node) ;
        if ( dsCurrent != null ) {
            if ( ! dsCurrent.equals(ds) )
                Log.warn(this.getClass(), "Replacing registered dataset for "+node);
        }
        map.put(node, ds);
    }

    public Dataset get(Resource node) {
        return map.get(node);
    }
    
    public void clear() {
        map.clear();
    }
}
