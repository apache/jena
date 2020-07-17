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

package org.apache.jena.shacl.compact;

import java.io.OutputStream;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.compact.writer.CompactWriter;

public class ShaclcWriter {

    /** Write shapes in <a href="https://w3c.github.io/shacl/shacl-compact-syntax/">SHACL Compact Syntax</a> (July 2020). */
    public static void print(OutputStream output, Shapes shapes) {
        IndentedWriter out = new IndentedWriter(output);
        out.setUnitIndent(4);
        print(out, shapes);
    }

    /** Write shapes in <a href="https://w3c.github.io/shacl/shacl-compact-syntax/">SHACL Compact Syntax</a> (July 2020). */
    public static void print(IndentedWriter out, Shapes shapes) {
        CompactWriter.print(out, shapes);
    }
}

