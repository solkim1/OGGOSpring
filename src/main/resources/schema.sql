DELIMITER //

DROP PROCEDURE IF EXISTS InsertTravelCourse //

CREATE PROCEDURE InsertTravelCourse(IN jsonData JSON, OUT result INT)
BEGIN
    -- 변수 선언
    DECLARE v_user_id VARCHAR(50);
    DECLARE v_sche_title VARCHAR(600);
    DECLARE v_sche_desc TEXT;
    DECLARE v_sche_st_dt DATE;
    DECLARE v_sche_st_tm TIME;
    DECLARE v_sche_ed_dt DATE;
    DECLARE v_sche_ed_tm TIME;
    DECLARE v_is_business CHAR(1);
    DECLARE v_poi_idx INT;
    DECLARE v_sche_num VARCHAR(50);
    DECLARE v_poi_name VARCHAR(200);
    DECLARE v_poi_type VARCHAR(50);
    DECLARE v_lat DECIMAL(17,14);
    DECLARE v_lng DECIMAL(17,14);
    DECLARE v_poi_desc LONGTEXT;
    DECLARE done INT DEFAULT FALSE;

    -- 커서 선언
    DECLARE cur CURSOR FOR 
    SELECT 
        user_id,
        sche_title,
        sche_desc,
        sche_st_dt,
        sche_st_tm,
        sche_ed_dt,
        sche_ed_tm,
        is_business,
        name,
        type,
        lat,
        lng,
        description,
        sche_num
    FROM JSON_TABLE(jsonData, '$[*]'
        COLUMNS (
            user_id VARCHAR(50) PATH '$.userId',
            sche_title VARCHAR(600) PATH '$.title',
            sche_desc TEXT PATH '$.description',
            sche_st_dt DATE PATH '$.startDate',
            sche_st_tm TIME PATH '$.departTime',
            sche_ed_dt DATE PATH '$.endDate',
            sche_ed_tm TIME PATH '$.arriveTime',
            is_business CHAR(1) PATH '$.isBusiness',
            name VARCHAR(200) PATH '$.name',
            type VARCHAR(50) PATH '$.type',
            lat DECIMAL(17,14) PATH '$.lat',
            lng DECIMAL(17,14) PATH '$.lng',
            description LONGTEXT PATH '$.description',
            sche_num VARCHAR(50) PATH '$.sche_num'
        )
    ) AS courses;

    -- 핸들러 선언
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN 
        SET result = 0;
        ROLLBACK;
    END;

    -- 트랜잭션 시작
    START TRANSACTION;

    -- 커서 열기
    OPEN cur;

    read_loop: LOOP
        -- 커서에서 데이터 가져오기
        FETCH cur INTO v_user_id, v_sche_title, v_sche_desc, v_sche_st_dt, v_sche_st_tm, v_sche_ed_dt, v_sche_ed_tm, v_is_business, v_poi_name, v_poi_type, v_lat, v_lng, v_poi_desc, v_sche_num;
        
        -- 데이터가 없으면 루프 종료
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- fake_poi 테이블에서 poi_name이 같은 레코드 찾기
        SELECT poi_idx INTO v_poi_idx
        FROM fake_poi
        WHERE poi_name = v_poi_name
        LIMIT 1;

        -- 레코드가 없는 경우 새로운 레코드 삽입 후 poi_idx 가져오기
        IF v_poi_idx IS NULL THEN
            INSERT INTO fake_poi (poi_name, poi_type, lat, lng, poi_desc)
            VALUES (v_poi_name, v_poi_type, v_lat, v_lng, v_poi_desc);
            
            -- 새로 삽입한 레코드의 poi_idx 가져오기
            SET v_poi_idx = LAST_INSERT_ID();
        END IF;

        -- tb_schedule 테이블에 데이터 삽입
        INSERT INTO tb_schedule (
            user_id,
            sche_title,
            sche_desc,
            sche_st_dt,
            sche_st_tm,
            sche_ed_dt,
            sche_ed_tm,
            is_business,
            poi_idx,
            sche_num
        ) VALUES (
            v_user_id,
            v_sche_title,
            v_sche_desc,
            v_sche_st_dt,
            v_sche_st_tm,
            v_sche_ed_dt,
            v_sche_ed_tm,
            v_is_business,
            v_poi_idx,
            v_sche_num
        );
    END LOOP;

    -- 커서 닫기
    CLOSE cur;

    -- 커밋 트랜잭션
    COMMIT;
    SET result = 1;
END //

DELIMITER ;
