package com.example.serviceportal.backend.persistence;

import com.example.serviceportal.backend.model.enums.RequestPriority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RequestPriorityConverter implements AttributeConverter<RequestPriority, String> {

    @Override
    public String convertToDatabaseColumn(RequestPriority attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public RequestPriority convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RequestPriority.fromValue(dbData);
    }
}