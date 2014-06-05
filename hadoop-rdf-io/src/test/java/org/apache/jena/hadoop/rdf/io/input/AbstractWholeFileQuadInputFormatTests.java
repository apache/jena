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

package org.apache.jena.hadoop.rdf.io.input;

import java.io.IOException;
import java.io.Writer;

import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFWriterRegistry;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Abstract tests for Quad input formats
 * 
 * 
 * 
 */
public abstract class AbstractWholeFileQuadInputFormatTests extends AbstractNodeTupleInputFormatTests<Quad, QuadWritable> {

    @Override
    protected boolean canSplitInputs() {
        return false;
    }

    @SuppressWarnings("deprecation")
    private void writeTuples(Dataset ds, Writer writer) {
        RDFDataMgr.write(writer, ds, RDFWriterRegistry.defaultSerialization(this.getRdfLanguage()));
    }

    /**
     * Gets the RDF language to write out generate tuples in
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();

    private void writeGoodTuples(Writer writer, int num) throws IOException {
        Dataset ds = DatasetFactory.createMem();
        Model m = ModelFactory.createDefaultModel();
        Resource currSubj = m.createResource("http://example.org/subjects/0");
        Property predicate = m.createProperty("http://example.org/predicate");
        for (int i = 0; i < num; i++) {
            if (i % 100 == 0) {
                ds.addNamedModel("http://example.org/graphs/" + (i / 100), m);
                m = ModelFactory.createDefaultModel();
            }
            if (i % 10 == 0) {
                currSubj = m.createResource("http://example.org/subjects/" + (i / 10));
            }
            m.add(currSubj, predicate, m.createTypedLiteral(i));
        }
        if (!m.isEmpty()) {
            ds.addNamedModel("http://example.org/graphs/extra", m);
        }
        this.writeTuples(ds, writer);
    }

    @Override
    protected final void generateTuples(Writer writer, int num) throws IOException {
        this.writeGoodTuples(writer, num);
        writer.close();
    }

    @Override
    protected final void generateMixedTuples(Writer writer, int num) throws IOException {
        // Write good data
        this.writeGoodTuples(writer, num / 2);

        // Write junk data
        for (int i = 0; i < num / 2; i++) {
            writer.write("junk data\n");
        }

        writer.flush();
        writer.close();
    }

    @Override
    protected final void generateBadTuples(Writer writer, int num) throws IOException {
        for (int i = 0; i < num; i++) {
            writer.write("junk data\n");
        }
        writer.flush();
        writer.close();
    }
}
