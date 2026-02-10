CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  username VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  address VARCHAR(255) NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT FALSE,
  activation_token VARCHAR(255) UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS videos (
  id SERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  tags TEXT[] NOT NULL DEFAULT '{}',
  thumbnail_path VARCHAR(512) NOT NULL,
  video_path VARCHAR(512) NOT NULL,
  view_count INTEGER NOT NULL DEFAULT 0,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  user_id INTEGER NOT NULL REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
  id SERIAL PRIMARY KEY,
  text TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  user_id INTEGER NOT NULL REFERENCES users(id),
  video_id INTEGER NOT NULL REFERENCES videos(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS watch_parties (
  id SERIAL PRIMARY KEY,
  room_code VARCHAR(64) NOT NULL UNIQUE,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  creator_id INTEGER NOT NULL REFERENCES users(id),
  current_video_id INTEGER
);

CREATE TABLE IF NOT EXISTS watch_party_members (
  id SERIAL PRIMARY KEY,
  joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  user_id INTEGER NOT NULL REFERENCES users(id),
  watch_party_id INTEGER NOT NULL REFERENCES watch_parties(id) ON DELETE CASCADE,
  CONSTRAINT uq_watch_party_member UNIQUE (user_id, watch_party_id)
);

CREATE INDEX IF NOT EXISTS idx_videos_created_at_desc ON videos (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comments_video_created_at_desc ON comments (video_id, created_at DESC);
