/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq.nativeimpl.message;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQTransactionContext;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Retrieves the xml content of the RabbitMQ message.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = RabbitMQConstants.ORG_NAME,
        packageName = RabbitMQConstants.RABBITMQ,
        functionName = "getXMLContent",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = RabbitMQConstants.MESSAGE_OBJECT,
                structPackage = RabbitMQConstants.PACKAGE_RABBITMQ),
        isPublic = true
)
public class GetXMLContent {

    public static Object getXMLContent(Strand strand, ObjectValue messageObjectValue) {
        boolean isInTransaction = false;
        byte[] messageContent = (byte[]) messageObjectValue.getNativeData(RabbitMQConstants.MESSAGE_CONTENT);
        RabbitMQTransactionContext transactionContext = (RabbitMQTransactionContext) messageObjectValue.
                getNativeData(RabbitMQConstants.RABBITMQ_TRANSACTION_CONTEXT);
        if (isInTransaction && !Objects.isNull(transactionContext)) {
            transactionContext.handleTransactionBlock();
        }
        try {
            return XMLFactory.parse(new String(messageContent, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return RabbitMQUtils.returnErrorValue(RabbitMQConstants.XML_CONTENT_ERROR
                    + exception.getMessage());
        }
    }

    private GetXMLContent() {
    }
}
