/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langlib.table;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function to get the current row of a table as a struct.
 *
 * @since 0.88
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.table",
        functionName = "getNext",
        args = {@Argument(name = "dt", type = TypeKind.TABLE)},
        returnType = {@ReturnType(type = TypeKind.ANY)},
        isPublic = true
)
public class GetNext extends BlockingNativeCallableUnit {

    public void execute(Context context) {
        BTable table = (BTable) context.getRefArgument(0);
        context.setReturnValues(table.getNext());
    }

    public static Object getNext(Strand strand, TableValue table) {
        return table.getNext();
    }
}
