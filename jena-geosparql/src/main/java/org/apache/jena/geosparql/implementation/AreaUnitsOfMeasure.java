/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.geosparql.implementation;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Area;

import org.apache.jena.geosparql.implementation.registry.UnitsRegistry;
import org.apache.jena.geosparql.implementation.registry.UnitsURIException;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.sis.measure.Units;

/**
 * Explicit area units accepted by {@code geof:area}.
 *
 * This registry is separate from {@link UnitsRegistry}, which does not provide
 * quantity-typed lookup. Units added only to {@code UnitsRegistry} are not
 * accepted as area units here.
 */
final class AreaUnitsOfMeasure {
    private static final Map<String, Unit<Area>> UNITS = new HashMap<>();

    static {
        UNITS.put(Unit_URI.SQUARE_METRE_QUDT, Units.SQUARE_METRE);
        UNITS.put(Unit_URI.SQUARE_KILOMETRE_QUDT, square(Units.KILOMETRE));
        UNITS.put(Unit_URI.SQUARE_CENTIMETRE_QUDT, square(Units.CENTIMETRE));
        UNITS.put(Unit_URI.SQUARE_MILLIMETRE_QUDT, square(Units.MILLIMETRE));
        UNITS.put(Unit_URI.SQUARE_FOOT_QUDT, square(Units.FOOT));
        UNITS.put(Unit_URI.SQUARE_YARD_QUDT, square(Units.FOOT.multiply(3)));
        UNITS.put(Unit_URI.SQUARE_INCH_QUDT, square(Units.INCH));
        UNITS.put(Unit_URI.SQUARE_MILE_QUDT, square(Units.STATUTE_MILE));
        UNITS.put(Unit_URI.HECTARE_QUDT, Units.HECTARE);
        UNITS.put(Unit_URI.ACRE_QUDT, Units.FOOT.pow(2).multiply(43560).asType(Area.class));
    }

    private AreaUnitsOfMeasure() {
    }

    static Unit<Area> getUnit(String uri) {
        Unit<Area> unit = UNITS.get(uri);
        if (unit != null) {
            return unit;
        }
        try {
            UnitsRegistry.getUnit(uri);
        } catch (UnitsURIException ex) {
            throw new UnitsURIException("Unrecognised area unit URI: " + uri, ex);
        }
        throw new UnitsConversionException("Area requires an area unit: " + uri);
    }

    private static Unit<Area> square(Unit<?> lengthUnit) {
        return lengthUnit.pow(2).asType(Area.class);
    }
}
