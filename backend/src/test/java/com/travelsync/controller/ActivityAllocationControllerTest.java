package com.travelsync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsync.dto.ActivityAllocationRequest;
import com.travelsync.service.ActivityAllocationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActivityAllocationControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ActivityAllocationController(new ActivityAllocationService()))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void exposesMaximumFlowActivityAllocationAsApiResponse() throws Exception {
        ActivityAllocationRequest request = new ActivityAllocationRequest(
                List.of(new ActivityAllocationRequest.Traveler("t1", "Asha", List.of("nature"))),
                List.of(new ActivityAllocationRequest.Activity("a1", "Nature walk", List.of("nature"), 1))
        );

        mockMvc.perform(post("/api/activities/allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedTravelers").value(1))
                .andExpect(jsonPath("$.data.assignments[0].travelerName").value("Asha"))
                .andExpect(jsonPath("$.metadata.algorithm").value("Edmonds–Karp maximum flow"));
    }
}
