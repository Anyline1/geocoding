package ru.anyline.geoservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.anyline.geoservice.Controller.GeocodingController;
import ru.anyline.geoservice.Service.GeocodingService;

import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeocodingController.class)
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeocodingService geocodingService;

    @Test
    public void shouldReturnValidResponseWhenAddressIsCountryName() throws Exception {
        String address = "Russia";
        String expectedResponse = "{\"country\":\"Russia\",\"coordinates\":[82.92,61.52]}";
        given(geocodingService.geocode(address)).willReturn(expectedResponse);

        mockMvc.perform(get("/geocode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("address", address))
                .andExpect(status().isOk())
                .andExpect(result -> result.getResponse().getContentAsString().equals(expectedResponse));
    }

    @Test
    public void shouldReturnValidResponseWhenAddressIsCityName() throws Exception {
        String cityName = "New York";
        String expectedResponse = "{\"city\":\"New York\",\"coordinates\":{\"lat\":40.7128,\"lon\":-74.0060}}";
        given(geocodingService.geocode(cityName)).willReturn(expectedResponse);

        mockMvc.perform(get("/geocode")
                        .param("address", cityName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("New York")))
                .andExpect(jsonPath("$.coordinates.lat", is(40.7128)))
                .andExpect(jsonPath("$.coordinates.lon", is(-74.0060)));
    }

    @Test
    public void shouldReturnValidResponseWhenAddressIsZipCode() throws Exception {
        String zipCode = "12345";
        String expectedResponse = "Valid response for zip code";
        given(geocodingService.geocode(zipCode)).willReturn(expectedResponse);

        mockMvc.perform(get("/geocode")
                        .param("address", zipCode))
                .andExpect(status().isOk())
                .andExpect(result -> result.getResponse().getContentAsString().equals(expectedResponse));
    }
}
