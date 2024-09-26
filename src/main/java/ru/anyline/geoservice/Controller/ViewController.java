package ru.anyline.geoservice.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // Обработка GET-запроса для отображения формы
    @GetMapping("/geocoding")
    public String showGeocodingForm() {
        return "geocoding"; // Имя HTML-файла без расширения .html
    }
}
