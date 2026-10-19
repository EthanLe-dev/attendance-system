package com.attendance_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaUpdater implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // Cập nhật schema an toàn cho MySQL để cho phép null trên các trường toạ độ và selfie
        // khi điểm danh thủ công hoặc bỏ qua GPS
        String[] alterStatements = {
                "ALTER TABLE attendance MODIFY COLUMN latitude DECIMAL(10,7) NULL",
                "ALTER TABLE attendance MODIFY COLUMN longitude DECIMAL(10,7) NULL",
                "ALTER TABLE attendance MODIFY COLUMN accuracy DECIMAL(10,2) NULL",
                "ALTER TABLE attendance MODIFY COLUMN selfie_path VARCHAR(500) NULL",
                "ALTER TABLE student MODIFY COLUMN class_name VARCHAR(50) NULL",
                "ALTER TABLE student MODIFY COLUMN class_order INT NULL",
                "ALTER TABLE student ADD COLUMN group_number INT NULL"
        };

        for (String sql : alterStatements) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                // Bỏ qua nếu bảng chưa tồn tại hoặc DB khác MySQL / đã được chỉnh sửa
                log.debug("Schema update note for '{}': {}", sql, e.getMessage());
            }
        }
    }
}
