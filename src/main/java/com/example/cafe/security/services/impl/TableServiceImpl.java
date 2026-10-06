package com.example.cafe.security.services.impl;

import com.example.cafe.entity.TableEntity;
import com.example.cafe.repository.TableRepository;
import com.example.cafe.security.services.TableService;
import com.example.cafe.services.ActivityLogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TableServiceImpl implements TableService {

    private final TableRepository repo;
    private final ActivityLogService activityLogService;   // 👈 MỚI

    public TableServiceImpl(TableRepository repo,
                            ActivityLogService activityLogService) {   // 👈 MỚI
        this.repo = repo;
        this.activityLogService = activityLogService;
    }

    // ==================== SAVE ====================
    @Override
    public TableEntity save(TableEntity t) {
        TableEntity saved = repo.save(t);

        try {
            activityLogService.logCreate(
                    "Bàn",
                    "Bàn số " + saved.getNumber(),
                    "Thêm bàn mới",
                    String.format("Tạo bàn số %d — sức chứa %d người, trạng thái %s",
                            saved.getNumber(),
                            saved.getCapacity() != null ? saved.getCapacity() : 0,
                            saved.getStatus())
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log CREATE table: " + e.getMessage());
        }

        return saved;
    }

    // ==================== UPDATE ====================
    @Override
    public TableEntity update(Long id, TableEntity t) {
        TableEntity old = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        Object oldStatus = old.getStatus();
        Integer oldCapacity = old.getCapacity();
        Integer oldNumber = old.getNumber();

        TableEntity updated = repo.findById(id).map(existing -> {
            if (t.getStatus() != null) {
                existing.setStatus(t.getStatus());
            }
            if (t.getCapacity() != null) {
                existing.setCapacity(t.getCapacity());
            }
            if (t.getNumber() != null) {
                existing.setNumber(t.getNumber());
            }
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Table not found"));

        try {
            StringBuilder changes = new StringBuilder();
            if (oldNumber != null && !oldNumber.equals(updated.getNumber())) {
                changes.append("Số bàn: ").append(oldNumber).append(" → ")
                        .append(updated.getNumber()).append(". ");
            }
            if (oldCapacity != null && !oldCapacity.equals(updated.getCapacity())) {
                changes.append("Sức chứa: ").append(oldCapacity).append(" → ")
                        .append(updated.getCapacity()).append(". ");
            }
            if (oldStatus != null && !oldStatus.equals(updated.getStatus())) {
                changes.append("Trạng thái: ").append(oldStatus).append(" → ")
                        .append(updated.getStatus()).append(". ");
            }

            activityLogService.logUpdate(
                    "Bàn",
                    "Bàn số " + updated.getNumber(),
                    "Cập nhật bàn",
                    changes.length() > 0
                            ? changes.toString().trim()
                            : "Cập nhật bàn số " + updated.getNumber()
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log UPDATE table: " + e.getMessage());
        }

        return updated;
    }

    // ==================== DELETE ====================
    @Override
    public void delete(Long id) {
        TableEntity t = repo.findById(id).orElse(null);
        String name = t != null ? "Bàn số " + t.getNumber() : "ID " + id;

        repo.deleteById(id);

        try {
            activityLogService.logDelete(
                    "Bàn",
                    name,
                    "Xóa bàn",
                    "Đã xóa " + name + " khỏi hệ thống"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log DELETE table: " + e.getMessage());
        }
    }

    @Override
    public Optional<TableEntity> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<TableEntity> findAll() {
        return repo.findAll();
    }
}