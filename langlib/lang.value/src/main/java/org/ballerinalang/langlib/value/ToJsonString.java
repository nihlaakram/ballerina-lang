/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.value;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Return the string that represents `v` in JSON format.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.value",
        functionName = "toJsonString",
        args = {@Argument(name = "v", type = TypeKind.JSON)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ToJsonString {

    public static String toJsonString(Strand strand, Object value) {

        if (value == null) {
            return "null";
        }

        BType type = TypeChecker.getType(value);
        if (type.getTag() < TypeTags.JSON_TAG) {
            return String.valueOf(value);
        }

        RefValue refValue = (RefValue) value;

        return refValue.stringValue();
    }
}
