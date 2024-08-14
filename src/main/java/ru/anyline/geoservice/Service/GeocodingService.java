package ru.anyline.geoservice.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.anyline.geoservice.Entity.GeocodingCache;
import ru.anyline.geoservice.Repository.GeocodingCacheRepository;

import java.util.Optional;

@Service
public class GeocodingService {

    @Value("${api.key}")
    private String apiKey;

    @Autowired
    private final GeocodingCacheRepository cacheRepository;


    public GeocodingService(GeocodingCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    @Cacheable("geocodingCache")
    public String geocode(String address) {
        String url = "https://api.opencagedata.com/geocode/v1/json?q=" + address + "&key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    @Cacheable("geocodingCache")
    public String reverseGeocode(double lat, double lon) {
        String url = "https://api.opencagedata.com/geocode/v1/json?q=" + lat + "," + lon + "&key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    private void saveToCache(String query, String response) {
        GeocodingCache cache = new GeocodingCache();
        cache.setQuery(query);
        cache.setResponse(response);
        cache.setTimestamp(System.currentTimeMillis());
        cacheRepository.save(cache);
    }


}

