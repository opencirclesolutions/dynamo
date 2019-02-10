package com.ocs.dynamo.ui.composite.export.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.composite.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.ui.composite.export.ExportDelegate;
import com.ocs.dynamo.ui.composite.export.ExportDialog;
import com.ocs.dynamo.ui.composite.export.ExportService;
import com.ocs.dynamo.ui.composite.export.FixedExportDialog;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.UI;

/**
 * Implementation of the export delegate
 * 
 * @author Bas Rutten
 *
 */
@Service
public class ExportDelegateImpl implements ExportDelegate {

	@Autowired
	private ExportService exportService;

	private Map<EntityModel<?>, CustomXlsStyleGenerator<?, ?>> customStyleMap = new HashMap<>();

	@Override
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, T extends AbstractEntity<ID>> void export(UI ui, EntityModel<T> entityModel,
			ExportMode mode, SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders,
			FetchJoinInformation... joins) {
		ExportDialog<ID, T> dialog = new ExportDialog<ID, T>(exportService, entityModel, mode, predicate, sortOrders,
				(CustomXlsStyleGenerator<ID, T>) customStyleMap.get(entityModel), joins);
		dialog.build();
		ui.addWindow(dialog);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, T extends AbstractEntity<ID>> void exportFixed(UI ui, EntityModel<T> entityModel,
			ExportMode mode, Collection<T> items) {
		FixedExportDialog<ID, T> dialog = new FixedExportDialog<ID, T>(exportService, entityModel, mode,
				(CustomXlsStyleGenerator<ID, T>) customStyleMap.get(entityModel), () -> Lists.newArrayList(items));
		dialog.build();
		ui.addWindow(dialog);
	}

	/**
	 * Adds a mapping between an entity model and a custom style generator
	 * 
	 * @param entityModel
	 * @param generator
	 */
	public void addCustomStyleGenerator(EntityModel<?> entityModel, CustomXlsStyleGenerator<?, ?> generator) {
		customStyleMap.put(entityModel, generator);
	}
}
