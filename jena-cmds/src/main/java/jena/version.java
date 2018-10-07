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

package jena;

import static org.apache.jena.atlas.logging.LogCtl.setCmdLogging;

import org.apache.jena.Jena ;

/**
 * jena.version
 * Print out the jena version and exit.
 */
public class version implements Jena {

    static { setCmdLogging(); }

    /**
     * Print out jena version information and exits.
     */
    public static void main(String... args) throws IllegalArgumentException, IllegalAccessException {
        String x = Jena.VERSION;
        System.out.println(x);
    }
}
