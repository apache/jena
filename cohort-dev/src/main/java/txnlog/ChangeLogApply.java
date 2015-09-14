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

package txnlog;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.DatasetChanges ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.QuadAction ;

class ChangeLogApply implements DatasetChanges {
    private final DatasetGraph dsg;

    ChangeLogApply(DatasetGraph dsg) { this.dsg = dsg ; }

    @Override
    public void start() {}

    @Override
    public void change(QuadAction qAction, Node g, Node s, Node p, Node o) {
        switch(qAction) {
            case ADD:       dsg.add(g, s, p, o) ; break ;
            case DELETE:    dsg.delete(g, s, p, o) ; break ;
            default:        /* no-op */
        }
    }

    @Override
    public void finish() {}

    @Override
    public void reset() {} 
}
