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

package org.apache.jena.shex.parser;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonException;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shex.ShexException;
import org.apache.jena.shex.ShexRecord;
import org.apache.jena.shex.ShapeMap;

/** Shape Expressions : JSON syntax */
public class ShExJ {
    /**
     * Parse the {@code InputStream} to get a ShEx shape map from JSON syntax.
     * @param input
     * @return ShexShapeMap
     */
    public static ShapeMap readShapeMapJson(InputStream input) {
        if ( input instanceof BufferedInputStream )
            input = new BufferedInputStream(input, 128*1024);
        JsonValue x = JSON.parseAny(input);
        if ( ! x.isArray() )
            throw new ShexException("Shex shape map: not a JSON array");
        List<ShexRecord> associations = new ArrayList<>();
        x.getAsArray().forEach(j->{
            if ( !j.isObject() ) {}
            ShexRecord a = parseShapeMapEntry(j.getAsObject());
            associations.add(a);
        });
        return ShapeMap.create(associations);
    }

    private static ShexRecord parseShapeMapEntry(JsonObject obj) {
        // Just enough to parse the maps in the validation test suite.

        // Full:
//      node: an RDF node, or a triple pattern which is used to select RDF nodes.
//      shape: ShEx shapeExprLabel or the string "START" for the start shape expression.
//      status: [default="conformant"] "nonconformant" or "conformant".
//      reason: [optional] a string stating a reason for failure or success.
//      appInfo: [optional] an application-specific JSON-LD structure

        try {
            String uri = getStrOrNull(obj, "node");
            if ( uri == null )
                throw new ShexException("Missing: required field: \"node\"");
            String shapeURI = getStrOrNull(obj, "shape");
            if ( shapeURI == null )
                throw new ShexException("Missing: required field: \"shape\"");

//            String status = getStrOrNull(obj, "status");
//            String reason = getStrOrNull(obj, "reason");
//            String appInfo = getStrOrNull(obj, "appInfo");
            Node nodeFocus = NodeFactory.createURI(uri);
            Node nodeShape = NodeFactory.createURI(shapeURI);
            return new ShexRecord(nodeFocus, nodeShape);
        } catch (JsonException ex) {
            throw new ShexException("Failed to parse shape map entry: "+JSON.toStringFlat(obj));
        }
    }

    private static String getStrOrNull(JsonObject obj, String field) {
        JsonValue jv = obj.get(field);
        if ( jv == null )
            return null;
        if ( jv.isString() )
            return jv.getAsString().value();
        return null ;
    }

}
