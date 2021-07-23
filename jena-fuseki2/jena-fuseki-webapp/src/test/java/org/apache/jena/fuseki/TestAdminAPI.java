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

package org.apache.jena.fuseki;

import static org.apache.jena.riot.web.HttpOp.execHttpGet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.base.Sys;
import org.apache.jena.fuseki.webapp.FusekiWebapp;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.engine.http.Params;
import org.apache.jena.web.HttpSC;
import org.junit.Test;

/** More tests of the admin functionality
 * See also TestAdmin.
 */
public class TestAdminAPI extends AbstractFusekiWebappTest {

    @Test public void add_delete_api_1() throws Exception {
        testAddDelete("db_mem", "mem", false);
    }

    @Test public void add_delete_api_2() throws Exception {
        // Deleted mmap files on Windows does not go away until the JVM exits.
        if ( org.apache.jena.tdb.sys.SystemTDB.isWindows )
            return;
        testAddDelete("db_tdb", "tdb", true);
    }

    @Test public void add_delete_api_3() throws Exception {
        // Deleted mmap files on Windows does not go away until the JVM exits.
        if ( Sys.isWindows )
            return;
        testAddDelete("db_tdb2", "tdb2", true);
    }

    private static void testAddDelete(String dbName, String dbType, boolean hasFiles) {
        String datasetURL = ServerCtl.urlRoot()+dbName;
        String admin = ServerCtl.urlRoot()+"$/";
        HttpEntity e = createFormEntity(dbName, dbType);

        assertFalse(exists(datasetURL));

        HttpOp.execHttpPost(admin+"datasets", e);

        RDFConnection conn = RDFConnectionFactory.connect(datasetURL);
        conn.update("INSERT DATA { <x:s> <x:p> 123 }");
        int x1 = count(conn);
        assertEquals(1, x1);

        Path pathDB = FusekiWebapp.dirDatabases.resolve(dbName);

        if ( hasFiles )
            assertTrue(Files.exists(pathDB));

        HttpOp.execHttpDelete(admin+"datasets/"+dbName);

        assertFalse(exists(datasetURL));

        //if ( hasFiles )
            assertFalse(Files.exists(pathDB));

        // Recreate : no contents.
        HttpOp.execHttpPost(admin+"datasets", e);
        assertTrue("false: exists("+datasetURL+")", exists(datasetURL));
        int x2 = count(conn);
        assertEquals(0, x2);
        if ( hasFiles )
            assertTrue(Files.exists(pathDB));
    }

    private static boolean exists(String url) {
        try ( TypedInputStream in = execHttpGet(url) ) {
            return true;
        } catch (HttpException ex) {
            if ( ex.getStatusCode() == HttpSC.NOT_FOUND_404 )
                return false;
            throw ex;
        }
    }

    static int count(RDFConnection conn) {
        try ( QueryExecution qExec = conn.query("SELECT (count(*) AS ?C) { ?s ?p ?o }")) {
            return qExec.execSelect().next().getLiteral("C").getInt();
        }
    }

    static HttpEntity createFormEntity(String dbName, String dbType) {
        List <? extends NameValuePair> parameters =
            Arrays.asList(
                new Params.Pair("dbName", dbName),
                new Params.Pair("dbType", dbType));
        UrlEncodedFormEntity e = new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8);
        return e;
    }
}

