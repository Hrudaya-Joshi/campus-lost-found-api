package com.campus.lostfound.controller;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ItemControllerTest {

    private MockMvc mockMvc;
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService();
        ItemController itemController = new ItemController(itemService);
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
    }

    @Test
    @DisplayName("GET /health - Should return UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("POST /items - Should add a new item and return 201 Created")
    void testAddItem() throws Exception {
        String payload = """
                {
                    "name": "Blue Water Bottle",
                    "description": "Stainless steel flask found near library",
                    "location": "Central Library 2nd Floor",
                    "type": "FOUND"
                }
                """;

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Blue Water Bottle")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.type", is("FOUND")));
    }

    @Test
    @DisplayName("GET /items - Should return all items")
    void testGetAllItems() throws Exception {
        Item item1 = new Item(null, "Keys", "Keychain with 3 keys", "Cafeteria", ItemType.LOST, ItemStatus.OPEN);
        Item item2 = new Item(null, "Notebook", "Math lecture notes", "Auditorium", ItemType.FOUND, ItemStatus.OPEN);
        itemService.addItem(item1);
        itemService.addItem(item2);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Keys")))
                .andExpect(jsonPath("$[1].name", is("Notebook")));
    }

    @Test
    @DisplayName("GET /items/{id} - Should return existing item by ID")
    void testGetItemByIdSuccess() throws Exception {
        Item item = new Item(null, "Umbrella", "Black folding umbrella", "East Gate", ItemType.LOST, ItemStatus.OPEN);
        Item saved = itemService.addItem(item);

        mockMvc.perform(get("/items/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Umbrella")))
                .andExpect(jsonPath("$.location", is("East Gate")));
    }

    @Test
    @DisplayName("GET /items/{id} - Should return 404 Not Found for nonexistent ID")
    void testGetItemByIdNotFound() throws Exception {
        mockMvc.perform(get("/items/99999"))
                .andExpect(status().isNotFound());
    }
}
