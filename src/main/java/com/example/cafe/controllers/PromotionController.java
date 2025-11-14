package com.example.cafe.controllers;

import com.example.cafe.entity.Promotion;
import com.example.cafe.security.services.PromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    private final PromotionService service;
    public PromotionController(PromotionService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<Promotion>> getAll() { return ResponseEntity.ok(service.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Promotion> getOne(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Promotion> create(@RequestBody Promotion p) {
        return ResponseEntity.ok(service.save(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Promotion> update(@PathVariable Long id, @RequestBody Promotion p) {
        return ResponseEntity.ok(service.update(id, p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}