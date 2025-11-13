INSERT INTO categories(category_name) VALUES ('Books') ON CONFLICT DO NOTHING;
INSERT INTO categories(category_name) VALUES ('Electronics') ON CONFLICT DO NOTHING;
INSERT INTO categories(category_name) VALUES ('Services') ON CONFLICT DO NOTHING;
