package com.ocs.dynamo.ui.composite.table;

import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.vaadin.data.util.BeanItemContainer;

public class ModelBasedTreeTest extends BaseIntegrationTest {

	@Inject
	private EntityModelFactory factory;

	@Test
	public void testWithAbstractTreeEntity() {
		BeanItemContainer<TreeEntity> container = new BeanItemContainer<>(TreeEntity.class);

		TreeEntity t1 = new TreeEntity(1, "top", null);
		TreeEntity t2 = new TreeEntity(2, "child1", t1);
		TreeEntity t3 = new TreeEntity(3, "child2", t1);
		TreeEntity t4 = new TreeEntity(4, "grandchild1", t2);
		TreeEntity t5 = new TreeEntity(5, "grandchild2", t3);

		List<TreeEntity> list = Lists.newArrayList(t1, t2, t3, t4, t5);
		container.addAll(list);

		ModelBasedTree<Integer, TreeEntity> tree = new ModelBasedTree<>(container, factory.getModel(TreeEntity.class));
		tree.expandItemsRecursively(t1);

		// check some item captions
		Assert.assertEquals("top", tree.getItemCaption(t1));
		Assert.assertEquals("grandchild2", tree.getItemCaption(t5));

		Assert.assertEquals(t1, tree.getParent(t2));
		Assert.assertEquals(t2, tree.getParent(t4));
		Assert.assertEquals(t3, tree.getParent(t5));
	}

	@Test
	public void testWithoutAbstractTreeEntity() {
		BeanItemContainer<FakeTreeEntity> container = new BeanItemContainer<>(FakeTreeEntity.class);

		FakeTreeEntity t1 = new FakeTreeEntity(1, "top", null);
		FakeTreeEntity t2 = new FakeTreeEntity(2, "child1", t1);
		FakeTreeEntity t3 = new FakeTreeEntity(3, "child2", t1);
		FakeTreeEntity t4 = new FakeTreeEntity(4, "grandchild1", t2);
		FakeTreeEntity t5 = new FakeTreeEntity(5, "grandchild2", t3);

		List<FakeTreeEntity> list = Lists.newArrayList(t1, t2, t3, t4, t5);
		container.addAll(list);

		ModelBasedTree<Integer, FakeTreeEntity> tree = new ModelBasedTree<Integer, FakeTreeEntity>(container,
		        factory.getModel(FakeTreeEntity.class)) {

			private static final long serialVersionUID = 650833887991210305L;

			@Override
			protected FakeTreeEntity determineParent(FakeTreeEntity child) {
				return child.getMaster();
			}
		};
		tree.expandItemsRecursively(t1);

		// check some item captions
		Assert.assertEquals("top", tree.getItemCaption(t1));
		Assert.assertEquals("grandchild2", tree.getItemCaption(t5));

		Assert.assertEquals(t1, tree.getParent(t2));
		Assert.assertEquals(t2, tree.getParent(t4));
		Assert.assertEquals(t3, tree.getParent(t5));
	}
}
