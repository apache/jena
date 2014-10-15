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

import java.util.ArrayList;
import java.util.List;

import com.spatial4j.core.distance.DistanceUtils;

public class DistanceUnitsUtils {

    public final static String defaultDistanceUnit = "kilometres" ;
    
	public final static List<String> SUPPORTED_UNITS;
	static {
	 // International spelling "metres" 
	 // As used by http://en.wikipedia.org/wiki/International_Bureau_of_Weights_and_Measures
		SUPPORTED_UNITS = new ArrayList<String>();
		SUPPORTED_UNITS.add("kilometres");           
        SUPPORTED_UNITS.add("kilometers");           // America spelling
		SUPPORTED_UNITS.add("km");
		SUPPORTED_UNITS.add("meters");        
        SUPPORTED_UNITS.add("metres");
		SUPPORTED_UNITS.add("m");
		SUPPORTED_UNITS.add("centimeters");
        SUPPORTED_UNITS.add("centimetres");
		SUPPORTED_UNITS.add("cm");
		SUPPORTED_UNITS.add("millimetres");
        SUPPORTED_UNITS.add("millimeters");
        SUPPORTED_UNITS.add("mm");
		SUPPORTED_UNITS.add("miles");
		SUPPORTED_UNITS.add("mi");
		SUPPORTED_UNITS.add("degrees");
		SUPPORTED_UNITS.add("de");
	}

	public static double dist2Degrees(double dist, String units) {
		double degrees = dist;

		if (units.equals("kilometers") || units.equals("kilometres") || units.equals("km"))
			return DistanceUtils.dist2Degrees(dist,
					DistanceUtils.EARTH_MEAN_RADIUS_KM);

		else if (units.equals("meters") || units.equals("metres") || units.equals("m"))
			return DistanceUtils.dist2Degrees(dist / 1000,
					DistanceUtils.EARTH_MEAN_RADIUS_KM);

		else if (units.equals("centimeters") || units.equals("centimetres") || units.equals("cm"))
			return DistanceUtils.dist2Degrees(dist / (1000 * 100),
					DistanceUtils.EARTH_MEAN_RADIUS_KM) ;

		else if ( units.equals("millimeters") || units.equals("millimetres") || units.equals("mm") || 
		          units.equals("milimeters") || units.equals("milimetres") ) // Common spelling mistake.
			return DistanceUtils.dist2Degrees(dist / (1000 * 1000),
					DistanceUtils.EARTH_MEAN_RADIUS_KM) ;

		else if (units.equals("miles") || units.equals("mi"))
			return DistanceUtils.dist2Degrees(dist,
					DistanceUtils.EARTH_MEAN_RADIUS_MI);

		else if (units.equals("degrees") || units.equals("de"))
			return degrees;

		throw new IllegalArgumentException("unknow distance units: "+ units);
	}
	
	public static boolean isSupportedUnits(String units){
		return SUPPORTED_UNITS.contains(units);
	}

}
