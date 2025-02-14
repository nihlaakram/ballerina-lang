/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.service.grpc.sample;

import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.util.TestUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for gRPC unary service with empty input/output.
 */
@Test(groups = "grpc-test")
public class UnaryBlockingEmptyValueTestCase extends GrpcBaseTest {

    private CompileResult result;

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "clients", "01_advanced_type_client.bal");
        result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
    }

    @Test
    public void testNoInputOutputStructClient() {
        //Person p = {name:"Danesh", address:{postalCode:10300, state:"Western", country:"Sri Lanka"}};
        BValue[] responses = BRunUtil.invoke(result, "testNoInputOutputStruct", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BMap);
        final BMap<String, BValue> response = (BMap<String, BValue>) responses[0];
//        StockQuote res = {symbol: "WSO2", name: "WSO2 Inc.", last: 14, low: 15, high: 16};
//        StockQuote res1 = {symbol: "Google", name: "Google Inc.", last: 100, low: 101, high: 102};
        Assert.assertTrue(response.get("stock") instanceof BValueArray);
        final BValueArray structArray = (BValueArray) response.get("stock");
        Assert.assertEquals(structArray.size(), 2);
    }

    @Test
    public void testInputStructNoOutputClient() {
        PackageInfo packageInfo = result.getProgFile().getPackageInfo(".");
        // Stock Quote struct
        // StockQuote quote2 = {symbol: "Ballerina", name:"ballerina/io", last:1.0, low:0.5, high:2.0};
        StructureTypeInfo requestInfo = packageInfo.getStructInfo("StockQuote");
        BStructureType requestType = requestInfo.getType();
        BMap<String, BValue> requestStruct = new BMap<String, BValue>(requestType);
        requestStruct.put("symbol", new BString("Ballerina"));
        requestStruct.put("name", new BString("ballerina/io"));
        requestStruct.put("last", new BFloat(1.0));
        requestStruct.put("low", new BFloat(0.5));
        requestStruct.put("high", new BFloat(2.0));

        BValue[] responses = BRunUtil.invoke(result, "testInputStructNoOutput", new BValue[]{requestStruct});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        final BString response = (BString) responses[0];
        Assert.assertEquals(response.stringValue(), "No Response");
    }
}
