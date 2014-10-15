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

package org.apache.jena.query.spatial;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.spatial4j.core.context.SpatialContextFactory;

/**
 * Definition of a "document"
 */
@SuppressWarnings("unused")
public class EntityDefinition {

	private final String entityField;
	private final String geoField;
	private final Set<Node> WKTPredicates;
	private final Set<Node> builtinWKTPredicates;
	private final Set<SpatialPredicatePair> spatialPredicatePairs;
	private final Set<SpatialPredicatePair> builtinSpatialPredicatePairs;

	private final static String geo_ns = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	public final static Property geo_latitude = ResourceFactory
			.createProperty(geo_ns + "lat");
	public final static Property geo_longitude = ResourceFactory
			.createProperty(geo_ns + "long");
	public final static Property geo_geometry = ResourceFactory
			.createProperty(geo_ns + "geometry");

	public final static Property geosparql_asWKT = ResourceFactory
			.createProperty("http://www.opengis.net/ont/geosparql#asWKT");

	public final static Resource geosparql_wktLiteral = ResourceFactory
			.createResource("http://www.opengis.net/ont/geosparql#wktLiteral");

	/**
	 * @param entityField
	 *            The entity being indexed (e.g. it's URI).
	 */
	public EntityDefinition(String entityField, String geoField) {
		this.entityField = entityField == null || entityField.isEmpty() ? "entityField"
				: entityField;
		this.geoField = geoField == null || geoField.isEmpty() ? "geoField"
				: geoField;
		this.WKTPredicates = new HashSet<Node>();
		this.builtinWKTPredicates = new HashSet<Node>();
		this.spatialPredicatePairs = new HashSet<SpatialPredicatePair>();
		this.builtinSpatialPredicatePairs = new HashSet<SpatialPredicatePair>();
		initBuiltinPredicates();
	}

	public void setSpatialContextFactory(String spatialContextFactoryClass) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("spatialContextFactory", spatialContextFactoryClass);
		SpatialQuery.ctx = SpatialContextFactory.makeSpatialContext(map,
				SpatialQuery.class.getClassLoader());
	}

	private void initBuiltinPredicates() {
		addBuiltinWKTPredicate(geo_geometry);
		addBuiltinWKTPredicate(geosparql_asWKT);
		addBuiltinSpatialPredicatePair(geo_latitude, geo_longitude);
	}

	private boolean addBuiltinWKTPredicate(Resource predicate) {
		builtinWKTPredicates.add(predicate.asNode());
		return addWKTPredicate(predicate);
	}

	public boolean addWKTPredicate(Resource predicate) {
		return WKTPredicates.add(predicate.asNode());
	}

	private boolean addBuiltinSpatialPredicatePair(Resource latitude_predicate,
			Resource longitude_predicate) {
		builtinSpatialPredicatePairs.add(new SpatialPredicatePair(
				latitude_predicate.asNode(), longitude_predicate.asNode()));
		return addSpatialPredicatePair(latitude_predicate, longitude_predicate);
	}

	public boolean addSpatialPredicatePair(Resource latitude_predicate,
			Resource longitude_predicate) {
		return spatialPredicatePairs.add(new SpatialPredicatePair(
				latitude_predicate.asNode(), longitude_predicate.asNode()));
	}

	public String getEntityField() {
		return entityField;
	}

	public String getGeoField() {
		return geoField;
	}

	public boolean isWKTPredicate(Node predicate) {
		return this.WKTPredicates.contains(predicate);
	}

	public boolean isSpatialPredicate(Node predicate) {
		return this.getSpatialPredicatePair(predicate) != null;
	}

	public boolean isLatitudePredicate(Node predicate) {
		return this.getSpatialPredicatePair(predicate).getLatitudePredicate()
				.equals(predicate);
	}

	public SpatialPredicatePair getSpatialPredicatePair(Node predicate) {
        for ( SpatialPredicatePair pair : this.spatialPredicatePairs )
        {
            if ( pair.getLatitudePredicate().equals( predicate ) || pair.getLongitudePredicate().equals( predicate ) )
            {
                return pair;
            }
        }
		return null;
	}

	public int getCustomSpatialPredicatePairCount() {
		return this.spatialPredicatePairs.size()
				- builtinSpatialPredicatePairs.size();
	}

	public int getSpatialPredicatePairCount() {
		return this.spatialPredicatePairs.size();
	}

	public int getCustomWKTPredicateCount() {
		return this.WKTPredicates.size() - builtinWKTPredicates.size();
	}

	public int getWKTPredicateCount() {
		return this.WKTPredicates.size();
	}

	public boolean hasSpatialPredicatePair(Node latitude_predicate,
			Node longitude_predicate) {
        for ( SpatialPredicatePair pair : this.spatialPredicatePairs )
        {
            if ( pair.getLatitudePredicate().equals( latitude_predicate ) && pair.getLongitudePredicate().equals(
                longitude_predicate ) )
            {
                return true;
            }
        }
		return false;
	}

	@Override
	public String toString() {
		return entityField + ":" + geoField;

	}
}
