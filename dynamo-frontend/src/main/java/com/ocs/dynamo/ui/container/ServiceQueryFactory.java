///*
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// */
//package com.ocs.dynamo.ui.container;
//
//import java.io.Serializable;
//
//import org.vaadin.addons.lazyquerycontainer.Query;
//import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
//import org.vaadin.addons.lazyquerycontainer.QueryFactory;
//
//import com.ocs.dynamo.domain.AbstractEntity;
//
///**
// * A factory for creating ServiceQuery objects
// * 
// * @author patrick.deenen
// */
//public class ServiceQueryFactory<ID extends Serializable, T extends AbstractEntity<ID>> implements QueryFactory {
//
//    /**
//     * Constructs a new query based on its definition
//     */
//    @SuppressWarnings("unchecked")
//    @Override
//    public Query constructQuery(QueryDefinition queryDefinition) {
//        ServiceQueryDefinition<ID, T> def = (ServiceQueryDefinition<ID, T>) queryDefinition;
//        switch (def.getQueryType()) {
//        case PAGING:
//            return new PagingServiceQuery<>((ServiceQueryDefinition<ID, T>) queryDefinition, null);
//        case ID_BASED:
//            return new IdBasedServiceQuery<>((ServiceQueryDefinition<ID, T>) queryDefinition, null);
//        default:
//            return null;
//        }
//    }
//
//}
