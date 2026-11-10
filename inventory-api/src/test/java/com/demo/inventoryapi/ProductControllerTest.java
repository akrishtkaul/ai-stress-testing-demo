package com.demo.inventoryapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchReturnsArray() throws Exception {
        mockMvc.perform(get("/products/search?q=Red"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchMissingParamReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/products/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductByIdReturns200() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getProductByMissingIdReturns404() throws Exception {
        mockMvc.perform(get("/products/9999999"))
                .andExpect(status().isNotFound());
    }
}
