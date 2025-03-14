CREATE TABLE IF NOT EXISTS chathub.engine_organization (
    engine_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    PRIMARY KEY (engine_id, organization_id),
    FOREIGN KEY (engine_id) REFERENCES chathub.engine(id),
    FOREIGN KEY (organization_id) REFERENCES chathub.organization(id)
);

INSERT INTO chathub.engine_organization (engine_id, organization_id)
SELECT e.id AS engine_id, o.id AS organization_id
FROM chathub.engine e
CROSS JOIN chathub.organization o
ON CONFLICT DO NOTHING;

