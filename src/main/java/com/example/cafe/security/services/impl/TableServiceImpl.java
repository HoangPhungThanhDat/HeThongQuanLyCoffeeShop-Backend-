package com.example.cafe.security.services.impl;

import com.example.cafe.entity.TableEntity;
import com.example.cafe.repository.TableRepository;
import com.example.cafe.security.services.TableService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TableServiceImpl implements TableService {
    private final TableRepository repo;

    public TableServiceImpl(TableRepository repo) { this.repo = repo; }

    @Override
    public TableEntity save(TableEntity t) { return repo.save(t); }

    @Override
    public TableEntity update(Long id, TableEntity t) {
        return repo.findById(id).map(existing -> {
            // Chỉ update trạng thái
            if (t.getStatus() != null) {
                existing.setStatus(t.getStatus());
            }
    
            // Nếu muốn có thể cập nhật thêm capacity hoặc number khi được gửi hợp lệ
            if (t.getCapacity() != null) {
                existing.setCapacity(t.getCapacity());
            }
    
            if (t.getNumber() != null) {
                existing.setNumber(t.getNumber());
            }
    
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Table not found"));
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }

    @Override
    public Optional<TableEntity> findById(Long id) { return repo.findById(id); }

    @Override
    public List<TableEntity> findAll() { return repo.findAll(); }
}