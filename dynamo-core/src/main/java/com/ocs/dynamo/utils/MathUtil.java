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

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MathUtil {

	public static final BigDecimal HUNDRED = new BigDecimal(100);

	private MathUtil() {
	}

	/**
	 * Performs a null-safe get of an Integer
	 * 
	 * @param x
	 *            the integer
	 * @return
	 */
	public static int nullSafeGet(Integer x) {
		return x == null ? 0 : x;
	}

	/**
	 * Divides the first argument by the second argument, then converts the result to a percentage
	 * 
	 * @param first
	 *            the first argument
	 * @param second
	 *            the second argument
	 * @param scale
	 * @return
	 */
	public static BigDecimal dividePercentage(BigDecimal first, BigDecimal second, int scale) {
		if (second == null || BigDecimal.ZERO.subtract(second).abs().doubleValue() < 0.00001) {
			return null;
		}
		return first.multiply(HUNDRED).divide(second, scale, RoundingMode.HALF_UP);
	}

	public static BigDecimal dividePercentage(Integer first, Integer second, int scale) {
		return dividePercentage(first == null ? null : new BigDecimal(first), second == null ? null : new BigDecimal(
		        second), scale);
	}
}
