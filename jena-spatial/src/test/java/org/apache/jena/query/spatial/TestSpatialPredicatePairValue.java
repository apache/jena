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

import junit.framework.TestCase;

import org.junit.Test;

public class TestSpatialPredicatePairValue {

	@Test public void testEqualsAndHashcode(){
		SpatialPredicatePair pair1 = new SpatialPredicatePair(
				EntityDefinition.geo_latitude.asNode(),
				EntityDefinition.geo_longitude.asNode());
		SpatialPredicatePair pair2 = new SpatialPredicatePair(
				EntityDefinition.geo_latitude.asNode(),
				EntityDefinition.geo_longitude.asNode());
		TestCase.assertTrue(pair1.equals(pair2));
		TestCase.assertTrue(pair1.hashCode() == pair2.hashCode());

		SpatialPredicatePairValue value1 = new SpatialPredicatePairValue(pair1);
		value1.setValue(EntityDefinition.geo_latitude.asNode(), 12.0);
		value1.setValue(EntityDefinition.geo_longitude.asNode(), 23.0);

		SpatialPredicatePairValue value2 = new SpatialPredicatePairValue(pair2);
		value2.setValue(EntityDefinition.geo_latitude.asNode(), 12.0);
		value2.setValue(EntityDefinition.geo_longitude.asNode(), 23.0);

		TestCase.assertTrue(value1.equals(value2));
		TestCase.assertTrue(value1.hashCode() == value2.hashCode());
		
		value1.setValue(EntityDefinition.geo_latitude.asNode(),null);
		value2.setValue(EntityDefinition.geo_latitude.asNode(),null);
		
		TestCase.assertTrue(value1.equals(value2));
		TestCase.assertTrue(value1.hashCode() == value2.hashCode());
		
		value2.setValue(EntityDefinition.geo_latitude.asNode(),23.0);
		TestCase.assertFalse(value1.equals(value2));
		TestCase.assertFalse(value1.hashCode() == value2.hashCode());
	}

}
