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

	/**
	 * Divides the first argument by the second argument, then converts to a percentage
	 * 
	 * @param first
	 *            the first argument
	 * @param second
	 *            the second argument
	 * @param scale
	 *            the desired scale
	 * @return
	 */
	public static BigDecimal dividePercentage(Integer first, Integer second, int scale) {
		return dividePercentage(first == null ? BigDecimal.ZERO : new BigDecimal(first), second == null ? null
		        : new BigDecimal(second), scale);
	}
	
	/**
	 * Returns the result of multiplying a value with a certain percentage,
	 * rounded to the specified precision
	 * 
	 * @param percentage
	 *            the percentage value
	 * @param value
	 *            the non-percentage value
	 * @param scale
	 *            the scale
	 * @return
	 */
	public static BigDecimal multiplyPercentage(BigDecimal percentage, BigDecimal value, int scale) {
		return percentage.multiply(value).divide(HUNDRED, scale, RoundingMode.HALF_UP);
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

	private MathUtil() {
	}
}
