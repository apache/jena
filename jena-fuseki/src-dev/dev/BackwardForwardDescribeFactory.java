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

package dev;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerFactory;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackwardForwardDescribeFactory implements DescribeHandlerFactory {

    final static Logger log = LoggerFactory.getLogger(BackwardForwardDescribeFactory.class);

    static {
        TDB.init();
        log.info("Attaching replacement describe handler");
        DescribeHandlerRegistry reg = DescribeHandlerRegistry.get();
        log.info("Clearing existing describe handlers");
        reg.clear();
        reg.add(new BackwardForwardDescribeFactory());
        log.info("Attached");
    }

    @Override
    public DescribeHandler create() {
        return new BackwardForwardDescribe();
    }

    public static class BackwardForwardDescribe implements DescribeHandler {

        private Dataset dataset;
        private Model result;
        private Model defaultModel;
        private Model unionModel;

        @Override
        public void start(Model accumulateResultModel, Context qContext) {
            this.result = accumulateResultModel;
            this.dataset = (Dataset) qContext.get(ARQConstants.sysCurrentDataset);
            this.defaultModel = dataset.getDefaultModel();
            this.unionModel = dataset.getNamedModel(Quad.unionGraph.getURI());
        }

        @Override
        public void describe(Resource resource) {
            result.add(defaultModel.listStatements(resource, null, (RDFNode) null));
            result.add(defaultModel.listStatements(null, null, resource));
            result.add(unionModel.listStatements(resource, null, (RDFNode) null));
            result.add(unionModel.listStatements(null, null, resource));

            // Gather labels for dangling refs
            Model labels = ModelFactory.createDefaultModel();
            ExtendedIterator<RDFNode> it = result.listObjects().andThen(result.listSubjects());
            while (it.hasNext()) {
                RDFNode node = it.next();
                if (node.isLiteral() || resource.equals(node)) continue;
                labels.add(defaultModel.listStatements((Resource) node, RDFS.label, (RDFNode) null));
                labels.add(unionModel.listStatements((Resource) node, RDFS.label, (RDFNode) null));
            }
            result.add(labels);
        }

        @Override
        public void finish() {
        }
    }
}
