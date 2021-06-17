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

package org.apache.jena.shex;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shex.expressions.TripleExpression;
import org.apache.jena.shex.sys.SysShex;

/**
 * Shex Schema - a collection of shapes.
 */
public class ShexSchema {

    private final ShexShape startShape;
    private final List<ShexShape> shapes;
    private final Map<Node, ShexShape> shapeMap;
    private final Map<Node, TripleExpression> tripleRefs;

    private ShexSchema shapesWithImports = null;

    private final String sourceURI;
    private final String baseURI;
    private final PrefixMap prefixes;
    private final List<String> imports;

    public static ShexSchema shapes(String source, String baseURI, PrefixMap prefixes, ShexShape startShape,
                                    List<ShexShape> shapes, List<String> imports,
                                    Map<Node, TripleExpression> tripleRefs) {
        shapes = new ArrayList<>(shapes);
        Map<Node, ShexShape> shapeMap = new LinkedHashMap<>();
        for ( ShexShape shape:  shapes) {
        if ( shape.getLabel() == null )
            System.err.println("No shape label");
        else
            shapeMap.put(shape.getLabel(), shape);
        }

        tripleRefs = new LinkedHashMap<>(tripleRefs);

        return new ShexSchema(source, baseURI, prefixes, startShape, shapes, shapeMap, imports, tripleRefs);
    }

    /*package*/ ShexSchema(String source, String baseURI, PrefixMap prefixes,
                           ShexShape startShape, List<ShexShape> shapes, Map<Node, ShexShape> shapeMap,
                           List<String> imports, Map<Node, TripleExpression> tripleRefMap) {
        this.sourceURI = source;
        this.baseURI = baseURI;
        this.prefixes = prefixes;
        this.startShape = startShape;
        this.shapes = shapes;
        this.shapeMap = shapeMap;
        this.imports = imports;
        this.tripleRefs = tripleRefMap;
    }

    /*
     * Get START shape.
     * <p>
     * Returns null when there is no START shape.
     */
    public ShexShape getStart() {
        return startShape;
    }

    /** Get all the shapes. This includes the start shape, which has label {@link SysShex#startNode}. */
    public List<ShexShape> getShapes() {
        return shapes;
    }

    /** Get all the shapes. This includes the start shape, which has label {@link SysShex#startNode}. */
    public TripleExpression getTripleExpression(Node label) {
        return tripleRefs.get(label);
    }

    public boolean hasImports() {
        return imports != null && ! imports.isEmpty();
    }

    public List<String> getImports() {
        return imports;
    }

    public String getSource() {
        return sourceURI;
    }

    public String getBase() {
        return baseURI;
    }

    /**
     * Import form of this ShexShape collection.
     * This involves removing the START reference.
     */
    public ShexSchema importsClosure() {
        if ( shapesWithImports != null )
            return shapesWithImports;
        if ( imports == null || imports.isEmpty() )
            return this;
        synchronized(this) {
            if ( shapesWithImports != null )
                return shapesWithImports;

            // Lost the name of this set of shapes.
            // In a cyclic import, including this set of shapes, we will import self again.
            // Harmless.

            Set<String> importsVisited = new HashSet<>();
            if ( sourceURI != null )
                importsVisited.add(sourceURI);
            List<ShexSchema> others = new ArrayList<>();
            others.add(this);

            closure(imports, importsVisited, others);

            // Calculate the merge
            List<ShexShape> mergedShapes = new ArrayList<>();
            Map<Node, ShexShape> mergedShapeMap = new LinkedHashMap<>();
            Map<Node, TripleExpression> mergedTripleRefs = new LinkedHashMap<>();

            mergeOne(this, mergedShapes, mergedShapeMap, mergedTripleRefs);
            for ( ShexSchema importedSchema : others ) {
                mergeOne(importedSchema, mergedShapes, mergedShapeMap, mergedTripleRefs);
            }
            // Does not include the start shape.
            // The "get" operation of a ShexSchem know about "start"
//            if ( this.startShape != null )
//                mergedShapeMap.put(SysShex.startNode, startShape);
            shapesWithImports = new ShexSchema(sourceURI, baseURI, prefixes,
                                               startShape, mergedShapes, mergedShapeMap,
                                               null/*imports*/, mergedTripleRefs);
            return shapesWithImports;
        }
    }

    /** Merge a schema into the accumulators.
     * Any start node is skipped.
     */
    private static void mergeOne(ShexSchema schema,
                                 List<ShexShape> mergedShapes,
                                 Map<Node, ShexShape> mergedShapeMap,
                                 Map<Node, TripleExpression> mergedTripleRefs
                                 ) {
        // Without start shape.
        schema.getShapes().stream().filter(sh->!SysShex.startNode.equals(sh.getLabel())).forEach(shape->{
            mergedShapes.add(shape);
            mergedShapeMap.put(shape.getLabel(), shape);
        });
        mergedTripleRefs.putAll(schema.tripleRefs);
    }

    private static void closure(List<String> imports, Set<String> importsVisited, List<ShexSchema> visited) {
        if ( imports == null || imports.isEmpty() )
            return;
        for ( String imp : imports ) {
            if ( importsVisited.contains(imp) )
                continue;
            importsVisited.add(imp);
            ShexSchema others = Shex.readSchema(imp);
            visited.add(others);
            closure(others.imports, importsVisited, visited);
        }
    }

    public ShexShape get(Node n) {
        if ( SysShex.startNode.equals(n) )
            return startShape;
        return shapeMap.get(n);
    }

    public boolean hasShape(Node n) {
        return shapeMap.containsKey(n);
    }

    public PrefixMap getPrefixMap() { return prefixes; }
}
