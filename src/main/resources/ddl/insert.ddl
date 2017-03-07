DELETE FROM article;

INSERT INTO article (id_art, name, code, username, guid)
  SELECT 'id_art_' || x.id_art, 'name_' || x.id_art, 'code_' || x.id_art, 'username_' || x.id_art, 'guid_' || x.id_art
    FROM generate_series(1, 100000) AS x(id_art);