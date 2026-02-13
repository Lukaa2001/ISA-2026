import client from './client';

export interface Comment {
  id: number;
  text: string;
  createdAt: string;
  user: {
    id: number;
    username: string;
  };
}

export interface PaginatedComments {
  comments: Comment[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}

export const commentApi = {
  list: (videoId: number, page = 1, limit = 20) =>
    client.get<PaginatedComments>(`/videos/${videoId}/comments`, { params: { page, limit } }),

  create: (videoId: number, text: string) =>
    client.post<Comment>(`/videos/${videoId}/comments`, { text }),
};
