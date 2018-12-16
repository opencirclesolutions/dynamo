package com.ocs.dynamo.ui.composite.export.impl;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.export.ExportService;
import com.ocs.dynamo.ui.composite.export.ModelBasedExportTemplate;
import com.ocs.dynamo.ui.utils.SortUtils;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;

@Service
public class ExportServiceImpl implements ExportService {

	@SuppressWarnings("unchecked")
	@Override
	public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] export(EntityModel<T> entityModel,
			SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders, FetchJoinInformation... joins) {
		BaseService<ID, T> service = (BaseService<ID, T>) ServiceLocatorFactory.getServiceLocator()
				.getServiceForEntity(entityModel.getEntityClass());
		FilterConverter<T> converter = new FilterConverter<T>(entityModel);
		Filter filter = converter.convert(predicate);

		ModelBasedExportTemplate<ID, T> template = new ModelBasedExportTemplate<ID, T>(service, entityModel,
				SortUtils.translate(sortOrders), filter, "title", false, null, joins) {

			@Override
			public int getPageSize() {
				return 500;
			}
		};

		return template.process(true);
	}

}
