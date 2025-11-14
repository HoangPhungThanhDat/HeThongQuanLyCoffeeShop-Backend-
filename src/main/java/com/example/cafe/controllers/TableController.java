package com.example.cafe.controllers;

import com.example.cafe.entity.TableEntity;
import com.example.cafe.security.services.TableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {
    private final TableService service;
    public TableController(TableService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<TableEntity>> getAll() { return ResponseEntity.ok(service.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<TableEntity> getOne(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TableEntity> create(@RequestBody TableEntity t) { return ResponseEntity.ok(service.save(t)); }

    @PutMapping("/{id}")
    public ResponseEntity<TableEntity> update(@PathVariable Long id, @RequestBody TableEntity t) {
        return ResponseEntity.ok(service.update(id, t));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}