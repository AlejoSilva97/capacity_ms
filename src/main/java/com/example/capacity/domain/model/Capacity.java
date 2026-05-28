package com.example.capacity.domain.model;

import com.example.capacity.domain.constants.Constants;
import com.example.capacity.domain.exceptions.InvalidFieldException;

import java.util.List;

public record Capacity(Long id, String name, String description, List<Technology> technologies) {
    public Capacity {
        if (technologies  == null) {
            throw new InvalidFieldException(Constants.CAPACITY_TECHNOLOGIES_REQUIRED);
        }
        if (technologies.size() < 3 || technologies.size() > 20) {
            throw new InvalidFieldException(Constants.TECHNOLOGIES_SIZE_VALIDATION_MESSAGE);
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidFieldException(Constants.CAPACITY_NAME_REQUIRED);
        }
        if (name.length() > 50) {
            throw new InvalidFieldException(Constants.CAPACITY_NAME_TOO_LONG);
        }
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidFieldException(Constants.CAPACITY_DESCRIPTION_REQUIRED);
        }
        if (description.length() > 90) {
            throw new InvalidFieldException(Constants.CAPACITY_DESCRIPTION_TOO_LONG);
        }
    }
}
