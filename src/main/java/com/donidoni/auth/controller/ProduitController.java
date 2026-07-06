package com.donidoni.auth.controller;

import com.donidoni.auth.crud.controller.AbstractCrudController;
import com.donidoni.auth.domain.Produit;
import com.donidoni.auth.dto.ProduitCreateDto;
import com.donidoni.auth.dto.ProduitResponseDto;
import com.donidoni.auth.dto.ProduitUpdateDto;
import com.donidoni.auth.service.ProduitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produits")
@Tag(name = "Produits", description = "API de gestion des produits (Exemple CRUD Générique)")
public class ProduitController extends AbstractCrudController<Produit, ProduitCreateDto, ProduitUpdateDto, ProduitResponseDto> {

    public ProduitController(final ProduitService service) {
        super(service);
    }
}
