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

package org.apache.jena.fuseki.geosparql;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;

public class Helper {
    static void run(Runnable r) {
        try {
            r.run();
        } catch (QueryExceptionHTTP ex) {
            maybeRetry(r, ex, ex.getStatusCode());
        }
        catch (HttpException ex) {
            maybeRetry(r, ex, ex.getStatusCode());
        }
    }

    private static int RETRIES = 5;

    private static void maybeRetry(Runnable r, RuntimeException ex, int statusCode) {
        int sc = statusCode;
        for (int i = 0 ; i < RETRIES ; i++ ) {
            String label = ex.getClass().getSimpleName();
            System.err.println(label+": "+statusCode);
            if ( ex.getCause() != null )
                System.err.println(label+": "+ex.getCause().getClass().getCanonicalName());
            if ( statusCode == -1 && ex.getCause() instanceof java.net.ConnectException ) {
                System.err.println(" ==== Re-try ["+i+"]");
                // Likely the server isn't fully up and running yet.
                // VM-based CI is prone to other threads having significant pauses.
                Lib.sleep(1000);
                try {
                    r.run();
                    break;
                } catch (QueryExceptionHTTP | HttpException ex2) {
                    continue;
                }
            } else
                throw ex;
        }
    }
}
