package com.ecommerce.microcommerce.web.controller;
import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.exceptions.ProduitIntrouvableException;
import com.ecommerce.microcommerce.model.Product;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.*;

@Api("Api pour les opérations de CRUD sur les produits.")
@RestController
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductDao productDao;

    @Autowired
    private HttpServletRequest requestContext;


    @GetMapping(value="/Produits")
    public MappingJacksonValue listeProduits() {
        logger.info("Début d'appel au service Produit de la requête: "+requestContext.getHeader("req-id"));
        List<Product> products = productDao.findAll();

        SimpleBeanPropertyFilter myFilter = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider filtersList = new SimpleFilterProvider().addFilter("dynamicFilter",myFilter);

        MappingJacksonValue productsFilters = new MappingJacksonValue(products);

        productsFilters.setFilters(filtersList);

        return  productsFilters;
    }

    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value="/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) throws ProduitIntrouvableException {
        logger.info("Début d'appel au service Produit de la requête: "+ requestContext.getHeader("req-id"));
        Product product = productDao.findById(id);
        if(product == null) throw new ProduitIntrouvableException("Le produit avec l'id " + id +" est introuvable.");
        return product;
    }

    @ApiOperation(value = "Affiche la marge des produits")
    @GetMapping(value="/AdminProduits")
    public Map<String, Integer> calculerMargeProduit() {
        logger.info("Début d'appel au service Produit de la requête: "+requestContext.getHeader("req-id"));
        Map<String, Integer> response = new HashMap<>();
        List<Product> products = productDao.findAll();
        products.forEach((e) -> {
            response.put(e.toString(),e.getPrix()-e.getPrixAchat());
        });
        return response;
    }

    @ApiOperation(value = "Range de manière alphabétique les produits.")
    @GetMapping(value="/sortProductsByLetters")
    public List<Product> trierProduitsParOrdreAlphabetique() {
        logger.info("Début d'appel au service Produit de la requête: "+requestContext.getHeader("req-id"));
        List<Product> products = productDao.findAll();
        List<Product> sortProductsByName = new ArrayList<>();

        Collections.sort(products, new Comparator<Product>() {
            @Override
            public int compare(final Product object1, final Product object2) {
                return object1.getNom().compareTo(object2.getNom());
            }
        });

        return products;
    }

    @GetMapping(value = "test/produits/{prixLimit}")
    public List<Product> testeDeRequetesPrix(@PathVariable int prixLimit) {
        logger.info("Début d'appel au service Produit de la requête: "+requestContext.getHeader("req-id"));
        return productDao.findByPrixGreaterThan(400);
    }

    @GetMapping(value = "test/produits/recherche/{recherche}")
    public List<Product> testeDeRequetesRecherche(@PathVariable String
                                                 recherche) {
        logger.info("Début d'appel au service Produit de la requête: "+requestContext.getHeader("req-id"));
        return productDao.findByNomLike("%"+recherche+"%");
    }

    @PostMapping(value="/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid  @RequestBody Product product) throws ProduitGratuitException {
        logger.info("Début d'appel au service Produit de la requête: "+requestContext.getHeader("req-id"));
        if(product.getPrix() == 0){
            throw new ProduitGratuitException("Tu ne pas pas vendre un produit gratuitement.");
        }

        Product productAdded = productDao.save(product);

        if( productAdded == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {
        logger.info("Début d'appel au service Produit de la requête: "+requestContext.getHeader("req-id"));
        productDao.deleteById(id);
    }

    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {
        logger.info("Début d'appel au service Produit de la requête: "+requestContext.getHeader("req-id"));
        productDao.save(product);
    }
}
