package com.example.serviceportal.backend.persistence;

import com.example.serviceportal.backend.model.enums.RequestStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RequestStatusConverter implements AttributeConverter<RequestStatus, String> {

    @Override
    public String convertToDatabaseColumn(RequestStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public RequestStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RequestStatus.fromValue(dbData);
    }
}