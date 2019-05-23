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
package org.apache.jena.geosparql.implementation.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.measure.Unit;
import javax.measure.quantity.Length;
import org.apache.jena.geosparql.implementation.UnitsOfMeasure;
import org.apache.jena.geosparql.implementation.vocabulary.Unit_URI;
import org.apache.sis.measure.Units;

/**
 *
 *
 */
public class UnitsRegistry {

    private static final Map<String, Unit<?>> UNITS_REGISTRY = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Unit<?>, String> UNITS_URI_REGISTRY = Collections.synchronizedMap(new HashMap<>());

    private static final Unit<Length> YARD = Units.METRE.multiply(1.0936132983);

    static {

        //Linear Units
        //URL
        addUnit(Unit_URI.METER_URL, Units.METRE);
        addUnit(Unit_URI.METRE_URL, Units.METRE);
        addUnit(Unit_URI.KILOMETER_URL, Units.KILOMETRE);
        addUnit(Unit_URI.KILOMETRE_URL, Units.KILOMETRE);
        addUnit(Unit_URI.CENTIMETER_URL, Units.CENTIMETRE);
        addUnit(Unit_URI.CENTIMETRE_URL, Units.CENTIMETRE);
        addUnit(Unit_URI.MILLIMETER_URL, Units.MILLIMETRE);
        addUnit(Unit_URI.MILLIMETRE_URL, Units.MILLIMETRE);

        //URN
        addUnit(Unit_URI.METRE_URN, Units.METRE);
        addUnit(Unit_URI.KILOMETRE_URN, Units.KILOMETRE);
        addUnit(Unit_URI.CENTIMETRE_URN, Units.CENTIMETRE);

        //Non-SI Linear Units
        //URL
        addUnit(Unit_URI.STATUTE_MILE_URL, Units.STATUTE_MILE);
        addUnit(Unit_URI.MILE_URL, Units.STATUTE_MILE);
        addUnit(Unit_URI.YARD_URL, YARD);
        addUnit(Unit_URI.FOOT_URL, Units.FOOT);
        addUnit(Unit_URI.INCH_URL, Units.INCH);
        addUnit(Unit_URI.NAUTICAL_MILE_URL, Units.NAUTICAL_MILE);
        addUnit(Unit_URI.US_SURVEY_FOOT_URL, Units.US_SURVEY_FOOT);

        //URN
        addUnit(Unit_URI.STATUTE_MILE_URN, Units.STATUTE_MILE);
        addUnit(Unit_URI.FOOT_URN, Units.FOOT);
        addUnit(Unit_URI.YARD_URN, YARD);
        addUnit(Unit_URI.NAUTICAL_MILE_URN, Units.NAUTICAL_MILE);
        addUnit(Unit_URI.US_SURVEY_FOOT_URN, Units.US_SURVEY_FOOT);

        //Angular Units
        //URL
        addUnit(Unit_URI.RADIAN_URL, Units.RADIAN);
        addUnit(Unit_URI.MICRORADIAN_URL, Units.MICRORADIAN);
        addUnit(Unit_URI.DEGREE_URL, Units.DEGREE);
        addUnit(Unit_URI.ARC_MINUTE_URL, Units.ARC_MINUTE);
        addUnit(Unit_URI.ARC_SECOND_URL, Units.ARC_SECOND);
        addUnit(Unit_URI.GRAD_URL, Units.GRAD);

        //URN
        addUnit(Unit_URI.RADIAN_URN, Units.RADIAN);
        addUnit(Unit_URI.MICRORADIAN_URN, Units.MICRORADIAN);
        addUnit(Unit_URI.DEGREE_URN, Units.DEGREE);
        addUnit(Unit_URI.ARC_MINUTE_URN, Units.ARC_MINUTE);
        addUnit(Unit_URI.ARC_SECOND_URN, Units.ARC_SECOND);
        addUnit(Unit_URI.GRAD_URN, Units.GRAD);

        //URN references from: https://sis.apache.org/apidocs/org/apache/sis/measure/Units.html
        //TODO: EPSG also defined units URIs at https://epsg.io/9096-units. More exhaustive than OGC.
    }

    public static final void addUnit(String unitURI, Unit<?> unit) {
        UNITS_REGISTRY.putIfAbsent(unitURI, unit);
        UNITS_URI_REGISTRY.putIfAbsent(unit, unitURI);
    }

    public static final void addUnit(UnitsOfMeasure unitsOfMeasure) {
        addUnit(unitsOfMeasure.getUnitURI(), unitsOfMeasure.getUnit());
    }

    public static final Unit<?> getUnit(String unitURI) {
        if (UNITS_REGISTRY.containsKey(unitURI)) {
            return UNITS_REGISTRY.get(unitURI);
        } else {
            throw new UnitsURIException("Unrecognised unit URI: " + unitURI);
        }
    }

    public static final String getUnitURI(UnitsOfMeasure unitOfMeasure) {
        return getUnitURI(unitOfMeasure.getUnit());
    }

    public static final String getUnitURI(Unit<?> unit) {
        if (UNITS_URI_REGISTRY.containsKey(unit)) {
            return UNITS_URI_REGISTRY.get(unit);
        } else {
            throw new UnitsURIException("Unrecognised unit: " + unit);
        }
    }

    public static final Boolean isLinearUnits(String unitURI) {

        if (UNITS_REGISTRY.containsKey(unitURI)) {

            Unit<?> unit = UNITS_REGISTRY.get(unitURI);
            Unit<?> unitSI = unit.getSystemUnit();

            return unitSI.equals(Units.METRE);
        } else {
            throw new UnitsURIException("Unrecognised unit URI: " + unitURI);
        }
    }

}
