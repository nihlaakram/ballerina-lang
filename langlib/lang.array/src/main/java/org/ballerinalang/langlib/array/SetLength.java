/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.langlib.array;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native implementation of lang.array:setLength((any|error)[], int).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", functionName = "setLength",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "i", type = TypeKind.INT)},
        isPublic = true
)
public class SetLength {

    public static void setLength(Strand strand, ArrayValue arr, long i) {
        throw BallerinaErrors.createError("Function 'setLength()' is not implemented");
    }
}
