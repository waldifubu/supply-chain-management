package com.example.supplychainmanagement.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

public class CustomFilter extends SimpleBeanPropertyFilter implements PropertyFilter {

    private boolean isSerializable;


    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        if (include(writer)) {
            if (!writer.getName().equals("orderId")) {
                writer.serializeAsField(pojo, jgen, provider);
                return;
            }
            System.out.println(isSerializable);
            if (isSerializable) {
                writer.serializeAsField(pojo, jgen, provider);
            }
        } else if (!jgen.canOmitFields()) { // since 2.3
            writer.serializeAsOmittedField(pojo, jgen, provider);
        }
    }

    @Override
    protected boolean include(BeanPropertyWriter writer) {
        return true;
    }

    @Override
    protected boolean include(PropertyWriter writer) {
        return true;
    }

    public boolean isSerializable() {
        return isSerializable;
    }

    public void setSerializable(boolean serializable) {
        isSerializable = serializable;
    }
}