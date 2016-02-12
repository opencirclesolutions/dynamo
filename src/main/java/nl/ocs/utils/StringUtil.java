package nl.ocs.utils;

public final class StringUtil {

	private static final String EMAIL_PATTERN = "(.+)@(.+)";

	private StringUtil() {
		// private constructor
	}

	/**
	 * Replaces all HTML breaks ("<br/>
	 * ") by commas
	 * 
	 * @param value
	 * @return
	 */
	public static String replaceHtmlBreaks(String value) {
		if (value == null) {
			return null;
		}
		value = value.replaceAll("<br/>", ", ").trim();
		if (value.endsWith(",")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}

	/**
	 * Restricts a string value to the maximum length of a certain field
	 * 
	 * @param value
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static String restrictToMaxFieldLength(String value, Class<?> clazz, String fieldName) {
		if (value == null) {
			return null;
		} else {
			int maxLength = ClassUtils.getMaxLength(clazz, fieldName);
			if (maxLength >= 0 && value.length() > maxLength) {
				value = value.substring(0, maxLength);
			}
		}
		return value;
	}

	/**
	 * Checks if an value is a valid email address - this is actually a very
	 * simple check that only checks for the @-sign
	 * 
	 * @param value
	 *            the value to check
	 */
	public static boolean isValidEmail(String value) {
		if (value == null) {
			return true;
		}

		return value.matches(EMAIL_PATTERN);
	}

}
