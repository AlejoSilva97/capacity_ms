CREATE TABLE IF NOT EXISTS capacities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    description VARCHAR(90)
);

CREATE TABLE IF NOT EXISTS capacity_technology (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_capacity BIGINT NOT NULL,
    id_technology BIGINT NOT NULL,
    CONSTRAINT fk_capacity FOREIGN KEY (id_capacity) REFERENCES capacities(id)
);
