export interface Song {
  id: string;
  title: string;
  artist: string;
  album: string;
  albumId?: string;
  duration: number; // seconds
  coverUrl: string;
  genre: string;
  year: number;
  codec: 'FLAC' | 'MP3' | 'AAC' | 'WAV' | 'OGG' | 'OPUS';
  bitrate: string; // e.g. "320 kbps" or "24-bit/96kHz"
  sampleRate: string; // e.g. "44.1 kHz" or "96.0 kHz"
  fileSize: string; // e.g. "8.4 MB"
  filePath: string;
  composer?: string;
  isFavorite: boolean;
  playCount: number;
  audioUrl?: string; // HTML5 audio source or synthesized waveform
  downloaded?: boolean;
}

export interface LyricLine {
  timestamp: number; // in seconds
  text: string;
  translation?: string;
}

export interface Lyrics {
  songId: string;
  isSynced: boolean;
  lines: LyricLine[];
  source: string;
}

export interface Playlist {
  id: string;
  name: string;
  description: string;
  songIds: string[];
  coverUrl?: string;
  createdAt: string;
}

export type TabType = 
  | 'songs' 
  | 'playlists' 
  | 'folders' 
  | 'albums' 
  | 'artists' 
  | 'genres' 
  | 'favorites' 
  | 'most_played' 
  | 'recently_added' 
  | 'history' 
  | 'downloads' 
  | 'podcasts' 
  | 'audiobooks';

export type SortField = 'title' | 'artist' | 'album' | 'duration' | 'playCount' | 'fileSize' | 'year';
export type SortOrder = 'asc' | 'desc';

export type VisualizerMode = 'spectrum' | 'bars' | 'waves' | 'particles' | 'circle';

export interface EqualizerPreset {
  name: string;
  gains: number[]; // 10 bands in dB (-12 to +12)
}

export interface AudioSettingsState {
  eqEnabled: boolean;
  presetName: string;
  bandGains: number[]; // 10 bands: 31, 62, 125, 250, 500, 1k, 2k, 4k, 8k, 16k
  preampDb: number;
  bassBoost: number; // 0 - 100
  virtualizer: number; // 0 - 100
  reverbPreset: string;
  playbackSpeed: number; // 0.5 to 2.0
  pitchPreserved: boolean;
  crossfadeDurationSec: number;
  gaplessPlayback: boolean;
  replayGain: boolean;
  volume: number; // 0 - 1
}

export type ViewMode = 
  | 'library'
  | 'player'
  | 'queue'
  | 'lyrics'
  | 'equalizer'
  | 'audiotools'
  | 'settings'
  | 'android-info';
