import client from './client';

export interface Video {
  id: number;
  title: string;
  description: string;
  tags: string[];
  thumbnailPath: string;
  videoPath: string;
  viewCount: number;
  latitude: number | null;
  longitude: number | null;
  createdAt: string;
  user: {
    id: number;
    username: string;
    firstName?: string;
    lastName?: string;
  };
}

export interface PaginatedVideos {
  videos: Video[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}

export const videoApi = {
  list: (page = 1, limit = 12) =>
    client.get<PaginatedVideos>('/videos', { params: { page, limit } }),

  getById: (id: number) =>
    client.get<Video>(`/videos/${id}`),

  upload: (formData: FormData) =>
    client.post<Video>('/videos', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000,
    }),

  getThumbnailUrl: (id: number) => {
    const baseUrl = import.meta.env.VITE_API_URL || '';
    return `${baseUrl}/api/videos/${id}/thumbnail`;
  },

  getVideoStreamUrl: (videoPath: string) => {
    const baseUrl = import.meta.env.VITE_API_URL || '';
    return `${baseUrl}/uploads/${videoPath}`;
  },
};
