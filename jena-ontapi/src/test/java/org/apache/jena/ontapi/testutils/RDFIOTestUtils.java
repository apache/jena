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

package org.apache.jena.ontapi.testutils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class RDFIOTestUtils {

    public static String asString(Model model, Lang ext) {
        return toStringWriter(model, ext).toString();
    }

    public static StringWriter toStringWriter(Model model, Lang lang) {
        StringWriter sw = new StringWriter();
        model.write(sw, lang.getName(), null);
        return sw;
    }

    public static Model loadResourceAsModel(String resource, Lang lang) {
        return readResourceToModel(ModelFactory.createDefaultModel(), resource, lang);
    }

    @SuppressWarnings("unchecked")
    public static <X extends Model> X readResourceToModel(X m, String resource, Lang lang) {
        try (InputStream in = Objects.requireNonNull(RDFIOTestUtils.class.getResourceAsStream(resource))) {
            return (X) m.read(in, null, lang.getName());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void save(Model data, Path file, Lang lang) {
        try (OutputStream out = Files.newOutputStream(file)) {
            data.write(out, lang.getName());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    public static Model readStringAsModel(String content, String lang) {
        var r = new StringReader(content);
        var m = ModelFactory.createDefaultModel();
        m.read(r, null, lang);
        return m;
    }
}
