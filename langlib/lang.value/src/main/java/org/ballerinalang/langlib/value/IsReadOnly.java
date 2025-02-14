/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.langlib.value;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Check the read only status of a given value.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "lang.value",
        functionName = "isReadOnly",
        args = {@Argument(name = "value", type = TypeKind.ANYDATA)},
        returnType = { @ReturnType(type = TypeKind.BOOLEAN) }
)
public class IsReadOnly {

    public static boolean isReadOnly(Strand strand, Object value) {
        return !(value instanceof RefValue) || ((RefValue) value).isFrozen();
    }
}
