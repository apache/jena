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

package org.apache.jena.sparql.engine;

import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;

/** Processing timeout strings. */
public class Timeouts {

    public static Pair<Long, Long> parseTimeoutStr(String str, TimeUnit unit) {
        try {
            if ( str.contains(",") ) {
                String[] a = str.split(",");
                if ( a.length > 2 ) {
                    return null;
                }
                long x1 = Long.parseLong(a[0]);
                x1 = unit.toMillis(x1);
                long x2 = Long.parseLong(a[1]);
                x2 = unit.toMillis(x2);
                return Pair.create(x1, x2);
            } else {
                long x = Long.parseLong(str);
                x = unit.toMillis(x);
                // Overall timeout
                return Pair.create(-1L, x);
            }
        } catch (Exception ex) {
            Log.warn(Timeouts.class, "Failed to parse timeout string: "+str+": "+ex.getMessage());
            return null;
        }
    }
}
