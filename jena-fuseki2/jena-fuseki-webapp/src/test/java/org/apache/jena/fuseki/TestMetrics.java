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

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpResponseHandler;
import org.apache.jena.web.HttpSC;
import org.junit.Test;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestMetrics extends AbstractFusekiTest {

    @Test
    public void can_retrieve_metrics() {
        RecordingResponseHandler responseHandler = new RecordingResponseHandler();

        HttpOp.execHttpGet( ServerCtl.urlRoot() + "$/metrics" , "", responseHandler);

        assertEquals(HttpSC.OK_200, responseHandler.statusCode);
        assertEquals(WebContent.contentTypeTextPlain, responseHandler.contentType);
        assertEquals(WebContent.charsetUTF8, responseHandler.encoding);
        assertTrue(responseHandler.content.contains("fuseki_requests_good"));
    }

    static class RecordingResponseHandler implements HttpResponseHandler {

        private int statusCode;
        private String contentType;
        private String encoding;
        private String content;

        @Override
        public void handle(String baseIRI, HttpResponse response) throws IOException {
            statusCode = response.getStatusLine().getStatusCode();
            String rawContentType = response.getEntity().getContentType().toString();
            contentType = substringBefore( substringAfter(rawContentType, "Content-Type: "), ";");
            encoding = substringAfter(rawContentType, "charset=");
            content = IOUtils.toString( response.getEntity().getContent(), encoding);
        }
    }
}
