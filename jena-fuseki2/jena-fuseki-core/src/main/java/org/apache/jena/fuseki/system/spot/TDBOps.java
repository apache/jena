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

package org.apache.jena.fuseki.system.spot;

/** Operations related to TDB1 and TDB2 */
public class TDBOps {
    /**
     * Test to see is a location appears to be a TDB1 database, or a database can be
     * created at the pathname.
     */
    public static boolean isTDB1(String pathname) {
        return SpotTDB1.isTDB1(pathname);
    }

    /**
     * Check whether a location is a TDB1 database, performing some validation
     * checks. Throw an exception if invalid. This check passes if a database can be
     * created at the location (i.e. it is empty or does not exist yet).
     * Validation is of the file structure, not the database contents.
     */
    public static void checkTDB1(String pathname) {
        SpotTDB1.checkTDB1(pathname);
    }

    /**
     * Test to see is a location appears to be a TDB2 database, or a database can be
     * created at the pathname.
     */
    public static boolean isTDB2(String pathname) {
        return SpotTDB2.isTDB2(pathname);
    }

    /**
     * Check whether a location is a TDB2 database, performing some validation
     * checks. Throw an exception if invalid. This check passes if a database can be
     * created at the location (i.e. it is empty or does not exist yet).
     * Validation is of the file structure, not the database contents.
     */
    public static void checkTDB2(String pathname) {
        SpotTDB2.checkTDB2(pathname);
    }
}

