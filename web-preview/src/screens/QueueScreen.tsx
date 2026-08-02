import React from 'react';
import { Song } from '../types';
import {
  X,
  Trash2,
  Shuffle,
  Save,
  GripVertical,
  Play,
  Volume2,
  Music
} from 'lucide-react';

interface QueueScreenProps {
  queue: Song[];
  currentSongIndex: number;
  isPlaying: boolean;
  onClose: () => void;
  onSelectSongIndex: (index: number) => void;
  onRemoveFromQueue: (index: number) => void;
  onClearQueue: () => void;
  onShuffleQueue: () => void;
  onSaveAsPlaylist: () => void;
}

export const QueueScreen: React.FC<QueueScreenProps> = ({
  queue,
  currentSongIndex,
  isPlaying,
  onClose,
  onSelectSongIndex,
  onRemoveFromQueue,
  onClearQueue,
  onShuffleQueue,
  onSaveAsPlaylist
}) => {
  return (
    <div className="fixed inset-0 z-50 bg-neutral-950 flex flex-col animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between p-4 bg-neutral-900 border-b border-neutral-800">
        <div className="flex items-center space-x-2">
          <h2 className="text-lg font-bold text-white">Cola de Reproducción</h2>
          <span className="text-xs px-2 py-0.5 bg-orange-500/20 text-orange-400 font-mono rounded-full">
            {queue.length} rolitas
          </span>
        </div>
        <button
          onClick={onClose}
          className="p-1.5 text-neutral-400 hover:text-white rounded-lg hover:bg-neutral-800"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      {/* Action Toolbar */}
      <div className="flex items-center justify-between px-4 py-2 bg-neutral-900/60 border-b border-neutral-800/60 text-xs">
        <button
          onClick={onShuffleQueue}
          className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-neutral-800 hover:bg-neutral-700 text-neutral-200"
        >
          <Shuffle className="w-3.5 h-3.5 text-orange-400" />
          <span>Aleatorizar</span>
        </button>
        <button
          onClick={onSaveAsPlaylist}
          className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-neutral-800 hover:bg-neutral-700 text-neutral-200"
        >
          <Save className="w-3.5 h-3.5 text-orange-400" />
          <span>Guardar como lista</span>
        </button>
        <button
          onClick={onClearQueue}
          className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-neutral-800 hover:bg-red-500/20 text-red-400"
        >
          <Trash2 className="w-3.5 h-3.5" />
          <span>Vaciar</span>
        </button>
      </div>

      {/* Song list */}
      <div className="flex-1 overflow-y-auto p-3 space-y-2">
        {queue.map((song, index) => {
          const isCurrent = index === currentSongIndex;
          return (
            <div
              key={`${song.id}-${index}`}
              onClick={() => onSelectSongIndex(index)}
              className={`flex items-center justify-between p-3 rounded-xl border transition-all cursor-pointer ${
                isCurrent
                  ? 'bg-orange-500/15 border-orange-500/50 text-orange-400 shadow-md'
                  : 'bg-neutral-900/80 hover:bg-neutral-800 border-neutral-800 text-neutral-300'
              }`}
            >
              <div className="flex items-center space-x-3 min-w-0">
                <GripVertical className="w-4 h-4 text-neutral-600 cursor-grab flex-shrink-0" />
                <div className="relative w-10 h-10 rounded-lg overflow-hidden bg-neutral-950 flex-shrink-0">
                  <img
                    src={song.coverUrl}
                    alt={song.title}
                    className="w-full h-full object-cover"
                    referrerPolicy="no-referrer"
                  />
                  {isCurrent && (
                    <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
                      <Volume2 className="w-5 h-5 text-orange-400 animate-pulse" />
                    </div>
                  )}
                </div>
                <div className="min-w-0">
                  <h4 className={`text-xs font-bold truncate ${isCurrent ? 'text-orange-400' : 'text-white'}`}>
                    {index + 1}. {song.title}
                  </h4>
                  <p className="text-[11px] text-neutral-400 truncate">{song.artist}</p>
                </div>
              </div>

              <div className="flex items-center space-x-2 flex-shrink-0">
                <span className="text-[10px] font-mono text-neutral-500">
                  {Math.floor(song.duration / 60)}:
                  {(song.duration % 60) < 10 ? '0' : ''}
                  {Math.floor(song.duration % 60)}
                </span>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onRemoveFromQueue(index);
                  }}
                  className="p-1.5 text-neutral-500 hover:text-red-400 rounded-lg hover:bg-neutral-800"
                  title="Quitar"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
