/*
 * Copyright 2019 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;

/**
 *
 *
 */
public class ModeSRS {

    private final HashMap<String, Integer> srsMap = new HashMap<>();
    private List<Entry<String, Integer>> srsList = new ArrayList<>();

    public ModeSRS() {
    }

    public void search(Model model) {

        NodeIterator nodeIter = model.listObjectsOfProperty(Geo.HAS_SERIALIZATION_PROP);
        boolean isGeometryLiteralsFound = nodeIter.hasNext();
        while (nodeIter.hasNext()) {
            RDFNode node = nodeIter.next();
            if (node.isLiteral()) {
                GeometryWrapper geometryWrapper = GeometryWrapper.extract(node.asLiteral());
                String srsURI = geometryWrapper.getSrsURI();

                //Put the SRS URI into the map.
                Integer count;
                if (srsMap.containsKey(srsURI)) {
                    count = srsMap.get(srsURI);
                    count++;
                } else {
                    count = 1;
                }
                srsMap.put(srsURI, count);
            }
        }

        if (!isGeometryLiteralsFound) {
            //No GeometryLiterals so check for Geo predicates use.
            List<RDFNode> geoList = model.listObjectsOfProperty(SpatialExtension.GEO_LAT_PROP).toList();

            if (!geoList.isEmpty()) {
                srsMap.put(SRS_URI.WGS84_CRS, geoList.size());
            }
        }

        srsList = srsMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toList());
    }

    public List<Entry<String, Integer>> getSrsList() {
        return srsList;
    }

    public HashMap<String, Integer> getSrsMap() {
        return srsMap;
    }

    public String getModeURI() throws SrsException {

        if (!srsList.isEmpty()) {
            return srsList.get(0).getKey();
        } else {
            throw new SrsException("No SRS found. Check 'http://www.opengis.net/ont/geosparql#hasSerialisation' or 'http://www.w3.org/2003/01/geo/wgs84_pos#lat'/'http://www.w3.org/2003/01/geo/wgs84_pos#lon' predicates are present in the source data. Hint: Inferencing with GeoSPARQL schema may be required.");
        }
    }

    @Override
    public String toString() {
        return "ModeSRS{" + "srsMap=" + srsMap + ", srsList=" + srsList + '}';
    }

}
