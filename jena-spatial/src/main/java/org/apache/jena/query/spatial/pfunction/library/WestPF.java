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

package org.apache.jena.query.spatial.pfunction.library;

import org.apache.jena.query.spatial.SpatialQuery;
import org.apache.jena.query.spatial.pfunction.DirectionWithPointPFBase;
import org.apache.jena.query.spatial.pfunction.SpatialMatch;

public class WestPF extends DirectionWithPointPFBase {

	/** Deconstruct the node or list object argument and make a SpatialMatch */
    @Override
    protected SpatialMatch getSpatialMatch(Double latitude, Double longitude, int limit) {
        SpatialMatch match = new SpatialMatch(SpatialQuery.ctx.getWorldBounds().getMinY(),
				SpatialQuery.ctx.getWorldBounds().getMinX(), SpatialQuery.ctx.getWorldBounds().getMaxY(),
				longitude, limit, getSpatialOperation());
		return match;
	}

}
