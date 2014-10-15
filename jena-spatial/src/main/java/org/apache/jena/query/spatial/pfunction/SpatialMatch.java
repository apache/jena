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

package org.apache.jena.query.spatial.pfunction;

import org.apache.jena.query.spatial.DistanceUnitsUtils;
import org.apache.jena.query.spatial.SpatialQuery;
import org.apache.lucene.spatial.query.SpatialOperation;

import com.spatial4j.core.shape.Shape;

public class SpatialMatch {

	private final Shape shape;
	private final int limit;
	private final SpatialOperation operation;

	public SpatialMatch(Double latitude, Double longitude, Double radius,
			String units, int limit, SpatialOperation operation) {

		double degrees = DistanceUnitsUtils.dist2Degrees(radius, units);
		this.shape = SpatialQuery.ctx.makeCircle(longitude, latitude, degrees);
		//System.out.println( SpatialQuery.ctx.toString(shape) );
		this.limit = limit;
		this.operation = operation;
	}

	public SpatialMatch(Double latitude1, Double longitude1, Double latitude2,
			Double longitude2, int limit, SpatialOperation operation) {
		this.shape = SpatialQuery.ctx.makeRectangle(longitude1, longitude2, latitude1, latitude2);
		this.limit = limit;
		this.operation = operation;
	}

	public Shape getShape() {
		return shape;
	}

	public int getLimit() {
		return limit;
	}

	public SpatialOperation getSpatialOperation() {
		return operation;
	}

	@Override
	public String toString() {
		return "(" + shape + " " + limit + " " + operation + ")";
	}

}
