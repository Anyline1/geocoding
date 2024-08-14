package ru.anyline.geoservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyline.geoservice.Entity.GeocodingCache;

import java.util.Optional;
@Repository
public interface GeocodingCacheRepository extends JpaRepository<GeocodingCache, Long> {
    Optional<GeocodingCache> findByQuery(String query);
}
