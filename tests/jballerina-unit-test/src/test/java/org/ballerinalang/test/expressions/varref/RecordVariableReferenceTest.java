/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.expressions.varref;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.MessageFormat;

/**
 * TestCases for Record Variable References.
 *
 * @since 0.985.0
 */
public class RecordVariableReferenceTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/varref/record-variable-reference.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/varref/record-variable-reference-negative.bal");
    }

    @Test(description = "Test simple record variable definition")
    public void testVariableAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testVariableAssignment");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 12);
        Assert.assertEquals(returns[3].stringValue(), "Y");
    }

    @Test(description = "Test simple record variable definition")
    public void testRecVarRefInsideRecVarRefInsideRecVarRef() {
        BValue[] returns = BRunUtil.invoke(result, "testRecVarRefInsideRecVarRefInsideRecVarRef");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("mKey1")).intValue(), 1);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("mKey2")).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 12);
        Assert.assertEquals(returns[2].stringValue(), "SomeVar1");
        Assert.assertNull(returns[3]);
    }

    @Test(description = "Test simple record variable definition")
    public void testRestParam() {
        BValue[] returns = BRunUtil.invoke(result, "testRestParam");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("var3")).intValue(), 12);
        Assert.assertEquals(((BMap) returns[0]).get("var4").stringValue(), "text");
    }

    @Test(description = "Test simple record variable definition")
    public void testRecordTypeInRecordVarRef() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordTypeInRecordVarRef");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("mKey1")).intValue(), 1);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("mKey2")).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 12);
        Assert.assertEquals((((BMap) returns[2]).get("var1")).stringValue(), "SomeVar1");
        Assert.assertNull(((BMap) returns[2]).get("var2"));
    }

    @Test(description = "Test tuple var ref inside record var ref")
    public void testTupleVarRefInRecordVarRef() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefInRecordVarRef");
        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(returns[0].stringValue(), "Mark");
        Assert.assertEquals(((BInteger) ((BValueArray) returns[1]).getRefValue(0)).intValue(), 1);
        Assert.assertEquals(((BInteger) ((BValueArray) returns[1]).getRefValue(1)).intValue(), 1);
        Assert.assertEquals(((BInteger) ((BValueArray) returns[1]).getRefValue(2)).intValue(), 1990);
        Assert.assertEquals(((BByte) returns[2]).intValue(), 1);
        Assert.assertEquals(returns[3].stringValue(), "Mark");
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[6]).intValue(), 1990);
    }

    @Test(description = "Test record var ref inside tuple var ref inside record var ref")
    public void testRecordInsideTupleInsideRecord() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "A");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "B");
        Assert.assertEquals(returns[1].stringValue(), "A");
        BMap child = (BMap) ((BMap) returns[2]).get("child");
        Assert.assertEquals(child.get("name").stringValue(), "C");
        Assert.assertEquals(((BInteger) ((BValueArray) child.get("yearAndAge")).getRefValue(0)).intValue(), 1996);
        Assert.assertEquals(((BMap) ((BValueArray) child.get("yearAndAge")).getRefValue(1)).get("format").stringValue(),
                "Z");
    }

    @Test(description = "Test record var ref inside tuple var ref inside record var ref")
    public void testRecordInsideTupleInsideRecord2() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord2");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "C");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1996);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 22);
        Assert.assertEquals(returns[3].stringValue(), "Z");
    }

    @Test(description = "Test record var ref with index based and field based var refs")
    public void testFieldAndIndexBasedVarRefs() {
        BValue[] returns = BRunUtil.invoke(result, "testFieldAndIndexBasedVarRefs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "D");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2002);
    }

    @Test(description = "Test record var ref rest parameter types")
    public void testRestParameterType() {
        BValue[] returns = BRunUtil.invoke(result, "testRestParameterType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    // TODO: Uncomment below tests once record literal is supported with var ref
//
//    @Test(description = "Test simple record variable definition")
//    public void testVarAssignmentOfRecordLiteral() {
//        BValue[] returns = BRunUtil.invoke(result, "testVarAssignmentOfRecordLiteral");
//        Assert.assertEquals(returns.length, 4);
//        Assert.assertEquals(returns[0].stringValue(), "Peter");
//        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
//        Assert.assertEquals(((BInteger) returns[2]).intValue(), 12);
//        Assert.assertEquals(returns[3].stringValue(), "Y");
//    }
//
//    @Test(description = "Test simple record variable definition")
//    public void testVarAssignmentOfRecordLiteral2() {
//        BValue[] returns = BRunUtil.invoke(result, "testVarAssignmentOfRecordLiteral2");
//        Assert.assertEquals(returns.length, 3);
//        Assert.assertEquals(returns[0].stringValue(), "Peter");
//        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
//        Assert.assertEquals(((BInteger) ((BMap) returns[2]).get("age")).intValue(), 12);
//        Assert.assertEquals(((BMap) returns[2]).get("format").stringValue(), "Y");
//    }
//
//    @Test(description = "Test simple record variable definition")
//    public void testVarAssignmentOfRecordLiteral3() {
//        BValue[] returns = BRunUtil.invoke(result, "testVarAssignmentOfRecordLiteral3");
//        Assert.assertEquals(returns.length, 3);
//        Assert.assertEquals(returns[0].stringValue(), "Peter");
//        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
//        Assert.assertEquals(((BInteger) ((BMap) returns[2]).get("age")).intValue(), 12);
//        Assert.assertEquals(((BMap) returns[2]).get("format").stringValue(), "Y");
//    }
//
//    @Test(description = "Test simple record variable definition")
//    public void testVarAssignmentOfRecordLiteral4() {
//        BValue[] returns = BRunUtil.invoke(result, "testVarAssignmentOfRecordLiteral4");
//        Assert.assertEquals(returns.length, 3);
//        Assert.assertEquals(returns[0].stringValue(), "Peter");
//        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
//        Assert.assertEquals(((BInteger) ((BMap) returns[2]).get("age")).intValue(), 12);
//        Assert.assertEquals(((BMap) returns[2]).get("format").stringValue(), "Y");
//    }

    @Test
    public void testNegativeRecordVariables() {
        Assert.assertEquals(resultNegative.getErrorCount(), 21);
        final String undefinedSymbol = "undefined symbol ";
        final String expectingClosedRecord = "invalid closed record binding pattern on opened record type {0}";

        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'fName'", 43, 12);
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'married'", 43, 19);
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'theAge'", 43, 41);
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'format'", 43, 49);
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'theMap'", 43, 63);
        BAssertUtil.validateError(resultNegative, ++i,
                MessageFormat.format(expectingClosedRecord, "'Age'"), 43, 33);
        BAssertUtil.validateError(resultNegative, ++i,
                MessageFormat.format(expectingClosedRecord, "'Age'"), 62, 33);
        BAssertUtil.validateError(resultNegative, ++i,
                "not enough fields to match to closed record type 'Person'", 69, 5);
        BAssertUtil.validateError(resultNegative, ++i, "variable assignment is required", 94, 5);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'Bar', found 'string'", 95, 12);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'Bar'", 95, 27);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'record type', found 'int'", 96, 38);
        BAssertUtil.validateError(resultNegative, ++i,
                "record literal is not supported for record binding pattern", 97, 38);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'Person', found 'Age'", 106, 19);
        BAssertUtil.validateError(resultNegative, ++i,
                "multiple matching record references found for field 'name'", 108, 5);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid record binding pattern; unknown field 'unknown2' in record type 'Person'", 119, 5);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid record binding pattern; unknown field 'unknown1' in record type 'Age'", 119, 26);
        BAssertUtil.validateError(resultNegative, ++i,
                "unknown type 'Data'", 123, 6);
        BAssertUtil.validateError(resultNegative, ++i,
                "unknown type 'Data'", 128, 6);
        BAssertUtil.validateError(resultNegative, ++i,
                                  "incompatible types: expected 'map<int>', found 'map<anydata>'", 161, 16);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'map<error>', found 'map'",
                                  164, 16);
    }
}
