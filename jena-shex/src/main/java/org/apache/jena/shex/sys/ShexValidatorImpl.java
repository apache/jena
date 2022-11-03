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

package org.apache.jena.shex.sys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shex.*;

class ShexValidatorImpl implements ShexValidator{

    /** Return the current system-wide {@code ShexValidator}. */
    public static ShexValidator get() { return SysShex.get();}

    /** Validate data using a collection of shapes and a shape map */
    @Override
    public ShexReport validate(Graph dataGraph, ShexSchema shapes, ShapeMap shapeMap) {
        Objects.requireNonNull(dataGraph);
        Objects.requireNonNull(shapes);
        Objects.requireNonNull(shapeMap);
        shapes = shapes.importsClosure();
        ValidationContext vCxt = new ValidationContext(dataGraph, shapes);
        List<ShexRecord> reports = new ArrayList<>();
        shapeMap.entries().forEach(mapEntry->{
            Collection<Node> focusNodes = focusNodes(dataGraph, mapEntry);
            if ( focusNodes == null )
                throw new InternalErrorException("Shex shape mapping has no node and no pattern");
            // The validation work for this map entry.
            for ( Node focus : focusNodes ) {
                validationStep(vCxt, mapEntry, mapEntry.shapeExprLabel, focus);
            }
        });

        ShexReport report = vCxt.generateReport();
        return report;
    }

    /** Validate a specific node (the focus), with a specific shape in a set of shapes. */
    @Override
    public ShexReport validate(Graph dataGraph, ShexSchema shapes, Node shapeRef, Node focus) {
        Objects.requireNonNull(shapeRef);
        Objects.requireNonNull(focus);
        Objects.requireNonNull(shapes);
        Objects.requireNonNull(dataGraph);
        ShexRecord entry = new ShexRecord(focus, shapeRef);
        shapes = shapes.importsClosure();
        ValidationContext vCxt = new ValidationContext(dataGraph, shapes);
        boolean isValid = validationStep(vCxt, entry, shapeRef, focus);
        return vCxt.generateReport();
    }

    /** Validate a specific node (the focus), against a given shape. */
    @Override
    public ShexReport validate(Graph dataGraph, ShexSchema shapes, ShexShape shape, Node focus) {
        Objects.requireNonNull(shape);
        Objects.requireNonNull(shapes);
        Objects.requireNonNull(dataGraph);
        Objects.requireNonNull(focus);
        ShexRecord entry = new ShexRecord(focus, shape.getLabel());
        shapes = shapes.importsClosure();
        ValidationContext vCxt = new ValidationContext(dataGraph, shapes);
        boolean isValid = validationStep(vCxt, entry, entry.shapeExprLabel, focus);
        return vCxt.generateReport();
    }

    @Override
    public ShexReport validate(Graph dataGraph, ShexSchema shapes, ShapeMap shapeMap, Node focus) {
        Objects.requireNonNull(shapes);
        Objects.requireNonNull(dataGraph);
        Objects.requireNonNull(shapeMap);
        Objects.requireNonNull(focus);
        shapes = shapes.importsClosure();
        ValidationContext vCxt = new ValidationContext(dataGraph, shapes);
        List<ShexRecord> reports = new ArrayList<>();
        shapeMap.entries().forEach(mapEntry->{
            validateOneShapeRecord(vCxt, mapEntry, focus);
        });

        ShexReport report = vCxt.generateReport();
        return report;
    }

    // Execute validation if the focus node is in the scope of the shapeRecord.
    private static boolean validateOneShapeRecord(ValidationContext vCxt, ShexRecord shapeRecord, Node focusNode) {
        Collection<Node> focusNodes = focusNodes(vCxt.getData(), shapeRecord);
        if ( focusNodes == null )
            throw new InternalErrorException("Shex shape mapping has no node and no pattern");
        if ( ! focusNodes.contains(focusNode) )
            return true;
        return validationStep(vCxt, shapeRecord, shapeRecord.shapeExprLabel, focusNode);
    }

    private static Collection<Node> focusNodes(Graph graph, ShexRecord  mapRecord) {
        if ( mapRecord.node != null ) {
            return List.of(mapRecord.node);
        }
        if ( mapRecord.pattern != null ) {
            Triple t = mapRecord.asMatcher();
            Collection<Node> focusNodes = graph.find(t)
                    .mapWith(triple->focusFromRecord(mapRecord, triple))
                    .toSet();
            return focusNodes;
        }
        return null;
    }

    private static Node focusFromRecord(ShexRecord mapRecord, Triple triple) {
        if ( mapRecord.isSubjectFocus() )
            return triple.getSubject();
        if ( mapRecord.isObjectFocus() )
            return triple.getObject();
        return null;
    }

    // Entry point for all validation.
    private static boolean validationStep(ValidationContext vCxt, ShexRecord mapEntry, Node shapeRef, Node focus) {
        track(mapEntry.shapeExprLabel, focus);
        // Isolate.
        ShexShape shape = vCxt.getShape(shapeRef);
        if ( shape == null ) {
            // No such shape.
            vCxt.getShape(shapeRef);
            String msg = "No such shape: "+ShexLib.displayStr(shapeRef);
            ReportItem item = new ReportItem(msg, shapeRef);
            vCxt.reportEntry(item);
            report(vCxt, mapEntry, shapeRef, ShexStatus.nonconformant, msg);
            return false;
        }
        return validationStepWorker(vCxt, mapEntry, shape, shapeRef, focus);
    }

    // Worker.
    private static boolean validationStepWorker(ValidationContext vCxt, ShexRecord mapEntry, ShexShape shape, Node shapeRef, Node focus) {
        // Isolate report entries.
        ValidationContext vCxtInner = ValidationContext.create(vCxt);
        vCxtInner.startValidate(shape, focus);
        boolean isValid = shape.satisfies(vCxtInner, focus);
        vCxtInner.finishValidate(shape, focus);
        if ( ! isValid ) {
            atLeastOneReportItem(vCxtInner, focus);
            vCxtInner.copyInto(vCxt); // Report items.
        }
        createShexReportLine(vCxt, mapEntry, isValid, shapeRef, focus);
        return isValid;
    }

    private static void createShexReportLine(ValidationContext vCxt, ShexRecord mapEntry, boolean conforms, Node shapeRef, Node focus) {
        // Shex shapes report.
        if ( conforms ) {
            report(vCxt, mapEntry, focus, ShexStatus.conformant, null);
            return;
        }

        if ( mapEntry == null )
            return;

        ReportItem item = ListUtils.last(vCxt.getReportItems());
        String reason = (item!=null) ? item.getMessage() : null;
        report(vCxt, mapEntry, focus, ShexStatus.nonconformant, reason);
    }

    private static void atLeastOneReportItem(ValidationContext vCxt, Node focus) {
        // Ensure at least one entry.
        if ( vCxt.getReportItems().isEmpty() ) {
            ReportItem reportItem = new ReportItem("Failed", focus);
            vCxt.reportEntry(reportItem);
        }
    }

    private static void report(ValidationContext vCxt, ShexRecord entry, Node focusNode, ShexStatus result, String reason) {
        vCxt.shexReport(entry, focusNode, result, reason);
    }

    private static void track(Node shapeExprLabel, Node focus) {}
}
