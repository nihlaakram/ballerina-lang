/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_FORM_DATA;

/**
 * Set the entity body with body parts.
 *
 * @since 0.964.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "mime",
        functionName = "setBodyParts",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        args = {@Argument(name = "bodyParts", type = TypeKind.ARRAY), @Argument(name = "contentType",
                type = TypeKind.STRING)},
        isPublic = true
)
public class SetBodyParts {

    public static void setBodyParts(Strand strand, ObjectValue entityObj, ArrayValue bodyParts, String contentType) {
        entityObj.addNativeData(BODY_PARTS, bodyParts);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType : MULTIPART_FORM_DATA);
    }
}
