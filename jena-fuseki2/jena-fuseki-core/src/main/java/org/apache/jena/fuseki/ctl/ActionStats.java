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

package org.apache.jena.fuseki.ctl;

import static java.lang.String.format;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;

public class ActionStats extends ActionContainerItem
{
    // For endpoint with "" as name.
    private static String emptyNameKeyPrefix = "_";

    public ActionStats() { super(); }

    @Override
    public void validate(HttpAction action) {}

    // This does not consult the system database for dormant etc.
    protected JsonValue execCommonContainer(HttpAction action) {
        if ( action.verbose )
            action.log.info(format("[%d] GET stats all", action.id));
        return generateStats(action.getDataAccessPointRegistry());
    }

    public static JsonObject generateStats(DataAccessPointRegistry registry) {
        JsonBuilder builder = new JsonBuilder();
        builder.startObject("top");
        builder.key(ServerConst.datasets);
        builder.startObject("datasets");
        registry.forEach((name, access)->statsDataset(builder, access));
        builder.finishObject("datasets");
        builder.finishObject("top");
        return builder.build().getAsObject();
    }

    protected JsonValue execCommonItem(HttpAction action) {
        String datasetPath = getItemDatasetName(action);
        if ( action.verbose )
            action.log.info(format("[%d] GET stats dataset %s", action.id, datasetPath));

        JsonBuilder builder = new JsonBuilder();
        DataAccessPoint dap = getItemDataAccessPoint(action, datasetPath);
        if ( dap == null )
            ServletOps.errorNotFound(datasetPath);
        builder.startObject("TOP");

        builder.key(ServerConst.datasets);
        builder.startObject("datasets");
        statsDataset(builder, datasetPath, action.getDataAccessPointRegistry());
        builder.finishObject("datasets");

        builder.finishObject("TOP");
        return builder.build();
    }

    public static JsonObject generateStats(DataAccessPoint access) {
        JsonBuilder builder = new JsonBuilder();
        statsDataset(builder, access);
        return builder.build().getAsObject();
    }

    private void statsDataset(JsonBuilder builder, String name, DataAccessPointRegistry registry) {
        DataAccessPoint access = registry.get(name);
        statsDataset(builder, access);
    }

    private static void statsDataset(JsonBuilder builder, DataAccessPoint access) {
        // Object started
        builder.key(access.getName());
        DataService dSrv = access.getDataService();
        builder.startObject("counters");

        builder.key(CounterName.Requests.getName()).value(dSrv.getCounters().value(CounterName.Requests));
        builder.key(CounterName.RequestsGood.getName()).value(dSrv.getCounters().value(CounterName.RequestsGood));
        builder.key(CounterName.RequestsBad.getName()).value(dSrv.getCounters().value(CounterName.RequestsBad));

        builder.key(ServerConst.endpoints).startObject("endpoints");
        int unique = 0;
        for ( Operation operName : dSrv.getOperations() ) {
            List<Endpoint> endpoints = access.getDataService().getEndpoints(operName);

            for ( Endpoint endpoint : endpoints ) {
                String k = endpoint.getName();
                if ( StringUtils.isEmpty(k) )
                    k = emptyNameKeyPrefix+(++unique);
                // Endpoint names are unique for a given service.
                builder.key(k);
                builder.startObject();

                operationCounters(builder, endpoint);
                builder.key(ServerConst.operation).value(operName.getJsonName());
                builder.key(ServerConst.description).value(operName.getDescription());

                builder.finishObject();
            }
        }
        builder.finishObject("endpoints");
        builder.finishObject("counters");
    }

    private static void operationCounters(JsonBuilder builder, Endpoint operation) {
        for (CounterName cn : operation.getCounters().counters()) {
            Counter c = operation.getCounters().get(cn);
            builder.key(cn.getName()).value(c.value());
        }
    }

    @Override
    protected JsonValue execPostContainer(HttpAction action) {
        return execCommonContainer(action);
    }

    @Override
    protected JsonValue execPostItem(HttpAction action) {
        return execCommonItem(action);
    }

    @Override
    protected JsonValue execGetContainer(HttpAction action) {
        return execCommonContainer(action);
    }

    @Override
    protected JsonValue execGetItem(HttpAction action) {
        return execCommonItem(action);
    }
}


