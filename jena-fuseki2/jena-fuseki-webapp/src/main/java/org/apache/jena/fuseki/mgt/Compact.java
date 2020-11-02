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

package org.apache.jena.fuseki.mgt;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.sparql.core.DatasetGraph;

/** Perform a backup */
public class Compact
{
    /** Perform a compaction
     *  A backup is a dump of the dataset in compressed N-Quads, done inside a transaction.
     */
    public static void compact(DatasetGraph dsg) {
        try {
            //DatabaseMg
        } catch (Exception ex) {
            Log.warn(Fuseki.serverLog, "Exception in backup", ex);
        }
    }
}

