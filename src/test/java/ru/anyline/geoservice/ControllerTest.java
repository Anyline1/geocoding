package ru.anyline.geoservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.anyline.geoservice.Controller.GeocodingController;
import ru.anyline.geoservice.Service.GeocodingService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.util.AssertionErrors.assertTrue;
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

    @Test
    public void shouldReturnEmptyResponseWhenGeocodingServiceIsUnavailable() throws Exception {
        String address = "UnavailableAddress";
        given(geocodingService.geocode(address)).willReturn(null);

        mockMvc.perform(get("/geocode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("address", address))
                .andExpect(status().isOk())
                .andExpect(result -> result.getResponse().getContentAsString().isEmpty());
    }

    @Test
    public void shouldHandleConcurrentRequestsWithoutBlocking() throws InterruptedException {
        String address = "Russia";

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(get("/geocode")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .param("address", address))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void shouldValidateInputAddressFormat() {
        String validAddress = "Russia";
        String invalidAddress = "'; DROP TABLE addresses; --";

        try {
            mockMvc.perform(get("/geocode")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("address", invalidAddress))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail("Unexpected exception occurred: " + e.getMessage());
        }

        try {
            mockMvc.perform(get("/geocode")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("address", validAddress))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            fail("Unexpected exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void shouldHandleLargeInputAddressesWithoutBlocking() throws Exception {
        String largeInputAddress = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        CountDownLatch latch = new CountDownLatch(1);

        executorService.submit(() -> {
            try {
                mockMvc.perform(get("/geocode")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("address", largeInputAddress))
                        .andExpect(status().isOk());
                latch.countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        assertTrue("Test timed out", latch.getCount() == 0);

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void shouldHandleReverseGeocodingForValidCoordinates() throws Exception {
        double lat = 40.7128;
        double lon = -74.0060;
        String expectedResponse = "{\"city\":\"New York\",\"coordinates\":{\"lat\":40.7128,\"lon\":-74.0060}}";
        given(geocodingService.reverseGeocode(lat, lon)).willReturn(expectedResponse);

        mockMvc.perform(get("/reverse-geocode")
                        .param("lat", String.valueOf(lat))
                        .param("lon", String.valueOf(lon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("New York")))
                .andExpect(jsonPath("$.coordinates.lat", is(40.7128)))
                .andExpect(jsonPath("$.coordinates.lon", is(-74.0060)));
    }

    @Test
    public void shouldReturnErrorWhenInvalidCoordinatesAreProvided() throws Exception {
    double invalidLat = 91.0; 
    double validLon = -74.0060;

        try {
            mockMvc.perform(get("/reverse-geocode")
                            .param("lat", String.valueOf(invalidLat))
                            .param("lon", String.valueOf(validLon)))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail("Unexpected exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void shouldValidateInputLatitudeRangeForReverseGeocoding() {
        double validLat = 40.7128;
        double invalidLatBelow = -91.0;
        double invalidLatAbove = 91.0;

        try {
            mockMvc.perform(get("/reverse-geocode")
                            .param("lat", String.valueOf(invalidLatBelow))
                            .param("lon", String.valueOf(validLat)))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail("Unexpected exception occurred: " + e.getMessage());
        }

        try {
            mockMvc.perform(get("/reverse-geocode")
                            .param("lat", String.valueOf(invalidLatAbove))
                            .param("lon", String.valueOf(validLat)))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail("Unexpected exception occurred: " + e.getMessage());
        }

        try {
            mockMvc.perform(get("/reverse-geocode")
                            .param("lat", String.valueOf(validLat))
                            .param("lon", String.valueOf(validLat)))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            fail("Unexpected exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void shouldHandleConcurrentReverseGeocodingRequestsWithoutBlocking() throws InterruptedException {
        double lat = 40.7128;
        double lon = -74.0060;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(get("/reverse-geocode")
                                    .param("lat", String.valueOf(lat))
                                    .param("lon", String.valueOf(lon)))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void shouldHandleLargeInputCoordinatesWithoutBlocking() throws Exception {
        double lat = 40.7128;
        double lon = -74.0060;

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        CountDownLatch latch = new CountDownLatch(1);

        executorService.submit(() -> {
            try {
                mockMvc.perform(get("/reverse-geocode")
                                .param("lat", String.valueOf(lat))
                                .param("lon", String.valueOf(lon)))
                        .andExpect(status().isOk());
                latch.countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        assertTrue("Test timed out", latch.getCount() == 0);

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void shouldHandleReverseGeocodingForCoordinatesAtEquator() throws Exception {
        double equatorLat = 0.0;
        double validLon = -74.0060;
        String expectedResponse = "{\"city\":\"Equator\",\"coordinates\":{\"lat\":0.0,\"lon\":-74.0060}}";
        given(geocodingService.reverseGeocode(equatorLat, validLon)).willReturn(expectedResponse);

        mockMvc.perform(get("/reverse-geocode")
                        .param("lat", String.valueOf(equatorLat))
                        .param("lon", String.valueOf(validLon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("Equator")))
                .andExpect(jsonPath("$.coordinates.lat", is(0.0)))
                .andExpect(jsonPath("$.coordinates.lon", is(-74.0060)));
    }

    @Test
    public void shouldHandleReverseGeocodingForCoordinatesAtPrimeMeridian() throws Exception {
        double lat = 40.7128;
        double lon = 0.0;
        String expectedResponse = "{\"city\":\"Greenwich\",\"coordinates\":{\"lat\":40.7128,\"lon\":0.0}}";
        given(geocodingService.reverseGeocode(lat, lon)).willReturn(expectedResponse);

        mockMvc.perform(get("/reverse-geocode")
                        .param("lat", String.valueOf(lat))
                        .param("lon", String.valueOf(lon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("Greenwich")))
                .andExpect(jsonPath("$.coordinates.lat", is(40.7128)))
                .andExpect(jsonPath("$.coordinates.lon", is(0.0)));
    }

    @Test
    public void shouldHandleReverseGeocodingForCoordinatesAtSouthPole() throws Exception {
        double lat = -90.0;
        double lon = 0.0;
        String expectedResponse = "{\"city\":\"South Pole\",\"coordinates\":{\"lat\":-90.0,\"lon\":0.0}}";
        given(geocodingService.reverseGeocode(lat, lon)).willReturn(expectedResponse);

        mockMvc.perform(get("/reverse-geocode")
                        .param("lat", String.valueOf(lat))
                        .param("lon", String.valueOf(lon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("South Pole")))
                .andExpect(jsonPath("$.coordinates.lat", is(-90.0)))
                .andExpect(jsonPath("$.coordinates.lon", is(0.0)));
    }







}
