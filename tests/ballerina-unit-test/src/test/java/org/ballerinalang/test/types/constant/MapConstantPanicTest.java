/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.types.constant;

import org.ballerinalang.tool.util.BCompileUtil;
import org.ballerinalang.tool.util.BRunUtil;
import org.ballerinalang.tool.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant test cases.
 */
public class MapConstantPanicTest {

    private static CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/map-literal-constant-panic.bal");
    }

    // boolean ----------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantBooleanMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantBooleanMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantBooleanMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantBooleanMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantBooleanMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantBooleanMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantBooleanMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantBooleanMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantBooleanMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantBooleanMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantBooleanMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantBooleanMapValueInArrayWithNewKey");
    }

    // int ---------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantIntMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantIntMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantIntMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantIntMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantIntMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantIntMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantIntMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantIntMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantIntMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantIntMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantIntMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantIntMapValueInArrayWithNewKey");
    }

    // byte --------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantByteMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantByteMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantByteMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantByteMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantByteMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantByteMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantByteMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantByteMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantByteMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantByteMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantByteMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantByteMapValueInArrayWithNewKey");
    }

    // float -------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantFloatMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantFloatMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantFloatMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantFloatMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantFloatMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantFloatMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantFloatMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantFloatMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantFloatMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantFloatMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantFloatMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantFloatMapValueInArrayWithNewKey");
    }

    // decimal -----------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantDecimalMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantDecimalMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantDecimalMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantDecimalMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantDecimalMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantDecimalMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantDecimalMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantDecimalMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantDecimalMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantDecimalMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantDecimalMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantDecimalMapValueInArrayWithNewKey");
    }

    // string ------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantStringMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantStringMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantStringMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantStringMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantStringMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantStringMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantStringMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantStringMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantStringMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantStringMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantStringMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantStringMapValueInArrayWithNewKey");
    }

    // nil ---------------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantNilMapValueWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantNilMapValueWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateNestedConstantNilMapValueWithNewKey() {
        BRunUtil.invoke(compileResult, "updateNestedConstantNilMapValueWithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantNilMapWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantNilMapWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateReturnedConstantNilMap2WithNewKey() {
        BRunUtil.invoke(compileResult, "updateReturnedConstantNilMap2WithNewKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantNilMapValueInArrayWithExistingKey() {
        BRunUtil.invoke(compileResult, "updateConstantNilMapValueInArrayWithExistingKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*modification not allowed on frozen value.*")
    public void updateConstantNilMapValueInArrayWithNewKey() {
        BRunUtil.invoke(compileResult, "updateConstantNilMapValueInArrayWithNewKey");
    }
}
