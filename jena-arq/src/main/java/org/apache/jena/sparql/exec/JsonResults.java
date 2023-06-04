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

package org.apache.jena.sparql.exec;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.lib.RDFTerm2Json;

/**
 * ARQ supports an extension query type:
 * <pre>
 *    JSON {
 *       "x": ?var1 ,
 *       "y": ?var2
 *    } WHERE {
 *       ...
 *    }
 * </pre>
 */
public class JsonResults {

    public static JsonArray results(QueryIterator queryIterator, Map<String, Node> template) {
        Iterator<JsonObject> iter = iterator(queryIterator, template);
        JsonArray array = new JsonArray();
        iter.forEachRemaining(x->array.add(x));
        return array;
    }

    public static Iterator<JsonObject> iterator(QueryIterator queryIterator, Map<String, Node> template) {
        return new JsonResultsIterator(queryIterator, template);
    }

    private static class JsonResultsIterator implements Iterator<JsonObject> {
        private final QueryIterator queryIterator;
        private final Map<String, Node> template;

        public JsonResultsIterator(QueryIterator queryIterator, Map<String, Node> template) {
            this.queryIterator = queryIterator;
            this.template = template;
        }

        @Override
        public boolean hasNext() {
            if ( queryIterator == null )
                return false;
            boolean r = queryIterator.hasNext();
            if ( !r )
                close();
            return r;
        }

        @Override
        public JsonObject next() {
            if ( ! hasNext() )
                throw new NoSuchElementException(this.getClass() + ".next");
            Binding binding = queryIterator.next();
            JsonObject jsonObject = JsonResults.generateJsonObject(binding, template);
            return jsonObject;
        }

        @Override
        public void forEachRemaining(Consumer<? super JsonObject> action) {
            if ( queryIterator == null )
                return;
            queryIterator.forEachRemaining(binding
                    -> action.accept( JsonResults.generateJsonObject(binding, template) ));
            close();
        }

        /** Close the query iterator */
        private void close() {
            queryIterator.close();
        }
    }

    private static JsonObject generateJsonObject(Binding binding, Map<String, Node> template) {
        JsonObject jsonObject = new JsonObject();
        template.forEach((field, node)->{
            Node n;
            if ( Var.isVar(node) ) {
                // Variable: "field": ?var
                n = binding.get(Var.alloc(node));
            } else {
                // Constant: "field": "something"
                n = node;
            }
            if ( n != null) {
                JsonValue value = RDFTerm2Json.fromNode(n);
                jsonObject.put(field, value);
            }
        });
        return jsonObject;
    }


}
