package ru.anyline.geoservice.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/geocoding")
    public String showGeocodingForm() {
        return "geocoding";
    }
}
