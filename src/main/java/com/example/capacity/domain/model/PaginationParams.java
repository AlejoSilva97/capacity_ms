package com.example.capacity.domain.model;

public record PaginationParams(Integer page, Integer size, String sortBy, String direction) {
}
