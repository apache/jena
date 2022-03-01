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

package org.apache.jena.fuseki.main;

import static org.junit.Assert.fail;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.web.HttpSC;
import org.apache.jena.web.HttpSC.Code;

public class FusekiTestLib {

    public static void expect400(Runnable runnable) {
        expectFail(runnable, HttpSC.Code.BAD_REQUEST);
    }

    public static void expect401(Runnable runnable) {
        expectFail(runnable, HttpSC.Code.UNAUTHORIZED);
    }

    public static void expect403(Runnable runnable) {
        expectFail(runnable, HttpSC.Code.FORBIDDEN);
    }

    public static void expect404(Runnable runnable) {
        expectFail(runnable, HttpSC.Code.NOT_FOUND);
    }

    public static void expectFail(Runnable runnable, Code code) {
        if ( code == null || ( 200 <= code.getCode() && code.getCode() < 300 ) ) {
            runnable.run();
            return;
        }
        try {
          runnable.run();
          fail("Failed: Got no exception: Expected HttpException "+code.getCode());
      } catch (HttpException ex) {
          if ( ex.getStatusCode() == code.getCode() )
              return;
          throw ex;
      }
    }

    public static int expectFail(Runnable runnable) {
        try {
          runnable.run();
          fail("Failed: Got no exception: Expected HttpException");
          return -1;
      } catch (HttpException ex) {
          return ex.getStatusCode();
      }
    }

    // Same - except a QueryExceptionHTTP.

    public static void expectQuery400(Runnable runnable) {
        expectQueryFail(runnable, HttpSC.Code.BAD_REQUEST);
    }

    public static void expectQuery401(Runnable runnable) {
        expectQueryFail(runnable, HttpSC.Code.UNAUTHORIZED);
    }

    public static void expectQuery403(Runnable runnable) {
        expectQueryFail(runnable, HttpSC.Code.FORBIDDEN);
    }

    public static void expectQuery404(Runnable runnable) {
        expectQueryFail(runnable, HttpSC.Code.NOT_FOUND);
    }

    public static void expectQueryFail(Runnable runnable, Code code) {
        if ( code == null || ( 200 <= code.getCode() && code.getCode() < 300 ) ) {
            runnable.run();
            return;
        }

        try {
          runnable.run();
          fail("Failed: Got no exception: Expected QueryExceptionHTTP "+code.getCode());
      } catch (QueryExceptionHTTP ex) {
          if ( ex.getStatusCode() == code.getCode() )
              return;
          throw ex;
      }
    }

    public static void expectOK(Runnable runnable) {
        runnable.run();
    }

    public static void expectQueryOK(Runnable runnable) {
        runnable.run();
    }

    public static void expectQueryAccessFail(Runnable runnable) {
        try {
             runnable.run();
             fail("Expected 401 or 403");
        } catch (QueryExceptionHTTP ex) {
            if ( ex.getStatusCode() == HttpSC.UNAUTHORIZED_401 ||
                ex.getStatusCode() == HttpSC.FORBIDDEN_403 )
                return;
            throw ex;
        }
    }

    public static void expectAccessFail(Runnable runnable) {
        try {
             runnable.run();
             fail("Expected 401 or 403");
        } catch (HttpException ex) {
            if ( ex.getStatusCode() == HttpSC.UNAUTHORIZED_401 ||
                ex.getStatusCode() == HttpSC.FORBIDDEN_403 )
                return;
            throw ex;
        }
    }
}
