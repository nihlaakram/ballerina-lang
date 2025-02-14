/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langlib.string;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

/**
 * Extern function ballerina.model.arrays:substring(string, int, int).
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.string",
        functionName = "substring",
        args = {@Argument(name = "mainString", type = TypeKind.STRING),
                @Argument(name = "startIndex", type = TypeKind.INT),
                @Argument(name = "endIndex", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class Substring extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String initialString = context.getStringArgument(0);

        long fromLong = context.getIntArgument(0);
        long toLong = context.getIntArgument(1);

        if (toLong != (int) toLong) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                                                           RuntimeErrors.INDEX_NUMBER_TOO_LARGE, toLong);
        }
        if (fromLong != (int) fromLong) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                                                           RuntimeErrors.INDEX_NUMBER_TOO_LARGE, fromLong);
        }

        int from = (int) fromLong;
        int to = (int) toLong;

        if (from < 0 || to > initialString.length()) {
            throw new BallerinaException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                                         "String index out of range. Actual:" + initialString.length() + " requested: "
                                                 + from + " to " + to);
        }
        BString subString = new BString(initialString.substring(from, to));
        context.setReturnValues(subString);
    }

    public static String substring(Strand strand, String value, long startIndex, long endIndex) {
        StringUtils.checkForNull(value);
        if (startIndex != (int) startIndex) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, startIndex);
        }
        if (endIndex != (int) endIndex) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, endIndex);
        }

        if (startIndex < 0 || endIndex > value.length()) {
            throw new BallerinaException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                    "String index out of range. Actual:" + value.length() + " requested: " + startIndex + " to " +
                            endIndex);
        }
        return value.substring((int) startIndex, (int) endIndex);
    }
}
