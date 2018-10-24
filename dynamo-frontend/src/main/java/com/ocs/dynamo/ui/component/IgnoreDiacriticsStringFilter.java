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
//package com.ocs.dynamo.ui.component;
//
//import java.text.Normalizer;
//
//import org.apache.commons.lang.ObjectUtils;
//
//import com.vaadin.v7.data.Container.Filter;
//import com.vaadin.v7.data.Item;
//import com.vaadin.v7.data.Property;
//
///**
// * A String filter that ignores diacritical characters when comparing
// * 
// * @author bas.rutten
// *
// */
//public class IgnoreDiacriticsStringFilter implements Filter {
//
//    private static final long serialVersionUID = -8965855020406086688L;
//
//    private final Object propertyId;
//
//    private final String filterString;
//
//    private final boolean ignoreCase;
//
//    private final boolean onlyMatchPrefix;
//
//    /**
//     * Constructor
//     * 
//     * @param propertyId
//     *            the name of the property to filter on
//     * @param filterString
//     *            the filter string
//     * @param ignoreCase
//     *            whether to ignore case
//     * @param onlyMatchPrefix
//     *            whether to only match the prefix
//     */
//    public IgnoreDiacriticsStringFilter(Object propertyId, String filterString, boolean ignoreCase,
//            boolean onlyMatchPrefix) {
//        this.propertyId = propertyId;
//        this.filterString = ignoreCase ? filterString.toLowerCase() : filterString;
//        this.ignoreCase = ignoreCase;
//        this.onlyMatchPrefix = onlyMatchPrefix;
//    }
//
//    @Override
//    public boolean passesFilter(Object itemId, Item item) {
//        final Property<?> p = item.getItemProperty(propertyId);
//        if (p == null) {
//            return false;
//        }
//        Object propertyValue = p.getValue();
//        if (propertyValue == null) {
//            return false;
//        }
//
//        // replace any diacritical characters
//        String temp = ignoreCase ? propertyValue.toString().toLowerCase() : propertyValue.toString();
//        temp = Normalizer.normalize(temp, Normalizer.Form.NFD);
//        temp = temp.replaceAll("[^\\p{ASCII}]", "");
//
//        final String value = temp;
//        if (onlyMatchPrefix) {
//            if (!value.startsWith(filterString)) {
//                return false;
//            }
//        } else {
//            if (!value.contains(filterString)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public boolean appliesToProperty(Object propertyId) {
//        return this.propertyId.equals(propertyId);
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) {
//            return true;
//        }
//
//        // Only ones of the objects of the same class can be equal
//        if (!(obj instanceof IgnoreDiacriticsStringFilter)) {
//            return false;
//        }
//        final IgnoreDiacriticsStringFilter o = (IgnoreDiacriticsStringFilter) obj;
//        return ObjectUtils.equals(propertyId, o.propertyId) && ObjectUtils.equals(filterString, o.filterString)
//                && ObjectUtils.equals(ignoreCase, o.ignoreCase)
//                && ObjectUtils.equals(onlyMatchPrefix, o.onlyMatchPrefix);
//    }
//
//    @Override
//    public int hashCode() {
//        return (propertyId != null ? propertyId.hashCode() : 0) ^ (filterString != null ? filterString.hashCode() : 0);
//    }
//
//    public Object getPropertyId() {
//        return propertyId;
//    }
//
//    public String getFilterString() {
//        return filterString;
//    }
//
//    public boolean isIgnoreCase() {
//        return ignoreCase;
//    }
//
//    public boolean isOnlyMatchPrefix() {
//        return onlyMatchPrefix;
//    }
//
//}
