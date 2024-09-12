package ru.anyline.geoservice.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.anyline.geoservice.Service.GeocodingService;

@RestController
public class GeocodingController {

    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/geocode")
    public ResponseEntity<?> geocode(@RequestParam String address) {
        if (address.matches(".*[';\"].*")) {
            return ResponseEntity.badRequest().body("Invalid input: potential SQL injection detected");
        }
        String response = geocodingService.geocode(address);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        if (lat < -90 || lat > 90) {
            return ResponseEntity.badRequest().body("Invalid input: latitude must be between -90 and 90");
        }
        String response = geocodingService.reverseGeocode(lat, lon);
        return ResponseEntity.ok(response);
    }
}

