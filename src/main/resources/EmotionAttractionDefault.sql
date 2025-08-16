INSERT INTO mood_trip_local.attraction_emotion_tags
(created_at, is_active, weight, attraction_id, tag_id, updated_at)
VALUES (NULL, b'1', 1.0, 1, 1, NULL);
INSERT INTO mood_trip_local.attraction_emotion_tags
(created_at, is_active, weight, attraction_id, tag_id, updated_at)
VALUES (NULL, b'1', 1.0, 1, 2, NULL);
INSERT INTO mood_trip_local.attraction_emotion_tags
(created_at, is_active, weight, attraction_id, tag_id, updated_at)
VALUES (NULL, b'1', 1.0, 1, 3, NULL);
INSERT INTO mood_trip_local.attraction_emotion_tags
(created_at, is_active, weight, attraction_id, tag_id, updated_at)
VALUES (NULL, b'1', 1.0, 1, 4, NULL);
INSERT INTO mood_trip_local.attraction_emotion_tags
(created_at, is_active, weight, attraction_id, tag_id, updated_at)
VALUES (NULL, b'1', 1.0, 1, 5, NULL);

-- (아래 같은 형식으로 2, 3, 4, 5번 attraction_id도 1~5번 tag_id로 쭉 반복)
INSERT INTO mood_trip_local.attraction_emotion_tags
(created_at, is_active, weight, attraction_id, tag_id, updated_at)
VALUES (NULL, b'1', 1.0, 2, 1, NULL);
INSERT INTO mood_trip_local.attraction_emotion_tags
(created_at, is_active, weight, attraction_id, tag_id, updated_at)
VALUES (NULL, b'1', 1.0, 2, 2, NULL);

-- attraction_id=5, tag_id=5까지 반복
