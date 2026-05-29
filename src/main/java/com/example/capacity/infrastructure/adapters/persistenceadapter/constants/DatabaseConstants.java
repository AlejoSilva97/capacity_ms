package com.example.capacity.infrastructure.adapters.persistenceadapter.constants;

public class DatabaseConstants {

    public static final String QUERY =
            "SELECT c.* FROM capacities c " +
                    "LEFT JOIN capacity_technology ct ON c.id = ct.id_capacity " +
                    "GROUP BY c.id, c.name, c.description " +
                    "ORDER BY %s %s " +
                    "LIMIT :size OFFSET :offset ";
    public static final String SORT_BY_TECHNOLOGIES = "technologies";
    public static final String SORT_BY_COUNT = "COUNT(ct.id_technology)";
    public static final String SORT_BY_NAME = "c.name";
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    public static final String SIZE = "size";
    public static final String OFFSET = "offset";
}
