package com.example.bankcards.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@WebMvcTest(UserCardController.class)
class UserCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Получение списка своих карт с маскированием и пагинацией")
    void getMyCards_ShouldReturnPagedMaskedCards() throws Exception {
        Card mockCard = Card.builder()
                .id(1L)
                .cardNumber("1234567812345678")
                .balance(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE)
                .ownerName("testuser")
                .expiryDate(LocalDate.now().plusYears(2))
                .build();

        when(cardService.getMyCards(eq("testuser"), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockCard), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.content[0].balance").value(1000.00))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Успешный перевод между своими картами")
    void transfer_ShouldReturnSuccessMessage() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("500.00"));

        doNothing().when(cardService).transferBetweenOwnCards(anyString(), anyLong(), anyLong(), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/user/cards/transfer")
                        .with(csrf()) // Важно для Spring Security
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Перевод успешно выполнен"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Блокировка карты пользователем")
    void lockMyCard_ShouldReturnOk() throws Exception {
        doNothing().when(cardService).lockCard(anyString(), anyLong());

        mockMvc.perform(patch("/api/v1/user/cards/1/block")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Доступ запрещен для неавторизованного пользователя")
    void getMyCards_Unauthorized_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/user/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Валидация: ошибка при отрицательной сумме перевода")
    void transfer_InvalidAmount_ShouldReturnBadRequest() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/v1/user/cards/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}