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

package org.apache.jena.sparql.engine.index;

import java.util.Set;

import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.index.HashIndexTable.MissingBindingException ;

/**
 * Creates {@link org.apache.jena.sparql.engine.index.IndexTable}s for use by
 * {@link org.apache.jena.sparql.engine.iterator.QueryIterMinus}.
 * <p>
 * Contribution from Paul Gearon
 */
public class IndexFactory {

    public static IndexTable createIndex(Set<Var> commonVars, QueryIterator data) {
        try {
            if (commonVars.size() == 1) {
                return new SetIndexTable(commonVars, data);
            } else {
                return new HashIndexTable(commonVars, data);
            }
        } catch (MissingBindingException e) {
            return new LinearIndex(commonVars, data, e.getData(), e.getMap());
        }
    }
}
