package com.ocs.dynamo.ui.composite.form.process;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.composite.form.process.ProgressForm;
import com.ocs.dynamo.ui.composite.form.process.ProgressForm.ProgressMode;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;

public class ProgressFormTest extends BaseMockitoTest {

	@Mock
	private UI ui;

	@Mock
	private VaadinSession session;

	@Mock
	private TestEntityService service;

	private int called = 0;

	private boolean afterWorkCalled = false;

	@Override
	public void setUp() {
		super.setUp();
		Mockito.when(ui.getSession()).thenReturn(session);
	}

	@Test
	public void testCreateSimple() throws InterruptedException {
		called = 0;
		afterWorkCalled = false;
		ProgressForm<Object> pf = new ProgressForm<Object>(UI.getCurrent(), ProgressMode.SIMPLE) {

			private static final long serialVersionUID = -3009623960109461650L;

			@Override
			protected void process(Object t, int estimatedSize) {
				for (int i = 0; i < 100; i++) {
					getCounter().increment();
				}
				called++;
			}

			@Override
			protected int estimateSize(Object t) {
				return 100;
			}

			@Override
			protected void doBuildLayout(Layout main) {

			}

			@Override
			protected void afterWorkComplete(boolean exceptionOccurred) {
				Assert.assertFalse(exceptionOccurred);
				afterWorkCalled = true;
			}
		};
		MockUtil.injectUI(pf, ui);
		pf.build();

		Assert.assertEquals(0, pf.getCounter().getCurrent());

		Assert.assertEquals(0, called);
		pf.startWork(null);

		Thread.sleep(1000);

		Assert.assertEquals(1, called);
		Assert.assertEquals(100, pf.getCounter().getCurrent());
		Assert.assertTrue(afterWorkCalled);
	}

	@Test
	public void testCreateProgressBar() throws InterruptedException {
		called = 0;
		afterWorkCalled = false;
		ProgressForm<Object> pf = new ProgressForm<Object>(UI.getCurrent(), ProgressMode.PROGRESSBAR) {

			private static final long serialVersionUID = -3009623960109461650L;

			@Override
			protected void process(Object t, int estimatedSize) {
				for (int i = 0; i < 100; i++) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {

					}
					getCounter().increment();
				}
				called++;
			}

			@Override
			protected int estimateSize(Object t) {
				return 100;
			}

			@Override
			protected void doBuildLayout(Layout main) {

			}

			@Override
			protected void afterWorkComplete(boolean exceptionOccurred) {
				Assert.assertFalse(exceptionOccurred);
				afterWorkCalled = true;
			}
		};
		MockUtil.injectUI(pf, ui);
		pf.build();

		Assert.assertEquals(0, pf.getCounter().getCurrent());

		Assert.assertEquals(0, called);
		pf.startWork(null);

		Thread.sleep(5000);

		Mockito.verify(ui, Mockito.atLeast(1)).access(Mockito.any(Runnable.class));

		Assert.assertEquals(1, called);
		Assert.assertEquals(100, pf.getCounter().getCurrent());
		Assert.assertTrue(afterWorkCalled);
	}

	@Test
	public void testException() throws InterruptedException {
		called = 0;
		afterWorkCalled = false;
		ProgressForm<Object> pf = new ProgressForm<Object>(UI.getCurrent(), ProgressMode.SIMPLE) {

			private static final long serialVersionUID = -3009623960109461650L;

			@Override
			protected void process(Object t, int estimatedSize) {
				throw new OCSRuntimeException("Test");
			}

			@Override
			protected int estimateSize(Object t) {
				return 100;
			}

			@Override
			protected void doBuildLayout(Layout main) {

			}

			@Override
			protected void afterWorkComplete(boolean exceptionOccurred) {
				Assert.assertTrue(exceptionOccurred);
				afterWorkCalled = true;
			}
		};
		MockUtil.injectUI(pf, ui);
		pf.build();

		Assert.assertEquals(0, pf.getCounter().getCurrent());

		Assert.assertEquals(0, called);
		pf.startWork(null);

		Thread.sleep(1000);

		Assert.assertEquals(0, called);
		Assert.assertEquals(0, pf.getCounter().getCurrent());
		Assert.assertTrue(afterWorkCalled);
	}
}
