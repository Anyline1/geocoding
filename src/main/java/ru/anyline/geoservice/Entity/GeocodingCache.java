package ru.anyline.geoservice.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class GeocodingCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String query;

    @Column(nullable = false)
    private String response;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public GeocodingCache(String query, String response, Date createdAt) {
        this.query = query;
        this.response = response;
        this.createdAt = createdAt;
    }

}
