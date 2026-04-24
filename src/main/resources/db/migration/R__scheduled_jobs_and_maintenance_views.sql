create or replace procedure job_mark_no_show()
language plpgsql
as $$
    begin
        update appointments
        set status = 'NO_SHOW'
        where status = 'CONFIRMED'
        and date_time < now() - interval '45 minutes';
    end;
    $$;


create or replace procedure job_archive_stale_drafts()
language plpgsql
as $$
    begin
        UPDATE listings
        SET status = 'ARCHIVED'
        WHERE status = 'DRAFT'
          AND created_at < now() - interval '30 days';
    end;
    $$;
-- check again for errors

CREATE OR REPLACE VIEW v_overdue_confirmed_appointments AS
SELECT *
FROM appointments
WHERE status = 'CONFIRMED'
  AND date_time < now() - interval '45 minutes';


-- View: draft listings that should be archived
CREATE OR REPLACE VIEW v_stale_draft_listings AS
SELECT *
FROM listings
WHERE status = 'DRAFT'
  AND created_at < now() - interval '30 days';