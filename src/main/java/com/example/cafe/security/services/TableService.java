package com.example.cafe.security.services;

import com.example.cafe.entity.TableEntity;

import java.util.List;
import java.util.Optional;

public interface TableService {
    TableEntity save(TableEntity t);
    TableEntity update(Long id, TableEntity t);
    void delete(Long id);
    Optional<TableEntity> findById(Long id);
    List<TableEntity> findAll();
}