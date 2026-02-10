package com.example.bankcards.util;


import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {

    @Mapping(target = "maskedNumber", expression = "java(card.getMaskedNumber())")

    CardResponseDTO toDto(Card card);
}