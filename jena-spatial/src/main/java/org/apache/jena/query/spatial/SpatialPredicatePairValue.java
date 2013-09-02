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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

public class SpatialPredicatePairValue {

	private static Logger log = LoggerFactory
			.getLogger(SpatialPredicatePairValue.class);

	public SpatialPredicatePairValue(SpatialPredicatePair pair) {
		this.pair = pair;
	}

	private SpatialPredicatePair pair;
	private Double latitudeValue;
	private Double longitudeValue;

	public Double getLatitudeValue() {
		return latitudeValue;
	}

	public Double getLongitudeValue() {
		return longitudeValue;
	}

	public SpatialPredicatePair getPair() {
		return pair;
	}

	public void setValue(Node predicate, Double value) {
		if (predicate.equals(pair.getLatitudePredicate())) {
			this.latitudeValue = value;
		} else if (predicate.equals(pair.getLongitudePredicate())) {
			this.longitudeValue = value;
		} else {
			log.warn("Try to set value to a SpatialPredicatePairValue with no such predicate: "
					+ predicate + " :: " + value);
		}

	}

	public Double getTheOtherValue(Node predicate) {
		if (pair.getLatitudePredicate().equals(predicate)) {
			return this.getLongitudeValue();
		} else if (predicate.equals(pair.getLongitudePredicate())) {
			return this.getLatitudeValue();
		} else {
			log.warn("Try to get value to a SpatialPredicatePairValue with no such predicate: "
					+ predicate);
			return null;
		}
	}

	@Override
	public int hashCode() {
		int latitudeHashCode = latitudeValue == null ? 0 : latitudeValue
				.hashCode() * 17;
		int longitudeHashCode = longitudeValue == null ? 0 : longitudeValue
				.hashCode() * 19;
		return pair.hashCode() * 11 + latitudeHashCode + longitudeHashCode;
	}

	@Override
	public boolean equals(Object otherObject) {
		// a quick test to see if the objects are identical
		if (this == otherObject)
			return true;

		// must return false if the explicit parameter is null
		if (otherObject == null)
			return false;

		// if the classes don't match, they can't be equal
		if (getClass() != otherObject.getClass())
			return false;

		// now we know otherObject is a non-null Employee
		SpatialPredicatePairValue other = (SpatialPredicatePairValue) otherObject;

		boolean latitudeValueEquals = this.latitudeValue == null ? other.latitudeValue == null
				: this.latitudeValue.equals(other.latitudeValue);
		boolean longitudeValueEquals = this.longitudeValue == null ? other.longitudeValue == null
				: this.longitudeValue.equals(other.longitudeValue);

		// test whether the fields have identical values
		return pair.equals(other.pair) && latitudeValueEquals
				&& longitudeValueEquals;
	}
}
