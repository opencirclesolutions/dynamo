package com.ocs.dynamo.importer.impl.template;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.importer.impl.BaseCsvImporter;
import com.ocs.dynamo.importer.impl.BaseTextImporter;
import com.ocs.dynamo.importer.template.TextImportTemplate;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;

public class TextImportTemplateTest extends BaseMockitoTest {

	@Mock
	private MessageService messageService;

	private BaseTextImporter importer = new BaseCsvImporter();

	@Test
	public void testBasic() {
		List<String[]> lines = new ArrayList<String[]>();
		lines.add(new String[] { "name", "number" });
		lines.add(new String[] { "1", "Kevin" });
		lines.add(new String[] { "2", "Stuart" });

		List<String> errors = new ArrayList<>();

		TextImportTemplate<Integer, TestDTO> template = new TextImportTemplate<Integer, TestDTO>(messageService, lines,
		        errors, false) {

			@Override
			protected TestDTO process(int rowNum, String[] row) {
				return importer.processRow(rowNum, row, TestDTO.class);
			}

			@Override
			protected boolean isAppropriateRow(String[] line) {
				return true;
			}

			@Override
			protected Integer getKeyFromRow(TestDTO t) {
				return null;
			}
		};
		List<TestDTO> result = template.execute();
		Assert.assertEquals(2, result.size());
		// duplicate error reported
		Assert.assertEquals(0, errors.size());
	}

	@Test
	public void testException() {
		List<String[]> lines = new ArrayList<String[]>();
		lines.add(new String[] { "name", "number" });

		// first value cannot be converted to a number
		lines.add(new String[] { "a", "Kevin" });
		lines.add(new String[] { "2", "Stuart" });

		List<String> errors = new ArrayList<>();

		TextImportTemplate<Integer, TestDTO> template = new TextImportTemplate<Integer, TestDTO>(messageService, lines,
		        errors, false) {

			@Override
			protected TestDTO process(int rowNum, String[] row) {
				return importer.processRow(rowNum, row, TestDTO.class);
			}

			@Override
			protected boolean isAppropriateRow(String[] line) {
				return true;
			}

			@Override
			protected Integer getKeyFromRow(TestDTO t) {
				return null;
			}
		};
		List<TestDTO> result = template.execute();
		Assert.assertEquals(1, result.size());
		// conversion error reported
		Assert.assertEquals(1, errors.size());
	}

	@Test
	public void testDuplicateCheck() {
		List<String[]> lines = new ArrayList<String[]>();
		lines.add(new String[] { "name", "number" });
		lines.add(new String[] { "1", "Kevin" });
		lines.add(new String[] { "1", "Stuart" });

		List<String> errors = new ArrayList<>();

		TextImportTemplate<Integer, TestDTO> template = new TextImportTemplate<Integer, TestDTO>(messageService, lines,
		        errors, true) {

			@Override
			protected TestDTO process(int rowNum, String[] row) {
				return importer.processRow(rowNum, row, TestDTO.class);
			}

			@Override
			protected boolean isAppropriateRow(String[] line) {
				return true;
			}

			@Override
			protected Integer getKeyFromRow(TestDTO t) {
				return t.getFirst();
			}
		};
		List<TestDTO> result = template.execute();
		Assert.assertEquals(1, result.size());

		// duplicate error reported
		Assert.assertEquals(1, errors.size());
	}

}
