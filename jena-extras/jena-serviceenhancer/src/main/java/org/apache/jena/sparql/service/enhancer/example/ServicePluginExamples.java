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

package org.apache.jena.sparql.service.enhancer.example;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.QueryExecUtils;

public class ServicePluginExamples {

    public static void main(String[] args) {
        customLinearJoin(DatasetFactory.empty());
    }

    public static void customLinearJoin(Dataset dataset) {
        Context cxt = ARQ.getContext().copy();
        ServiceEnhancerInit.wrapOptimizer(cxt);

        String queryStr = "SELECT * {\n"
                + "  BIND(<urn:foo> AS ?s)\n"
                + "  SERVICE <loop:urn:arq:self> {\n"
                + "      { BIND(?s AS ?x) } UNION { BIND(?s AS ?y) }\n"
                + "  }\n"
                + "}";
        execQueryAndShowResult(dataset, queryStr, cxt);

        /*
         * -------------------------------------
         * | s         | x         | y         |
         * =====================================
         * | <urn:foo> | <urn:foo> |           |
         * | <urn:foo> |           | <urn:foo> |
         * -------------------------------------
         */
    }

    public static void execQueryAndShowResult(
            Dataset dataset,
            String queryStr,
            Context cxt) {
        try {
            try (QueryExec exec = QueryExecDatasetBuilder.create()
                    .dataset(dataset.asDatasetGraph())
                    .query(queryStr)
                    .context(cxt)
                    .build()) {
                QueryExecUtils.exec(exec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
