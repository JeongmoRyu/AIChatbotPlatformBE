CREATE TABLE IF NOT EXISTS chathub.organization (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    use_yn CHAR(1) NOT NULL DEFAULT 'Y',  -- 활성 여부 ('Y': 사용, 'N': 비활성)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (use_yn IN ('Y', 'N'))  -- 'Y' 또는 'N'만 허용
);

INSERT INTO chathub.organization (id, name)
VALUES (1, 'My Organization')
ON CONFLICT DO NOTHING;

SELECT setval('organization_id_seq', (SELECT MAX(id) FROM chathub.organization));

CREATE TABLE IF NOT EXISTS chathub.member_organization (
    member_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    PRIMARY KEY (member_id, organization_id),
    FOREIGN KEY (member_id) REFERENCES chathub.member(user_key),
    FOREIGN KEY (organization_id) REFERENCES chathub.organization(id)
);

INSERT INTO chathub.member_organization (member_id, organization_id)
SELECT user_key, 1
FROM chathub.member
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS chathub.chatbot_info_organization (
    chatbot_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    PRIMARY KEY (chatbot_id, organization_id),
    FOREIGN KEY (chatbot_id) REFERENCES chathub.chatbot_info(id),
    FOREIGN KEY (organization_id) REFERENCES chathub.organization(id)
);

INSERT INTO chathub.chatbot_info_organization (chatbot_id, organization_id)
SELECT id, 1
FROM chathub.chatbot_info
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS chathub.function_organization (
    function_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    PRIMARY KEY (function_id, organization_id),
    FOREIGN KEY (function_id) REFERENCES chathub.base_member_function(id),
    FOREIGN KEY (organization_id) REFERENCES chathub.organization(id)
);

INSERT INTO chathub.function_organization (function_id, organization_id)
SELECT id, 1
FROM chathub.base_member_function
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS chathub.role (
	id SERIAL PRIMARY KEY,
	name VARCHAR(50) NOT NULL UNIQUE,  -- 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN' 등
	role_level INT NOT NULL            -- 권한 계층 (4=SUPER_ADMIN, 3=ADMIN, 2=EDITOR, 1=USER)
);

INSERT INTO chathub.role (id, name, role_level)
VALUES
(1, 'ROLE_USER', 1),
(2, 'ROLE_EDITOR', 2),
(3, 'ROLE_ADMIN', 3),
(4, 'ROLE_SUPER_ADMIN', 4)
ON CONFLICT DO NOTHING;

SELECT setval('role_id_seq', (SELECT MAX(id) FROM chathub.role));

CREATE TABLE IF NOT EXISTS chathub.menu (
	id BIGSERIAL PRIMARY KEY,
	title VARCHAR(255) NOT NULL,
	sub_title VARCHAR(255) NULL,
	description TEXT NULL,
	"to" VARCHAR(255) NOT NULL,
    menu_type VARCHAR(50) NOT NULL,    -- 'TOP' 탑메뉴, 'HOME' 홈메뉴
    seq INT4 NOT NULL DEFAULT 1,
	use_yn CHAR(1) NOT NULL DEFAULT 'Y',
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CHECK (use_yn IN ('Y', 'N'))
);

INSERT INTO chathub.menu (id, title, sub_title, description, "to", menu_type, seq)
VALUES
(1, 'menu:AI와의_대화', '' , 'menu:다양한_LLM_모델과_대화해보세요', '/ai-chat', 'HOME', 1),
(2, 'menu:챗플레이', 'Chat Play', 'menu:쉽고_빠르게_문서를_모델에_학습시키고_해당_문서에_대한_대화를_나눌_수_있습니다', '/chat-play', 'HOME', 2),
(3, 'menu:챗허브', 'Chat Hub', 'menu:Input_데이터와_Output_설정을_조정하여_정교한_커스텀챗봇을_만들_수_있습니다', '/chat-hub', 'HOME', 3),
(4, 'Embedding Ranker', '', 'menu:다양한_임베딩_모델을_시험하고_성능_비교를_통해_문서에_최적화된_조합을_찾을_수_있습니다.', '/embedding-history', 'HOME', 4),
(5, 'menu:홈', '' , '', '/home', 'TOP', 1),
(6, 'menu:AI와의_대화', '' , '', '/ai-chat', 'TOP', 2),
(7, 'menu:챗플레이', '', '', '/chat-play', 'TOP', 3),
(8, 'menu:챗허브', '', '', '/chat-hub', 'TOP', 4),
(9, 'Embedding Ranker', '', '', '/embedding-history', 'TOP', 5)
ON CONFLICT DO NOTHING;

SELECT setval('menu_id_seq', (SELECT MAX(id) FROM chathub.menu));

CREATE TABLE IF NOT EXISTS chathub.menu_organization (
	menu_id BIGINT NOT NULL,
	organization_id BIGINT NOT NULL,
	PRIMARY KEY (menu_id, organization_id),
	FOREIGN KEY (menu_id) REFERENCES chathub.menu(id),
	FOREIGN KEY (organization_id) REFERENCES chathub.organization(id)
);

CREATE INDEX IF NOT EXISTS idx_menu_organization ON menu_organization(menu_id);

INSERT INTO chathub.menu_organization (menu_id, organization_id)
SELECT id, 1
FROM chathub.menu
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS chathub.menu_role (
	menu_id BIGINT NOT NULL,
	min_role_level INT NOT NULL,  -- 최소 접근 가능 권한 (1: USER 이상, 2: EDITOR 이상, 3: ADMIN 이상, 4: SUPER_ADMIN만)
	PRIMARY KEY (menu_id, min_role_level),
	FOREIGN KEY (menu_id) REFERENCES chathub.menu(id)
);

CREATE INDEX IF NOT EXISTS idx_menu_role ON menu_role(menu_id);

INSERT INTO chathub.menu_role (menu_id, min_role_level)
VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 3), -- Embedding Ranker admin 이상만
(5, 1),
(6, 1),
(7, 1),
(8, 1),
(9, 3)  -- Embedding Ranker admin 이상만
ON CONFLICT DO NOTHING;