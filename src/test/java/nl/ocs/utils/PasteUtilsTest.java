package nl.ocs.utils;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class PasteUtilsTest {

	@Test
	public void testSplit() {
		Assert.assertNull(PasteUtils.split(null));

		String[] result = PasteUtils.split("3");
		Assert.assertEquals("3", result[0]);

		result = PasteUtils.split("3 4 5");
		Assert.assertEquals("3", result[0]);
		Assert.assertEquals("4", result[1]);
		Assert.assertEquals("5", result[2]);

		result = PasteUtils.split("3\t4\n5");
		Assert.assertEquals("3", result[0]);
		Assert.assertEquals("4", result[1]);
		Assert.assertEquals("5", result[2]);

		Assert.assertTrue(StringUtils.isWhitespace("\t"));
	}

	@Test
	public void testSplitComplex() {
		String[] result = PasteUtils.split("\t\t3");
		Assert.assertEquals("", result[0]);
		Assert.assertEquals("", result[1]);
		Assert.assertEquals("3", result[2]);

		result = PasteUtils.split("\t\t\t3\t\t\t4");
		Assert.assertEquals("", result[0]);
		Assert.assertEquals("", result[1]);
		Assert.assertEquals("", result[2]);
		Assert.assertEquals("3", result[3]);
		Assert.assertEquals("", result[4]);
		Assert.assertEquals("", result[5]);
		Assert.assertEquals("4", result[6]);
		
		result = PasteUtils.split("   3   4");
		Assert.assertEquals("", result[0]);
		Assert.assertEquals("", result[1]);
		Assert.assertEquals("", result[2]);
		Assert.assertEquals("3", result[3]);
		Assert.assertEquals("", result[4]);
		Assert.assertEquals("", result[5]);
		Assert.assertEquals("4", result[6]);
	}

	@Test
	public void testToInt() {
		Assert.assertEquals(new Integer(1234), PasteUtils.toInt("1234"));
		Assert.assertEquals(new Integer(1234), PasteUtils.toInt("1,234"));
		Assert.assertEquals(new Integer(1234), PasteUtils.toInt("1.234"));
	}

	@Test
	public void testTranslateSeparators() {
		Assert.assertEquals("2,3", PasteUtils.translateSeparators("2,3", new Locale("nl")));
		Assert.assertEquals("2,3", PasteUtils.translateSeparators("2.3", new Locale("nl")));

		Assert.assertEquals("2.3", PasteUtils.translateSeparators("2,3", new Locale("us")));
		Assert.assertEquals("2.3", PasteUtils.translateSeparators("2.3", new Locale("us")));
	}
}
