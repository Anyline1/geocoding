package ru.anyline.geoservice.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ViewController.class)
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

}
