package com.demo.inventoryapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrderReturns201() throws Exception {
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("u1"))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void createOrderMissingUserIdReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemReturns200WithItems() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u2\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode order = objectMapper.readTree(result.getResponse().getContentAsString());
        long orderId = order.get("id").asLong();

        mockMvc.perform(post("/orders/" + orderId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void addItemBadQuantityReturnsBadRequest() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u3\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode order = objectMapper.readTree(result.getResponse().getContentAsString());
        long orderId = order.get("id").asLong();

        mockMvc.perform(post("/orders/" + orderId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemMissingOrderReturns404() throws Exception {
        mockMvc.perform(post("/orders/9999999/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderReturnsOrderWithItems() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u4\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode order = objectMapper.readTree(result.getResponse().getContentAsString());
        long orderId = order.get("id").asLong();

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getMissingOrderReturns404() throws Exception {
        mockMvc.perform(get("/orders/9999999"))
                .andExpect(status().isNotFound());
    }
}
