package com.ocs.dynamo.ui.composite.form.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.composite.form.process.ProgressForm.ProgressMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinSession;

public class ProgressFormTest extends BaseMockitoTest {

	@Mock
	private UI ui;

	@Mock
	private VaadinSession session;

	@Mock
	private TestEntityService service;

	private int called = 0;

	private boolean afterWorkCalled = false;

	@BeforeEach
	public void setUp() {
		when(ui.getSession()).thenReturn(session);
	}

	@Test
	public void testCreateSimple() throws InterruptedException {
		called = 0;
		afterWorkCalled = false;
		ProgressForm<Object> pf = new ProgressForm<Object>(UI.getCurrent(), ProgressMode.SIMPLE);
		pf.setEstimateSize(Object -> 100);
		pf.setProcessConsumer((o, estimatedSize) -> {
			for (int i = 0; i < 100; i++) {
				pf.getCounter().increment();
			}
			called++;
		});

		pf.setAfterWorkComplete(exceptionOccurred -> {
			assertFalse(exceptionOccurred);
			afterWorkCalled = true;
		});

		MockUtil.injectUI(pf, ui);
		pf.build();

		assertEquals(0, pf.getCounter().getCurrent());

		assertEquals(0, called);
		pf.startWork(null);

		Thread.sleep(1000);

		assertEquals(1, called);
		assertEquals(100, pf.getCounter().getCurrent());
		assertTrue(afterWorkCalled);
	}

	@Test
	public void testCreateProgressBar() throws InterruptedException {
		when(ui.access(any())).thenAnswer(a -> {
			Command c = (Command) a.getArgument(0);
			c.execute();
			return null;
		});

		called = 0;
		afterWorkCalled = false;
		ProgressForm<Object> pf = new ProgressForm<Object>(UI.getCurrent(), ProgressMode.PROGRESSBAR);

		pf.setAfterWorkComplete(exceptionOccurred -> {
			assertFalse(exceptionOccurred);
			afterWorkCalled = true;
		});
		pf.setProcessConsumer((t, estimatedSize) -> {
			for (int i = 0; i < 100; i++) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {

				}
				pf.getCounter().increment();
			}
			called++;
		});
		pf.setEstimateSize(est -> 100);

		MockUtil.injectUI(pf, ui);
		pf.build();

		assertEquals(0, pf.getCounter().getCurrent());

		assertEquals(0, called);
		pf.startWork(null);

		Thread.sleep(5000);

		verify(ui, atLeast(1)).access(any(Command.class));

		assertEquals(1, called);
		assertEquals(100, pf.getCounter().getCurrent());
		assertTrue(afterWorkCalled);
	}

	@Test
	public void testException() throws InterruptedException {

		when(ui.access(any())).thenAnswer(a -> {
			Command c = (Command) a.getArgument(0);
			c.execute();
			return null;
		});

		called = 0;
		afterWorkCalled = false;
		ProgressForm<Object> pf = new ProgressForm<Object>(UI.getCurrent(), ProgressMode.SIMPLE);
		MockUtil.injectUI(pf, ui);
		pf.setEstimateSize(est -> 100);
		pf.setProcessConsumer((t, estimatedSize) -> {
			throw new OCSRuntimeException("Test");
		});
		pf.setAfterWorkComplete(exceptionOccurred -> {
			assertTrue(exceptionOccurred);
			afterWorkCalled = true;
		});

		pf.build();

		assertEquals(0, pf.getCounter().getCurrent());

		assertEquals(0, called);
		pf.startWork(null);

		Thread.sleep(1000);

		assertEquals(0, called);
		assertEquals(0, pf.getCounter().getCurrent());
		assertTrue(afterWorkCalled);
	}
}
