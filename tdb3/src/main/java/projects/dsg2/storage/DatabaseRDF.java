/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package projects.dsg2.storage;

import org.apache.jena.sparql.core.Transactional;
import projects.prefixes.DatasetPrefixesStorage2;

// "Storage" - full database level.
// And make StorageRDF, DatasetPrefixesStorage2 -- TransactionalComponents?

public interface DatabaseRDF extends StorageRDF, DatasetPrefixesStorage2, Transactional {

    /**
     * @return the triples/quads storage. 
     */
    public default StorageRDF getData()                     { return this ; }
    
    /**
     * @return the prefixes storage.
     */
    public default DatasetPrefixesStorage2 getPrefixes()    { return this ; }
}
