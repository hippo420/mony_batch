--계정 생성
CREATE USER finadm WITH PASSWORD '1234';
CREATE USER metadm WITH PASSWORD '1234';

-- $ finadm execute
-- public 스키마 소유자 변경 (finadm이 소유하도록)
ALTER SCHEMA public OWNER TO finadm;

-- metadm이 public 스키마 접근 가능하도록
GRANT USAGE ON SCHEMA public TO metadm;

-- metadm에게 finadm이 만든 기존 테이블 전체 권한 부여
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO metadm;

-- metadm에게 시퀀스 권한도 부여 (auto_increment용)
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO metadm;

-- 앞으로 finadm이 새로 만드는 테이블/시퀀스에도 자동 권한 부여
ALTER DEFAULT PRIVILEGES FOR USER finadm IN SCHEMA public
GRANT ALL PRIVILEGES ON TABLES TO metadm;

ALTER DEFAULT PRIVILEGES FOR USER finadm IN SCHEMA public
GRANT ALL PRIVILEGES ON SEQUENCES TO metadm;