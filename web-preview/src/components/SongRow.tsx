import React from 'react';
import { Song } from '../types';
import { MoreVertical, Heart, Music, Play } from 'lucide-react';

interface SongRowProps {
  song: Song;
  isCurrent: boolean;
  isPlaying: boolean;
  onSelect: () => void;
  onToggleFavorite: (songId: string, e: React.MouseEvent) => void;
  onOpenContextMenu: (song: Song, e: React.MouseEvent) => void;
}

export const SongRow: React.FC<SongRowProps> = ({
  song,
  isCurrent,
  isPlaying,
  onSelect,
  onToggleFavorite,
  onOpenContextMenu,
}) => {
  const formatDuration = (sec: number) => {
    const mins = Math.floor(sec / 60);
    const secs = Math.floor(sec % 60);
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  return (
    <div
      onClick={onSelect}
      className={`group relative flex items-center justify-between p-3 rounded-xl transition-all cursor-pointer border ${
        isCurrent
          ? 'bg-orange-500/10 border-orange-500/40 text-orange-400'
          : 'bg-neutral-900/60 hover:bg-neutral-800/80 border-neutral-800/60 text-neutral-200'
      }`}
    >
      <div className="flex items-center space-x-3.5 min-w-0">
        {/* Cover Thumbnail with overlay Play icon */}
        <div className="relative w-12 h-12 rounded-lg overflow-hidden bg-neutral-950 flex-shrink-0 border border-neutral-800">
          {song.coverUrl ? (
            <img
              src={song.coverUrl}
              alt={song.title}
              className="w-full h-full object-cover"
              referrerPolicy="no-referrer"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center bg-neutral-800 text-neutral-400">
              <Music className="w-6 h-6" />
            </div>
          )}
          {isCurrent && (
            <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
              {isPlaying ? (
                <div className="flex items-end space-x-0.5 h-4">
                  <span className="w-1 bg-orange-500 animate-bounce h-full" style={{ animationDelay: '0ms' }} />
                  <span className="w-1 bg-orange-500 animate-bounce h-full" style={{ animationDelay: '150ms' }} />
                  <span className="w-1 bg-orange-500 animate-bounce h-full" style={{ animationDelay: '300ms' }} />
                </div>
              ) : (
                <Play className="w-5 h-5 text-orange-400 fill-orange-400" />
              )}
            </div>
          )}
        </div>

        {/* Info */}
        <div className="min-w-0">
          <h4 className={`text-sm font-semibold truncate ${isCurrent ? 'text-orange-400' : 'text-white'}`}>
            {song.title}
          </h4>
          <p className="text-xs text-neutral-400 truncate mt-0.5">
            {song.artist} • <span className="text-neutral-500">{song.album}</span>
          </p>
          <div className="flex items-center space-x-2 mt-1">
            <span className="text-[10px] font-mono px-1.5 py-0.2 bg-neutral-800 text-neutral-300 rounded border border-neutral-700">
              {song.codec}
            </span>
            <span className="text-[10px] text-neutral-500 font-mono">
              {song.bitrate}
            </span>
          </div>
        </div>
      </div>

      {/* Right side controls */}
      <div className="flex items-center space-x-2 flex-shrink-0">
        <span className="text-xs text-neutral-400 font-mono hidden sm:inline">
          {formatDuration(song.duration)}
        </span>

        {/* Favorite heart */}
        <button
          onClick={(e) => onToggleFavorite(song.id, e)}
          className="p-1.5 text-neutral-400 hover:text-red-500 transition-colors"
          title="Favorito"
        >
          <Heart
            className={`w-4 h-4 ${
              song.isFavorite ? 'fill-red-500 text-red-500' : ''
            }`}
          />
        </button>

        {/* 3-dots Context menu */}
        <button
          onClick={(e) => {
            e.stopPropagation();
            onOpenContextMenu(song, e);
          }}
          className="p-1.5 text-neutral-400 hover:text-white rounded-lg hover:bg-neutral-800 transition-colors"
          title="Menú Opciones"
        >
          <MoreVertical className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
