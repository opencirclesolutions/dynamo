package com.ocs.dynamo.constants;

import java.util.Locale;

/**
 * Various constants that are used by the framework.
 * 
 * @author bas.rutten
 *
 */
public final class OCSConstants {

	/**
	 * Maximum cache size (for lazy query container)
	 */
	public static final int CACHE_SIZE = 10000;

	/**
	 * CSS style for an element that is almost full width
	 */
	public static final String CSS_ALMOST_FULL_WIDTH = "almostFullWidth";

	/**
	 * Custom style for forms that should span 50% of the screen
	 */
	public static final String CSS_CLASS_HALFSCREEN = "halfScreen";

	/**
	 * The CSS class that is given to an image component used to display an
	 * uploaded file
	 */
	public static final String CSS_CLASS_UPLOAD = "fileUpload";

	/**
	 * The CSS class that indicates a dangerous value
	 */
	public static final String CSS_DANGER = "danger";

	/**
	 * The CSS class that is assigned to numerical cells in a table
	 */
	public static final String CSS_NUMERICAL = "numerical";

	/**
	 * Currency symbol
	 */
	public static final String CURRENCY_SYMBOL = "currencySymbol";

	/**
	 * The default locale
	 */
	public static final Locale DEFAULT_LOCALE = Locale.UK;

	/**
	 * Larger page size (for lazy query container)
	 */
	public static final int EXTENDED_PAGE_SIZE = 500;

	/**
	 * The default ID field
	 */
	public static final String ID = "id";

	/**
	 * The default page size for the lazy query container.
	 */
	public static final int PAGE_SIZE = 20;

	/**
	 * The screen mode
	 */
	public static final String SCREEN_MODE = "screenMode";

	/**
	 * The name of the variable that keeps track of which tab is selected
	 */
	public static final String SELECTED_TAB = "selectedTab";

	/**
	 * System property that indicates whether to use the thousands grouping
	 * separator in edit mode
	 */
	public static final String SP_THOUSAND_GROUPING = "ocs.edit.thousands.grouping";

	public static final String SP_DECIMAL_PRECISION = "ocs.default.decimal.precision";

	
	/**
	 * The name of the variable that is used to store the user
	 */
	public static final String USER = "user";

	/**
	 * The name of the variable that is used to store the user name in the
	 * session
	 */
	public static final String USER_NAME = "userName";

	/**
	 * the UTF-8 character set
	 */
	public static final String UTF_8 = "UTF-8";

	private OCSConstants() {}
}
