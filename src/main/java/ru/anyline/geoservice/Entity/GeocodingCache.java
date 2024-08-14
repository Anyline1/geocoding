package ru.anyline.geoservice.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class GeocodingCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String query;

    @Column(nullable = false)
    private String response;

    @Column(nullable = false)
    private long timestamp;

}
