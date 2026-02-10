package com.example.bankcards.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AdminCardService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@WebMvcTest(AdminCardController.class)
@Import(SecurityConfig.class)
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminCardService adminService;

    @MockBean
    private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Админ: Получение всех карт системы")
    void getAllCards_ShouldReturnPagedCards() throws Exception {
        Card card = Card.builder()
                .id(10L)
                .cardNumber("4444555566667777")
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .ownerName("someuser")
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        Page<Card> page = new PageImpl<>(List.of(card), PageRequest.of(0, 10), 1);

        when(adminService.getAllCards(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 7777"))
                .andExpect(jsonPath("$.content[0].ownerName").value("someuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Админ: Создание новой карты")
    void createCard_ShouldReturnCreated() throws Exception {
        CreateCardRequest req = new CreateCardRequest();
        req.setUserId(1L);
        req.setCardNumber("1111222233334444");
        req.setInitialBalance(new BigDecimal("100.00"));

        Card createdCard = Card.builder()
                .id(1L)
                .cardNumber(req.getCardNumber())
                .balance(req.getInitialBalance())
                .status(CardStatus.ACTIVE)
                .ownerName("user1")
                .expiryDate(LocalDate.now().plusYears(5))
                .build();

        when(adminService.createCard(anyLong(), anyString(), any())).thenReturn(createdCard);

        mockMvc.perform(post("/api/v1/admin/cards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Админ: Изменение статуса карты")
    void changeStatus_ShouldReturnOk() throws Exception {
        doNothing().when(adminService).updateCardStatus(anyLong(), any(CardStatus.class));

        mockMvc.perform(patch("/api/v1/admin/cards/1/status")
                        .with(csrf())
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Доступ запрещен: USER не может зайти в Admin API")
    void adminApi_WhenUserRole_ShouldReturnForbidden() throws Exception {
        when(adminService.getAllCards(any())).thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/v1/admin/cards"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Админ: Удаление карты")
    void deleteCard_ShouldReturnNoContent() throws Exception {
        doNothing().when(adminService).deleteCard(1L);

        mockMvc.perform(delete("/api/v1/admin/cards/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}