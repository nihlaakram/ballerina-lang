/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.encoding.nativeimpl;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Extern function ballerina.encoding:encodeBase64Url.
 *
 * @since 0.991.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "encoding",
        functionName = "encodeBase64Url", isPublic = true
)
public class EncodeBase64Url {

    public static String encodeBase64Url(Strand strand, ArrayValue input) {
        byte[] encodedValue = Base64.getUrlEncoder().encode(input.getBytes());
        return new String(encodedValue, StandardCharsets.ISO_8859_1);
    }
}
