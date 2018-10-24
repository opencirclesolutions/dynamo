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
//package com.ocs.dynamo.ui.utils;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.mockito.Mock;
//
//import com.ocs.dynamo.constants.DynamoConstants;
//import com.ocs.dynamo.domain.model.EntityModelFactory;
//import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
//import com.ocs.dynamo.service.MessageService;
//import com.ocs.dynamo.test.BaseMockitoTest;
//import com.ocs.dynamo.test.MockUtil;
//import com.ocs.dynamo.ui.composite.table.TableUtils;
//import com.vaadin.v7.ui.Table;
//
//import junitx.util.PrivateAccessor;
//
//public class TableUtilsTest extends BaseMockitoTest {
//
//    private EntityModelFactory factory = new EntityModelFactoryImpl();
//
//    @Mock
//    private MessageService messageService;
//
//    @BeforeClass
//    public static void beforeClass() {
//        System.setProperty(DynamoConstants.SP_DEFAULT_LOCALE, "de");
//    }
//
//    @Before
//    public void setupTableUtilsTest() throws NoSuchFieldException {
//        MockUtil.mockMessageService(messageService);
//        PrivateAccessor.setField(factory, "messageService", messageService);
//    }
//
//    @Test
//    public void testDefaultInit() {
//        Table table = new Table();
//        TableUtils.defaultInitialization(table);
//
//        Assert.assertTrue(table.isImmediate());
//        Assert.assertFalse(table.isEditable());
//        Assert.assertFalse(table.isMultiSelect());
//        Assert.assertTrue(table.isColumnReorderingAllowed());
//        Assert.assertTrue(table.isColumnCollapsingAllowed());
//        Assert.assertTrue(table.isSortEnabled());
//    }
//
//}
