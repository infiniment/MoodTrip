-- emotion_category (대분류) 데이터 삽입
INSERT INTO emotion_category (emotion_category_name, emotion_category_icon, display_order) VALUES
                                                                                               ('평온 & 힐링', '🌿', 1),
                                                                                               ('사랑 & 로맨스', '💕', 2),
                                                                                               ('모험 & 스릴', '⚡', 3),
                                                                                               ('자유 & 해방', '🕊️', 4),
                                                                                               ('기쁨 & 즐거움', '✨', 5),
                                                                                               ('감성 & 예술', '🎨', 6),
                                                                                               ('열정 & 에너지', '🔥', 7),
                                                                                               ('성찰 & 사색', '🤔', 8),
                                                                                               ('위로 & 공감', '😌', 9),
                                                                                               ('희망 & 긍정', '🌟', 10),
                                                                                               ('우울 & 슬픔', '😔', 11),
                                                                                               ('불안 & 걱정', '😰', 12),
                                                                                               ('분노 & 짜증', '😡', 13),
                                                                                               ('피로 & 무기력', '😴', 14),
                                                                                               ('놀라움 & 신기함', '😲', 15);

-- M_emotion (소분류) 데이터 삽입
-- 카테고리 ID는 위 INSERT 순서에 따라 1부터 15까지 부여된다고 가정합니다.

-- 1. 평온 & 힐링 (emotion_category_id = 1)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (1, '평온', 1), (1, '안정', 2), (1, '휴식', 3), (1, '치유', 4), (1, '명상', 5), (1, '고요', 6), (1, '위안', 7), (1, '여유', 8);

-- 2. 사랑 & 로맨스 (emotion_category_id = 2)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (2, '설렘', 1), (2, '낭만', 2), (2, '사랑', 3), (2, '애정', 4), (2, '달콤함', 5), (2, '애틋함', 6), (2, '그리움', 7), (2, '감성', 8);

-- 3. 모험 & 스릴 (emotion_category_id = 3)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (3, '모험', 1), (3, '스릴', 2), (3, '도전', 3), (3, '짜릿함', 4), (3, '흥미', 5), (3, '용기', 6), (3, '대담함', 7), (3, '역동성', 8);

-- 4. 자유 & 해방 (emotion_category_id = 4)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (4, '자유', 1), (4, '해방', 2), (4, '독립', 3), (4, '개방감', 4), (4, '무구속', 5), (4, '탈출', 6), (4, '경쾌함', 7), (4, '시원함', 8);

-- 5. 기쁨 & 즐거움 (emotion_category_id = 5)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (5, '기쁨', 1), (5, '즐거움', 2), (5, '행복', 3), (5, '만족', 4), (5, '환희', 5), (5, '황홀감', 6), (5, '즐거운', 7), (5, '흥겨움', 8);

-- 6. 감성 & 예술 (emotion_category_id = 6)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (6, '감성', 1), (6, '영감', 2), (6, '창조력', 3), (6, '미적감각', 4), (6, '몽환적', 5), (6, '신비로움', 6), (6, '예술적', 7), (6, '감동', 8);

-- 7. 열정 & 에너지 (emotion_category_id = 7)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (7, '열정', 1), (7, '에너지', 2), (7, '활력', 3), (7, '패기', 4), (7, '의욕', 5), (7, '동기부여', 6), (7, '생동감', 7), (7, '벅찬', 8);

-- 8. 성찰 & 사색 (emotion_category_id = 8)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (8, '성찰', 1), (8, '사색', 2), (8, '고민', 3), (8, '깊이', 4), (8, '철학적', 5), (8, '내면탐구', 6), (8, '명상적', 7), (8, '깨달음', 8);

-- 9. 위로 & 공감 (emotion_category_id = 9)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (9, '위로', 1), (9, '공감', 2), (9, '따뜻함', 3), (9, '포근함', 4), (9, '친밀감', 5), (9, '소속감', 6), (9, '이해', 7), (9, '연대감', 8);

-- 10. 희망 & 긍정 (emotion_category_id = 10)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (10, '희망', 1), (10, '긍정', 2), (10, '낙관', 3), (10, '기대', 4), (10, '설렘', 5), (10, '꿈', 6), (10, '비전', 7), (10, '미래지향', 8);

-- 11. 우울 & 슬픔 (emotion_category_id = 11)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (11, '우울', 1), (11, '슬픔', 2), (11, '눈물', 3), (11, '상실감', 4), (11, '외로움', 5), (11, '허무함', 6), (11, '회한', 7), (11, '아련함', 8);

-- 12. 불안 & 걱정 (emotion_category_id = 12)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (12, '불안', 1), (12, '걱정', 2), (12, '초조함', 3), (12, '두려움', 4), (12, '긴장', 5), (12, '스트레스', 6), (12, '압박감', 7), (12, '부담', 8);

-- 13. 분노 & 짜증 (emotion_category_id = 13)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (13, '분노', 1), (13, '짜증', 2), (13, '화남', 3), (13, '억울함', 4), (13, '분함', 5), (13, '좌절', 6), (13, '갑갑함', 7), (13, '답답함', 8);

-- 14. 피로 & 무기력 (emotion_category_id = 14)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (14, '피로', 1), (14, '무기력', 2), (14, '지침', 3), (14, '나른함', 4), (14, '무료함', 5), (14, '권태', 6), (14, '침체', 7), (14, '소진', 8);

-- 15. 놀라움 & 신기함 (emotion_category_id = 15)
INSERT INTO M_emotion (emotion_category_id, tag_name, display_order) VALUES
                                                                         (15, '놀라움', 1), (15, '신기함', 2), (15, '경이로움', 3), (15, '신선함', 4), (15, '호기심', 5), (15, '흥미진진', 6), (15, '충격', 7), (15, '감탄', 8);
