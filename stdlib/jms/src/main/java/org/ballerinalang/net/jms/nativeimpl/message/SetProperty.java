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
 *
 */

package org.ballerinalang.net.jms.nativeimpl.message;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Retrieves the JMS transport property from the Message for the given name.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JAVA_JMS,
        functionName = "setProperty",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.MESSAGE_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class SetProperty {

    public static Object setProperty(Strand strand, ObjectValue msgObj, String key, Object value) {
        Message message = JmsUtils.getJMSMessage(msgObj);
        try {
            if (value instanceof Long) {
                message.setLongProperty(key, (Long) value);
            } else if (value instanceof Double) {
                message.setDoubleProperty(key, (Double) value);
            } else if (value instanceof Byte) {
                message.setByteProperty(key, (Byte) value);
            } else if (value instanceof Integer) {
                message.setIntProperty(key, (Integer) value);
            } else if (value instanceof Boolean) {
                message.setBooleanProperty(key, (Boolean) value);
            } else {
                message.setStringProperty(key, value.toString());
            }
        } catch (JMSException ex) {
            return BallerinaAdapter.getError("Error setting the property", ex);
        }
        return null;
    }

    private SetProperty() {
    }
}
