import client from './client';

export interface WatchPartyMember {
  id: number;
  user: {
    id: number;
    username: string;
  };
}

export interface WatchParty {
  id: number;
  roomCode: string;
  isActive: boolean;
  createdAt: string;
  creator: {
    id: number;
    username: string;
  };
  members: WatchPartyMember[];
  currentVideoId: number | null;
}

export const watchPartyApi = {
  create: () =>
    client.post<WatchParty>('/watch-party'),

  getRoom: (roomCode: string) =>
    client.get<WatchParty>(`/watch-party/${roomCode}`),

  join: (roomCode: string) =>
    client.post<WatchParty>(`/watch-party/${roomCode}/join`),

  close: (roomCode: string) =>
    client.delete(`/watch-party/${roomCode}`),
};
