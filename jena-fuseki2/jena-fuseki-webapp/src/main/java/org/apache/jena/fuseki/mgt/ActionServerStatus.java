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

package org.apache.jena.fuseki.mgt;

import static org.apache.jena.riot.WebContent.charsetUTF8;
import static org.apache.jena.riot.WebContent.contentTypeJSON;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.ctl.ActionCtl;
import org.apache.jena.fuseki.ctl.JsonDescription;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.ServerConst;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;

/** Description of datasets for a server */
public class ActionServerStatus extends ActionCtl
{
    public ActionServerStatus() { super(); }

    @Override
    public void validate(HttpAction action) {}

    @Override
    public void execGet(HttpAction action) {
        executeLifecycle(action);
    }

    @Override
    public void execPost(HttpAction action) {
        executeLifecycle(action);
    }

    @Override
    public  void execute(HttpAction action) {
        try {
            description(action);
            ServletOps.success(action);
        } catch (IOException e) {
            ServletOps.errorOccurred(e);
        }
    }

    private void description(HttpAction action) throws IOException {
        ServletOutputStream out = action.response.getOutputStream();
        action.response.setContentType(contentTypeJSON);
        action.response.setCharacterEncoding(charsetUTF8);

        JsonBuilder builder = new JsonBuilder();
        builder.startObject();
        describeServer(builder, action.request.getLocalPort());
        describeDatasets(builder, action.getDataAccessPointRegistry());
        builder.finishObject();

        JsonValue v = builder.build();
        JSON.write(out, v);
        out.println();
        out.flush();
    }

    private void describeServer(JsonBuilder builder, int requestPort) {
        String versionStr = Fuseki.VERSION;
        String builtDateStr = Fuseki.BUILD_DATE;
        if ( versionStr == null || versionStr.startsWith("${") )
            versionStr = "Development";
        if ( builtDateStr == null || builtDateStr.startsWith("${") )
            builtDateStr = "Unknown";

        builder
            .pair(ServerMgtConst.version,   versionStr)
            .pair(ServerMgtConst.built,     builtDateStr)
            .pair(ServerMgtConst.startDT,   Fuseki.serverStartedAt())
            .pair(ServerMgtConst.uptime,    Fuseki.serverUptimeSeconds())
;

    }

    private void describeDatasets(JsonBuilder builder, DataAccessPointRegistry registry) {
        builder.key(ServerConst.datasets);
        JsonDescription.arrayDatasets(builder, registry);
    }

}

