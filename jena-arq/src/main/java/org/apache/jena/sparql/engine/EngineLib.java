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
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.query.QueryExecution;

/** Misc query engine related functions */
public class EngineLib {

    /**
     * Parse string in the format "number" or "number,number" and apply this to the
     * {@code QueryExecution} object.
     */
    public static void parseSetTimeout(QueryExecution qExec, String str, TimeUnit unit, boolean merge) {
        if ( str == null )
            return;
        try {
            Pair<Long, Long> pair = parseTimoutStr(str, unit);
            long x1 = pair.getLeft();
            long x2 = pair.getRight();
            if ( merge )
                mergeTimeouts(qExec, x1, x2);
            else
                qExec.setTimeout(x1, x2);
        } catch (RuntimeException ex) {
            Log.warn(qExec, "Can't interpret string for timeout: " + str);
        }
    }

    public static Pair<Long, Long> parseTimoutStr(String str, TimeUnit unit) {
        if ( str.contains(",") ) {
            String[] a = str.split(",");
            if ( a.length > 2 )
                throw new QueryBuildException();
            long x1 = Long.parseLong(a[0]);
            x1 = unit.toMillis(x1);
            long x2 = Long.parseLong(a[1]);
            x2 = unit.toMillis(x2);
            return Pair.create(x1, x2);
        } else {
            long x = Long.parseLong(str);
            x = unit.toMillis(x);
            // Overall timout
            return Pair.create(-1L, x);
        }
    }

    /** Merge in query timeouts - that is respect settings in qExec already there. */
    private static void mergeTimeouts(QueryExecution qExec, long timeout1, long timeout2) {
        // Bound timeout if the QueryExecution alreasdy has a setting
        if ( timeout1 >= 0 ) {
            if ( qExec.getTimeout1() != -1 )
                timeout1 = Math.min(qExec.getTimeout1(), timeout1);
        } else
            timeout1 = qExec.getTimeout1();

        if ( timeout2 >= 0 ) {
            if ( qExec.getTimeout2() != -1 )
                timeout2 = Math.min(qExec.getTimeout2(), timeout2);
        } else
            timeout2 = qExec.getTimeout2();
        qExec.setTimeout(timeout1, timeout2);
    }

}
