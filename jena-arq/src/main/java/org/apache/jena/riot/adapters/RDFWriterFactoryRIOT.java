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

package org.apache.jena.riot.adapters;

import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.RDFWriterF;

/** Adapter to old style Jena writer factory */
public class RDFWriterFactoryRIOT implements RDFWriterF {
    public RDFWriterFactoryRIOT() {}

    @Override
    public RDFWriter getWriter() {
        return getWriter(null);
    }

    @Override
    public RDFWriter getWriter(String langname) {
        return new RDFWriterRIOT(langname);
    }

    @Override
    public String setWriterClassName(String lang, String className) {
        return null;
    }

    @Override
    public void resetRDFWriterF() {
        // does nothing as the reader can not be modified.

    }

    @Override
    public String removeWriter(String lang) {
        return null;
    }
}
