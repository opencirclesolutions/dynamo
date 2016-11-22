/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.utils;

/**
 * Some utilities for working with latitude/longitude coordinates
 * 
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
public final class GeoUtils {

	/**
	 * A coordinate which is defined by a latitude and a longitude
	 * 
	 * @author bas.rutten
	 *
	 */
	public static class Coordinate {
		private Double latitude;
		private Double longitude;

		/**
		 * Constructor
		 * 
		 * @param latitude
		 *            the latitude
		 * @param longitude
		 *            hte longitude
		 */
		public Coordinate(Double latitude, Double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		public Double getLatitude() {
			return latitude;
		}

		public Double getLongitude() {
			return longitude;
		}
	}

	private GeoUtils() {
		// hidden constructor
	}

	/**
	 * Calculate the center of a set of coordinates
	 * 
	 * @param coordinates the set of coordinates
	 * @return
	 */
	public static Coordinate calculateCenter(Coordinate... coordinates) {
		if (coordinates == null) {
			return null;
		} else if (coordinates.length == 1) {
			return coordinates[0];
		}
		double x = 0, y = 0, z = 0;
		for (Coordinate c : coordinates) {
			double latitude = c.getLatitude() * Math.PI / 180;
			double longitude = c.getLongitude() * Math.PI / 180;
			x += Math.cos(latitude) * Math.cos(longitude);
			y += Math.cos(latitude) * Math.sin(longitude);
			z += Math.sin(latitude);
		}

		x = x / coordinates.length;
		y = y / coordinates.length;
		z = z / coordinates.length;

		double centralLongitude = Math.atan2(y, x);
		double centralSquareRoot = Math.sqrt(x * x + y * y);
		double centralLatitude = Math.atan2(z, centralSquareRoot);

		return new Coordinate(centralLatitude * 180 / Math.PI, centralLongitude * 180 / Math.PI);
	}

}
