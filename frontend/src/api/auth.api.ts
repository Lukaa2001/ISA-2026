import client from './client';

export interface RegisterData {
  email: string;
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  address: string;
}

export interface LoginData {
  email: string;
  password: string;
}

export interface UserData {
  id: number;
  email: string;
  username: string;
  firstName: string;
  lastName: string;
  address?: string;
  createdAt?: string;
}

export const authApi = {
  register: (data: RegisterData) =>
    client.post('/auth/register', data),

  login: (data: LoginData) =>
    client.post<{ token: string; user: UserData }>('/auth/login', data),

  getMe: () =>
    client.get<UserData>('/auth/me'),
};
