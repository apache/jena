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

package org.apache.jena.ttl_test.test.turtle;

import junit.framework.TestCase;
import org.apache.jena.rdf.model.*;
import org.apache.jena.ttl_test.turtle.TurtleParseException;
import org.apache.jena.ttl_test.turtle.TurtleReader;

public class TestItemSyntax extends TestCase {
    String uri;
    public TestItemSyntax(String name, String uri) {
        super(name);
        this.uri = uri;
    }

    @Override
    public void runTest() {
        Model model = ModelFactory.createDefaultModel();
        RDFReaderI t = new TurtleReader();
        try {
            t.read(model, uri);
        } catch (TurtleParseException ex) {
            throw ex;
        }
    }

}
