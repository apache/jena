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
package org.apache.jena.sparql.engine.benchmark;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;

/** Utility methods to read RDF query task descriptions and create {@link QueryTask} objects from them. */
public class QueryTaskReader {
    // Internal vocabulary for representing the query tasks. May be changed / improved.
    public static final String NS = "http://www.example.org/";
    public static final Property queryString = ResourceFactory.createProperty(NS + "queryString");
    public static final Property expectedResultSetSize = ResourceFactory.createProperty(NS + "expectedResultSetSize");

    /**
     * The 'skipValidation' property disables checking the expected result set size for the specified versions
     * A more complex solution could allow specifying expected results per version
     */
    public static final Property skipValidation = ResourceFactory.createProperty(NS + "skipValidation");
    public static final Property skipExecution = ResourceFactory.createProperty(NS + "skipExecution");

    public static QueryTask loadOne(String location, String jenaVersion) {
        Creator<QueryTaskBuilder> taskBuilder = QueryTaskBuilderRegistry.get().get(jenaVersion);
        return loadOne(location, jenaVersion, taskBuilder);
    }

    public static QueryTask loadOne(String location, String jenaVersion, Creator<QueryTaskBuilder> taskBuilderCreator) {
        List<QueryTask> tasks = load(location, jenaVersion, taskBuilderCreator);
        if (tasks.size() != 1) {
            throw new RuntimeException("Exactly one task expected");
        }
        return tasks.get(0);
    }

    public static List<QueryTask> load(String location, String jenaVersion, Creator<QueryTaskBuilder> taskBuilderCreator) {
        Model model = RDFDataMgr.loadModel(location);
        return load(model, jenaVersion, taskBuilderCreator);
    }

    public static List<QueryTask> load(Model model, String jenaVersion, Creator<QueryTaskBuilder> taskBuilderCreator) {
        List<Resource> taskDescriptions = model.listResourcesWithProperty(queryString).toList();
        List<QueryTask> result = taskDescriptions.stream()
                .map(task -> configure(task, jenaVersion, taskBuilderCreator.create()).build())
                .collect(Collectors.toList());
        return result;
    }

    public static QueryTaskBuilder configure(Resource taskDescription, String jenaVersion, QueryTaskBuilder taskBuilder) {
        String query = taskDescription.getRequiredProperty(queryString).getString();
        long size = Optional.ofNullable(taskDescription.getProperty(expectedResultSetSize)).map(Statement::getLong).orElse(-1l);

        Set<String> skippedExecutions = taskDescription.listProperties(skipExecution).mapWith(Statement::getString).toSet();
        boolean skipExecution = skippedExecutions.contains(jenaVersion);

        Set<String> skippedVersions = taskDescription.listProperties(skipValidation).mapWith(Statement::getString).toSet();
        boolean skipValidation = skippedVersions.contains(jenaVersion);

        taskBuilder
            .query(query)
            .expectedResultSetSize(size)
            .skipExecution(skipExecution)
            .skipValidation(skipValidation);
        return taskBuilder;
    }
}
