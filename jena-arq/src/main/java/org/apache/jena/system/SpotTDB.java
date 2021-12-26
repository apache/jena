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

package org.apache.jena.system;

import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Spot TDB databases without needing TDB code on the class path. Better spotting
 * code is in org.apache.jena.fuseki.system.spot.TDBOps but that requires TDB1/TDB2
 * on the classpath.
 */
public class SpotTDB {

    public static boolean isTDB(DatasetGraph dsg) {
        return isTDB1(dsg) || isTDB2(dsg);
    }

    public static boolean isTDB1(DatasetGraph dsg) {
        String classname = dsg.getClass().getName();
        //org.apache.jena.tdb.transaction.DatasetGraphTransaction
        return classname.startsWith("org.apache.jena.tdb.");
    }

    public static boolean isTDB2(DatasetGraph dsg) {
        String classname = dsg.getClass().getName();
        // org.apache.jena.tdb2.store.DatasetGraphSwitchable
        return classname.startsWith("org.apache.jena.tdb2.");
    }

}
