package ru.anyline.geoservice.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.anyline.geoservice.Entity.GeocodingCache;
import ru.anyline.geoservice.Repository.GeocodingCacheRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class GeocodingService {

    @Value("${api.key}")
    private String apiKey;
    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeocodingCacheRepository cacheRepository;

    @Autowired
    public GeocodingService(RedisTemplate<String, String> redisTemplate,
                            GeocodingCacheRepository cacheRepository) {
        this.redisTemplate = redisTemplate;
        this.cacheRepository = cacheRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String geocode(String address) {

        String query = "geocode:" + address;
        Optional<GeocodingCache> cachedResult = cacheRepository.findByQuery(query);
        if (isAddressCached(address)) {
            return redisTemplate.opsForValue().get(address);
        } else if (cachedResult.isPresent()) {
            return cachedResult.get().getResponse();
        }

        String url = "https://api.opencagedata.com/geocode/v1/json?q=" + address + "&key=" + apiKey;
        String response = restTemplate.getForObject(url, String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode location = root.path("results").get(0).path("geometry");
            double latitude = location.path("lat").asDouble();
            double longitude = location.path("lng").asDouble();
            String result = String.format("{\"latitude\": %f, \"longitude\": %f, \"address\": \"%s\"}", latitude, longitude, address);
            cacheRepository.save(new GeocodingCache(query, result, new Date()));
            redisTemplate.opsForValue().set(address, result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Unable to geocode the address\"}";
        }
    }

    public boolean isAddressCached(String address){
        Boolean hasKey = redisTemplate.hasKey(address);
        return Boolean.TRUE.equals(hasKey);
    }

    public String reverseGeocode(double lat, double lon) {

        String query = "reverse-geocode:" + lat + "," + lon;

        Optional<GeocodingCache> cachedResult = cacheRepository.findByQuery(query);
        if (cachedResult.isPresent()) {
            return cachedResult.get().getResponse();
        }

        String url = "https://api.opencagedata.com/geocode/v1/json?q=" + lat + "," + lon + "&key=" + apiKey;
        String response = restTemplate.getForObject(url, String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode firstResult = root.path("results").get(0);
            String formattedAddress = firstResult.path("formatted").asText();
            String result = String.format("{\"latitude\": %f, \"longitude\": %f, \"address\": \"%s\"}", lat, lon, formattedAddress);
            cacheRepository.save(new GeocodingCache(query, result, new Date()));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Unable to reverse geocode the coordinates\"}";
        }
    }


}

