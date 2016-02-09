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
 
package repack;

import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphWrapper ;

public class DatasetGraphSwitchable extends DatasetGraphWrapper
{
    private DatasetGraph dsgx ;
    
    /** The dataset to use for redirection - can be overridden.
     *  It is also guarantee that this is called only once per
     *  delegated call.  Changes to the wrapped object can be
     *  made based on that contract. 
     */
    @Override
    public DatasetGraph get() { return dsgx ; }

    public DatasetGraph set(DatasetGraph dsg) { return dsgx = dsg ; }
    
    public DatasetGraphSwitchable(DatasetGraph dsg) {
        super(null) ;
        dsgx = dsg ;
    }
}

