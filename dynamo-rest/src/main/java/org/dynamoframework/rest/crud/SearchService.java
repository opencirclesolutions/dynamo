package org.dynamoframework.rest.crud;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.dao.SortOrders;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.AttributeType;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.QueryType;
import org.dynamoframework.exception.OCSValidationException;
import org.dynamoframework.filter.*;
import org.dynamoframework.rest.crud.search.*;
import org.dynamoframework.service.BaseSearchService;
import org.dynamoframework.service.ServiceLocator;
import org.dynamoframework.service.ServiceLocatorFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

    private final EntityCopier entityCopier;

    public <ID, T extends AbstractEntity<ID>> SearchResultsModel<T> search(SearchModel searchModel,
                                                                           EntityModel<T> entityModel) {
        BaseSearchService<ID, T> service = findService(entityModel.getEntityClass());
        if (searchModel.getPaging().getType() == QueryType.PAGING) {
            return searchPaging(searchModel, service, entityModel);
        } else {
            return searchIdBased(searchModel, service, entityModel);
        }
    }

    private <ID, T extends AbstractEntity<ID>> SearchResultsModel<T> searchPaging(SearchModel searchModel, BaseSearchService<ID, T> service, EntityModel<T> entityModel) {
        Filter and = createFilter(searchModel, entityModel);

        long count = service.count(and, true);
        if (count == 0) {
            return SearchResultsModel.<T>builder()
                    .stats(createStatsModel(searchModel, 0, false))
                    .results(Collections.emptyList())
                    .build();
        }

        int maxResults = entityModel.getMaxSearchResults();
        boolean tooManyResults = count > maxResults;
        if (tooManyResults) {
            // truncate if too many results would be returned
            if (searchModel.getPaging().getPageNumber() *
                    searchModel.getPaging().getPageSize() >= maxResults) {
                return SearchResultsModel.<T>builder()
                        .stats(createStatsModel(searchModel, maxResults, true))
                        .results(Collections.emptyList())
                        .build();
            }
        }

        SortOrders sortOrders = createSortOrders(searchModel, entityModel);
        List<T> results = service.fetch(and, searchModel.getPaging()
                        .getPageNumber(), searchModel.getPaging().getPageSize(),
                sortOrders);
        results = entityCopier.copyResults(results, entityModel);

        return SearchResultsModel.<T>builder()
                .stats(createStatsModel(searchModel, tooManyResults ? maxResults : (int) count, tooManyResults))
                .results(results)
                .build();
    }

    private <ID, T extends AbstractEntity<ID>> SearchResultsModel<T> searchIdBased(SearchModel searchModel, BaseSearchService<ID, T> service, EntityModel<T> entityModel) {
        SortOrders sortOrders = createSortOrders(searchModel, entityModel);

        Filter and = createFilter(searchModel, entityModel);
        List<ID> ids = service.findIds(and, sortOrders.getOrders().toArray(new SortOrder[0]));

        if (ids.isEmpty()) {
            return SearchResultsModel.<T>builder()
                    .stats(createStatsModel(searchModel, 0, false))
                    .results(Collections.emptyList())
                    .build();
        }

        int maxResults = entityModel.getMaxSearchResults();
        boolean tooManyResults = ids.size() > maxResults;
        if (tooManyResults) {
            ids = ids.subList(0, maxResults);
        }

        int offset = searchModel.getPaging().getPageNumber() *
                searchModel.getPaging().getPageSize();
        List<ID> subList = createSubList(ids, offset, searchModel.getPaging().getPageSize());

        List<T> results = service.fetchByIds(subList, sortOrders);
        results = entityCopier.copyResults(results, entityModel);

        return SearchResultsModel.<T>builder()
                .stats(createStatsModel(searchModel, ids.size(), tooManyResults))
                .results(results)
                .build();
    }

    public <ID, T extends AbstractEntity<ID>> And createFilter(SearchModel searchModel, EntityModel<T> model) {
        if (searchModel.getFilters() == null) {
            return null;
        }

        List<? extends Filter> filters = searchModel.getFilters().stream()
                .map(filterModel -> mapFilter(model, filterModel)).filter(Objects::nonNull).toList();
        return new And(filters.toArray(new Filter[0]));
    }

    private Object convertValue(Object value, AttributeModel am) {
        if ((am.getType().equals(LocalDate.class) || am.isSearchDateOnly()) && value instanceof String str) {
            return LocalDate.parse(str);
        }
        return value;
    }

    private <ID, T extends AbstractEntity<ID>> AbstractFilter mapFilter(EntityModel<T> model, FilterModel filterModel) {
        AttributeModel am = model.getAttributeModel(filterModel.getName());

        validateValidSearchRequest(filterModel, am);

        if (filterModel instanceof OrFilterModel ofm) {
            List<? extends Filter> mapped = ofm.getOrFilters() == null ? Collections.emptyList() : ofm.getOrFilters()
                    .stream().map(fm -> mapFilter(model, fm))
                    .toList();
            return new Or(mapped.toArray(new Filter[0]));
        } else if (filterModel instanceof  NotFilterModel nfm) {
            return new Not(mapFilter(model, nfm.getFilter()));
//        } else if (filterModel instanceof AndFilterModel afm) {
////                List<? extends Filter> mapped = afm.getFilters() == null ? Collections.emptyList() : afm.getFilters()
////                        .stream().map(fm -> mapFilter(model, fm))
////                        .toList();
////                return new And(mapped.toArray(new Filter[0]));
       } else if (filterModel instanceof EqualsFilterModel efm) {
            String prop = mapSearchProperty(am);
            Object value = convertValue(efm.getValue(), am);

            if (am.getType() == String.class) {
                return mapStringFilter(efm, am);
            } else if (am.isSearchDateOnly()) {
                // time stamp field, but search for range only
                LocalDate date = (LocalDate) value;
                if (am.getType() == Instant.class) {
                    return new Between(am.getName(), date.atStartOfDay().toInstant(ZoneOffset.UTC),
                            date.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC));
                } else if (am.getType() == LocalDateTime.class) {
                    return new Between(am.getName(), date.atStartOfDay(),
                            date.plusDays(1).atStartOfDay().minusNanos(1));
                }

            } else if (am.getAttributeType() == AttributeType.BASIC) {
                // other basic attributes like booleans or enums
                return new Compare.Equal(prop, value);
            } else {
                // complex values (expect "property.id")
                return new Compare.Equal(prop + ".id", value);
            }
        } else if (filterModel instanceof NumberRangeFilterModel rfm) {
            return mapNumberRangeFilter(rfm, am);
        } else if (filterModel instanceof DateRangeFilterModel rfm) {
            return mapDateRangeFilter(rfm, am);
        } else if (filterModel instanceof InstantRangeFilterModel rfm) {
            return mapInstantRangeFilter(rfm, am);
        } else if (filterModel instanceof LocalDateTimeRangeFilterModel rfm) {
            return mapLocalDateTimeRangeFilter(rfm, am);
        } else if (filterModel instanceof TimeRangeFilterModel tfm) {
            return mapTimeRangeFilter(tfm, am);
        } else if (filterModel instanceof NumberInFilterModel nfm) {
            if (am.getAttributeType() != AttributeType.MASTER && am.getAttributeType() !=
                    AttributeType.DETAIL) {
                throw new OCSValidationException("In filter specified for basic attribute %s"
                        .formatted(nfm.getName()));
            }
            String prop = mapSearchProperty(am);
            return new In(prop + ".id", nfm.getValues());
        }
        return null;
    }

    private AbstractFilter mapInstantRangeFilter(InstantRangeFilterModel filterModel, AttributeModel am) {
        String prop = mapSearchProperty(am);

        if (am.isSearchDateOnly()) {
            throw new OCSValidationException("Range filter found for attribute %s but search for date expected".formatted(am.getName()));
        }

        if (filterModel.getFrom() != null && filterModel.getTo() != null) {
            return new Between(prop, filterModel.getFrom(), filterModel.getTo());
        } else if (filterModel.getFrom() != null) {
            return new Compare.GreaterOrEqual(prop, filterModel.getFrom());
        } else if (filterModel.getTo() != null) {
            return new Compare.LessOrEqual(prop, filterModel.getTo());
        } else {
            throw new OCSValidationException("No bounds specified for filter %s"
                    .formatted(filterModel.getName()));
        }
    }

    private AbstractFilter mapLocalDateTimeRangeFilter(LocalDateTimeRangeFilterModel filterModel, AttributeModel am) {
        String prop = mapSearchProperty(am);
        if (am.isSearchDateOnly()) {
            throw new OCSValidationException("Range filter found for attribute %s but search for date expected".formatted(am.getName()));
        }

        if (filterModel.getFrom() != null && filterModel.getTo() != null) {
            return new Between(prop,
                    filterModel.getFrom(),
                    filterModel.getTo());
        } else if (filterModel.getFrom() != null) {
            return new Compare.GreaterOrEqual(prop, filterModel.getFrom());
        } else if (filterModel.getTo() != null) {
            return new Compare.LessOrEqual(prop, filterModel.getTo());
        } else {
            throw new OCSValidationException("No bounds specified for filter %s"
                    .formatted(filterModel.getName()));
        }
    }

    private AbstractFilter mapTimeRangeFilter(TimeRangeFilterModel filterModel, AttributeModel am) {
        String prop = mapSearchProperty(am);
        if (filterModel.getFrom() != null && filterModel.getTo() != null) {
            return new Between(prop,
                    filterModel.getFrom(), filterModel.getTo());
        } else if (filterModel.getFrom() != null) {
            return new Compare.GreaterOrEqual(prop, filterModel.getFrom());
        } else if (filterModel.getTo() != null) {
            return new Compare.LessOrEqual(prop, filterModel.getTo());
        } else {
            throw new OCSValidationException("No bounds specified for filter %s"
                    .formatted(filterModel.getName()));
        }
    }

    private Like mapStringFilter(EqualsFilterModel efm, AttributeModel am) {
        String prop = mapSearchProperty(am);
        String pattern = efm.getValue() + "%";
        if (!am.isSearchPrefixOnly()) {
            pattern = "%" + pattern;
        }

        return new Like(prop, pattern,
                am.isSearchCaseSensitive());
    }

    private AbstractFilter mapNumberRangeFilter(NumberRangeFilterModel rfm, AttributeModel am) {
        String prop = mapSearchProperty(am);
        if (am.isSearchForExactValue()) {
            throw new OCSValidationException("Range filter found for attribute %s but search for exact value expected".formatted(am.getName()));
        }

        if (rfm.getFrom() != null && rfm.getTo() != null) {
            return new Between(prop,
                    (Comparable<?>) rfm.getFrom(), (Comparable<?>) rfm.getTo());
        } else if (rfm.getFrom() != null) {
            return new Compare.GreaterOrEqual(prop, rfm.getFrom());
        } else if (rfm.getTo() != null) {
            return new Compare.LessOrEqual(prop, rfm.getTo());
        } else {
            throw new OCSValidationException("No bounds specified for filter %s"
                    .formatted(rfm.getName()));
        }
    }

    private AbstractFilter mapDateRangeFilter(DateRangeFilterModel filterModel, AttributeModel am) {
        if (am.isSearchForExactValue()) {
            throw new OCSValidationException("Range filter found for attribute %s but search for exact value expected".formatted(am.getName()));
        }

        String prop = mapSearchProperty(am);
        Comparable<?> from = (Comparable<?>) convertValue(filterModel.getFrom(), am);
        Comparable<?> to = (Comparable<?>) convertValue(filterModel.getTo(), am);

        if (filterModel.getFrom() != null && filterModel.getTo() != null) {
            return new Between(prop,
                    from, to);
        } else if (filterModel.getFrom() != null) {
            return new Compare.GreaterOrEqual(prop, from);
        } else if (filterModel.getTo() != null) {
            return new Compare.LessOrEqual(prop, to);
        } else {
            throw new OCSValidationException("No bounds specified for filter %s"
                    .formatted(filterModel.getName()));
        }
    }

    private String mapSearchProperty(AttributeModel am) {
        return am.getActualSearchPath();
    }

    /**
     * Validate that the search filter is valid (must have an attribute model, and attribute
     * must be searchable)
     *
     * @param filterModel the filter model
     * @param am          the attribute model
     */
    private void validateValidSearchRequest(FilterModel filterModel, AttributeModel am) {

        // for "or" filter, no attributes are needed
        if (filterModel instanceof OrFilterModel || filterModel instanceof NotFilterModel) {
            return;
        }

        if (am == null) {
            throw new OCSValidationException("Attribute '%s' does not exist".formatted(
                    filterModel.getName()
            ));
        }

        if (!am.isSearchable()) {
            throw new OCSValidationException("Searching on attribute '%s' is not allowed".formatted(
                    filterModel.getName()
            ));
        }
    }

    private <ID> List<ID> createSubList(List<ID> input, int offset, int pageSize) {
        List<ID> subList;

        if (offset > input.size()) {
            subList = Collections.emptyList();
        } else if (offset + pageSize > input.size()) {
            subList = input.subList(offset, input.size());
        } else {
            subList = input.subList(offset, offset + pageSize);
        }
        return subList;
    }

    public SortOrders createSortOrders(SearchModel searchModel, EntityModel<?> entityModel) {
        SortModel sortModel = searchModel.getSort();
        SortOrders orders = new SortOrders(new SortOrder(sortModel.getSortField(),
                SortOrder.Direction.valueOf(sortModel.getSortDirection().toString())));

        List<SortOrder> mappedOrders = orders.getOrders().stream().map(order -> {
            AttributeModel am = entityModel.getAttributeModel(order.getProperty());

            if (am == null || !am.isSortable()) {
                throw new OCSValidationException("Sorting on attribute %s is not supported"
                        .formatted(order.getProperty()));
            }

            return new SortOrder(am.getActualSortPath(),
                    order.getDirection());

        }).toList();
        return new SortOrders(mappedOrders.toArray(new SortOrder[0]));
    }

    private StatsModel createStatsModel(SearchModel searchModel, int count, boolean tooManyResults) {
        return StatsModel.builder()
                .pageNumber(searchModel.getPaging().getPageNumber())
                .pageSize(searchModel.getPaging().getPageSize())
                .totalResults(count)
                .tooManyResults(tooManyResults)
                .build();
    }

    @SuppressWarnings("unchecked")
    private <ID, T extends AbstractEntity<ID>> BaseSearchService<ID, T> findService(Class<?> clazz) {
        return (BaseSearchService<ID, T>) serviceLocator.getSearchServiceForEntity(clazz);
    }
}
