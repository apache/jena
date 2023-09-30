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

package org.apache.jena.fuseki.test;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.web.HttpSC;
import org.junit.Assert;

public class HttpTest {

    public static void expect4xx(Runnable action) {
        try {
            action.run();
            Assert.fail("Expected HttpException");
        } catch (QueryExceptionHTTP ex) {
            if ( ex.getStatusCode() < 400 || ex.getStatusCode() > 499 )
                Assert.fail(ex.getMessage());
            } catch (HttpException ex) {
            // -1 : any status code in HttpException
            if ( ex.getStatusCode() < 400 || ex.getStatusCode() > 499 )
                Assert.fail(ex.getMessage());
        }
    }

    public static void expect400(Runnable action) {
        execWithHttpException(HttpSC.BAD_REQUEST_400, action);
    }

    public static void expect401(Runnable action) {
        execWithHttpException(HttpSC.UNAUTHORIZED_401, action);
    }

    public static void expect403(Runnable action) {
        execWithHttpException(HttpSC.FORBIDDEN_403, action);
    }

    public static void expect404(Runnable action) {
        execWithHttpException(HttpSC.NOT_FOUND_404, action);
    }

    public static void expect405(Runnable action) {
        execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, action);
    }

    public static void expect415(Runnable action) {
        execWithHttpException(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, action);
    }


    public static void execWithHttpException(int expectedStatusCode, Runnable action) {
        try {
            action.run();
            Assert.fail("Expected HttpException "+expectedStatusCode);
        } catch (QueryExceptionHTTP ex) {
            if ( expectedStatusCode > 0 )
                Assert.assertEquals(ex.getMessage()+":: ", expectedStatusCode, ex.getStatusCode());
        } catch (HttpException ex) {
            if ( expectedStatusCode > 0 )
                Assert.assertEquals(ex.getMessage()+":: ", expectedStatusCode, ex.getStatusCode());
        }
    }

    public static void expectQuery403(Runnable action) {
        expectQuery(action, HttpSC.FORBIDDEN_403);
    }

    public static void expectQuery401(Runnable action) {
        expectQuery(action, HttpSC.UNAUTHORIZED_401);
    }

    public static void expectQuery(Runnable action, int expected) {
        try {
            action.run();
            throw new HttpException("Expected QueryExceptionHTTP["+expected+"]");
        } catch (QueryExceptionHTTP ex) {
            if ( ex.getStatusCode() != expected )
                throw ex;
        }
    }
}
