package org.ballerinalang.openapi.utils;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.openapi.typemodel.OpenApiPropertyType;

/**
 * This class is to parse OpenApi schema types.
 */
public class SchemaParser {

    public void parseIntegerSchema(Object integer, OpenApiPropertyType openApiPropertyType) {
        openApiPropertyType.setPropertyType("int");
    }

    public void parseStringSchema(Object string,  OpenApiPropertyType openApiPropertyType) {
        openApiPropertyType.setPropertyType("string");
    }

    public void parseBooleanSchema(Object string,  OpenApiPropertyType openApiPropertyType) {

    }

    public void parseObjectSchema(Object string,  OpenApiPropertyType openApiPropertyType) {
        //TODO Implement object scehema
    }

    public void parseArraySchema(Object array, OpenApiPropertyType openApiPropertyType) {
        ArraySchema arrayObj = (ArraySchema) array;
        if (arrayObj.getItems() != null) {
            final Schema<?> items = arrayObj.getItems();
            openApiPropertyType.setIsArray(true);
            if (items.getType() != null) {
                openApiPropertyType.setPropertyType(items.getType());
            } else if (items.get$ref() != null) {
                final String[] referenceType = items.get$ref().split("/");
                openApiPropertyType.setPropertyType(referenceType[referenceType.length - 1]);
            }
        }
    }

    public void parseNumberSchema(Object number, OpenApiPropertyType openApiPropertyType) {
        openApiPropertyType.setPropertyType("int");
    }

    public void parseSchema(Object schema, OpenApiPropertyType openApiPropertyType) {
        Schema schemaObj = (Schema) schema;
        if (schemaObj.get$ref() != null && schemaObj.get$ref().startsWith("#")) {
            String[] ref = schemaObj.get$ref().split("/");
            openApiPropertyType.setPropertyType(StringUtils.capitalize(ref[ref.length - 1]));
        }
    }
}
