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

package org.ballerinalang.langlib.table;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;


/**
 * Native implementation of lang.table.TableIterator:next().
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.table", functionName = "next",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "TableIterator",
                structPackage = "ballerina/lang.table"),
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Next {
    //TODO: refactor hard coded values
    public static Object next(Strand strand, ObjectValue m) {
        IteratorValue tableIterator = (IteratorValue) m.getNativeData("&iterator&");

        if (tableIterator == null) {
            tableIterator = ((TableValue) m.get("m")).getIterator();
            m.addNativeData("&iterator&", tableIterator);
        }

        if (tableIterator.hasNext()) {
            Object tableRow =  tableIterator.next();
            BFunctionType nextFuncType = m.getType().getAttachedFunctions()[0].type;
            BRecordType recordType = (BRecordType) ((BUnionType) nextFuncType.retType).getMemberTypes().get(0);
            return BallerinaValues.createRecord(new MapValueImpl<>(recordType), tableRow);
        }

        return null;
    }
}
