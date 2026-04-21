CREATE OR REPLACE FUNCTION get_top_active_users(
    p_start_ts timestamp,
    p_end_ts timestamp
)
RETURNS TABLE (
    user_id bigint,
    username text,
    email text,
    name text,
    surname text,
    listings_created bigint,
    reviews_left bigint,
    avg_rating_left numeric,
    appointments_total bigint,
    appointments_done bigint,
    appointments_no_show bigint,
    appointments_cancelled bigint,
    favorites_saved_all_time bigint,
    activity_score bigint,
    activity_rank bigint
)
LANGUAGE sql
AS $$
WITH listings_by_user AS (
    SELECT
        c.user_id AS user_id,
        COUNT(*) AS listings_created
    FROM listings l
    JOIN owners o ON o.user_id = l.owner_id
    JOIN clients c ON c.user_id =o.user_id
    WHERE l.created_at >= p_start_ts
      AND l.created_at < p_end_ts
    GROUP BY c.user_id
),
reviews_by_user AS (
    SELECT
        r.reviewer_id AS user_id,
        COUNT(*) AS reviews_left,
        AVG(r.rating)::numeric(10,2) AS avg_rating_left
    FROM reviews r
    WHERE r.created_at >= p_start_ts
      AND r.created_at < p_end_ts
    GROUP BY r.reviewer_id
),
appointments_by_user AS (
    SELECT
        c.user_id AS user_id,
        COUNT(*) AS appointments_total,
        COUNT(*) FILTER (WHERE a.status = 'DONE') AS appointments_done,
        COUNT(*) FILTER (WHERE a.status = 'NO_SHOW') AS appointments_no_show,
        COUNT(*) FILTER (WHERE a.status = 'CANCELLED') AS appointments_cancelled
    FROM appointments a
    JOIN owners o ON o.user_id = a.responsible_owner_id
    JOIN clients c ON c.user_id = o.user_id
    WHERE a.date_time >= p_start_ts
      AND a.date_time < p_end_ts
    GROUP BY c.user_id
),
favorites_by_user AS (
    SELECT
        c.user_id AS user_id,
        COUNT(*) AS favorites_saved_all_time
    FROM favorite_listings f
    JOIN clients c ON c.user_id = f.client_id
    GROUP BY c.user_id
)
SELECT
    u.user_id,
    u.username::text,
    u.email::text,
    u.name::text,
    u.surname::text,
    COALESCE(l.listings_created, 0) AS listings_created,
    COALESCE(rv.reviews_left, 0) AS reviews_left,
    COALESCE(rv.avg_rating_left, 0) AS avg_rating_left,
    COALESCE(ap.appointments_total, 0) AS appointments_total,
    COALESCE(ap.appointments_done, 0) AS appointments_done,
    COALESCE(ap.appointments_no_show, 0) AS appointments_no_show,
    COALESCE(ap.appointments_cancelled, 0) AS appointments_cancelled,
    COALESCE(fv.favorites_saved_all_time, 0) AS favorites_saved_all_time,
    (
        COALESCE(l.listings_created, 0) * 5
            + COALESCE(rv.reviews_left, 0) * 3
            + COALESCE(ap.appointments_done, 0) * 2
            + COALESCE(fv.favorites_saved_all_time, 0)
            - COALESCE(ap.appointments_no_show, 0) * 2
        ) AS activity_score,
    DENSE_RANK() OVER (
        ORDER BY
            (
                COALESCE(l.listings_created, 0) * 5
                + COALESCE(rv.reviews_left, 0) * 3
                + COALESCE(ap.appointments_done, 0) * 2
                + COALESCE(fv.favorites_saved_all_time, 0)
                - COALESCE(ap.appointments_no_show, 0) * 2
            ) DESC,
            COALESCE(l.listings_created, 0) DESC,
            COALESCE(rv.reviews_left, 0) DESC,
            u.user_id ASC
    ) AS activity_rank
FROM users u
         LEFT JOIN listings_by_user l ON l.user_id = u.user_id
         LEFT JOIN reviews_by_user rv ON rv.user_id = u.user_id
         LEFT JOIN appointments_by_user ap ON ap.user_id = u.user_id
         LEFT JOIN favorites_by_user fv ON fv.user_id = u.user_id
WHERE COALESCE(l.listings_created, 0)
          + COALESCE(rv.reviews_left, 0)
          + COALESCE(ap.appointments_total, 0)
          + COALESCE(fv.favorites_saved_all_time, 0) > 0
ORDER BY activity_rank
    LIMIT 10;
$$;