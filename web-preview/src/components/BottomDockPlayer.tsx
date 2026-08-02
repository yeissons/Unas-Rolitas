import React from 'react';
import { Song } from '../types';
import { Play, Pause, SkipForward, Disc3, ListMusic, FileText } from 'lucide-react';

interface BottomDockPlayerProps {
  currentSong: Song | null;
  isPlaying: boolean;
  currentTime: number;
  onTogglePlay: () => void;
  onNext: () => void;
  onOpenPlayer: () => void;
  onOpenQueue: () => void;
  onOpenLyrics: () => void;
}

export const BottomDockPlayer: React.FC<BottomDockPlayerProps> = ({
  currentSong,
  isPlaying,
  currentTime,
  onTogglePlay,
  onNext,
  onOpenPlayer,
  onOpenQueue,
  onOpenLyrics
}) => {
  if (!currentSong) return null;

  const progressPercent = (currentTime / currentSong.duration) * 100;

  return (
    <div
      onClick={onOpenPlayer}
      className="fixed bottom-0 left-0 right-0 z-40 bg-neutral-950/95 backdrop-blur-xl border-t border-neutral-800 shadow-2xl cursor-pointer hover:bg-neutral-900/90 transition-colors"
    >
      {/* Top progress line */}
      <div className="w-full bg-neutral-800 h-1">
        <div
          className="bg-orange-500 h-1 transition-all duration-300"
          style={{ width: `${Math.min(100, Math.max(0, progressPercent))}%` }}
        />
      </div>

      <div className="flex items-center justify-between px-3 py-2 max-w-7xl mx-auto">
        {/* Left: Song Art & Info */}
        <div className="flex items-center space-x-3 min-w-0">
          <div className="relative w-11 h-11 rounded-lg overflow-hidden bg-neutral-900 border border-neutral-800 flex-shrink-0">
            {currentSong.coverUrl ? (
              <img
                src={currentSong.coverUrl}
                alt={currentSong.title}
                className={`w-full h-full object-cover ${isPlaying ? 'animate-pulse' : ''}`}
                referrerPolicy="no-referrer"
              />
            ) : (
              <Disc3 className="w-6 h-6 text-orange-500 m-auto" />
            )}
          </div>
          <div className="min-w-0">
            <h4 className="text-xs font-bold text-white truncate">{currentSong.title}</h4>
            <p className="text-[11px] text-neutral-400 truncate">{currentSong.artist}</p>
          </div>
        </div>

        {/* Right: Controls & Shortcuts */}
        <div
          onClick={(e) => e.stopPropagation()}
          className="flex items-center space-x-2 flex-shrink-0"
        >
          <button
            onClick={onOpenLyrics}
            className="p-2 text-neutral-400 hover:text-white rounded-full hover:bg-neutral-800"
            title="Letras"
          >
            <FileText className="w-4 h-4" />
          </button>
          <button
            onClick={onOpenQueue}
            className="p-2 text-neutral-400 hover:text-white rounded-full hover:bg-neutral-800"
            title="Cola"
          >
            <ListMusic className="w-4 h-4" />
          </button>
          <button
            onClick={onTogglePlay}
            className="p-2.5 rounded-full bg-orange-500 text-white hover:bg-orange-600 transition-transform active:scale-95 shadow-md shadow-orange-500/30"
            title={isPlaying ? 'Pausar' : 'Reproducir'}
          >
            {isPlaying ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4 fill-white" />}
          </button>
          <button
            onClick={onNext}
            className="p-2 text-neutral-300 hover:text-white rounded-full hover:bg-neutral-800"
            title="Siguiente"
          >
            <SkipForward className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
