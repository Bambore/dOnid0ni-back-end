package com.donidoni.auth.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.domain.Produit;
import org.springframework.stereotype.Repository;

@Repository
public interface ProduitRepository extends BaseRepository<Produit> {
}
