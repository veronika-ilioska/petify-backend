package com.petify.petify.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceScheduler {

    private final JdbcTemplate jdbcTemplate;

    public MaintenanceScheduler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void markNoShowAppointments() {
        jdbcTemplate.execute("CALL job_mark_no_show()");
    }

    @Scheduled(cron = "0 10 2 * * *")
    public void archiveStaleDraftListings() {
        jdbcTemplate.execute("CALL job_archive_stale_drafts()");
    }
}
