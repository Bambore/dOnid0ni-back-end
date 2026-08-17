package com.donidoni.auth.metier.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.crud.dto.ApiResponse;
import com.donidoni.auth.metier.domain.Pays;
import com.donidoni.auth.metier.dto.PaysCreateDto;
import com.donidoni.auth.metier.dto.PaysResponseDto;
import com.donidoni.auth.metier.dto.PaysUpdateDto;
import com.donidoni.auth.metier.service.PaysService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pays")
@Tag(name = "Pays", description = "Destinations d'approvisionnement des groupages et des sondages")
public class PaysController extends AbstractCrudController<Pays, PaysCreateDto, PaysUpdateDto, PaysResponseDto> {

    private final PaysService paysService;

    public PaysController(final PaysService service) {
        super(service);
        this.paysService = service;
    }

    @GetMapping("/actifs")
    @Operation(summary = "Lister les destinations actives (formulaire de sondage)")
    public ResponseEntity<ApiResponse<List<PaysResponseDto>>> listerActifs() {
        return ResponseEntity.ok(ApiResponse.success(paysService.listerActifs()));
    }
}
