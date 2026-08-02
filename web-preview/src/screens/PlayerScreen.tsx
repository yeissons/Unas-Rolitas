import React, { useState } from 'react';
import { Song, VisualizerMode } from '../types';
import { VinylPlayer } from '../components/VinylPlayer';
import { AudioVisualizer } from '../components/AudioVisualizer';
import {
  ChevronDown,
  ListMusic,
  FileText,
  Sliders,
  Shuffle,
  SkipBack,
  Play,
  Pause,
  SkipForward,
  Repeat,
  Repeat1,
  Heart,
  Volume2,
  Clock,
  Gauge,
  Sparkles,
  Zap
} from 'lucide-react';

interface PlayerScreenProps {
  song: Song | null;
  isPlaying: boolean;
  currentTime: number;
  repeatMode: 'off' | 'all' | 'one';
  isShuffle: boolean;
  playbackSpeed: number;
  sleepTimerMins: number | null;
  onClose: () => void;
  onTogglePlay: () => void;
  onNext: () => void;
  onPrev: () => void;
  onSeek: (sec: number) => void;
  onToggleRepeat: () => void;
  onToggleShuffle: () => void;
  onToggleFavorite: (id: string) => void;
  onChangeSpeed: (speed: number) => void;
  onSetSleepTimer: (mins: number | null) => void;
  onOpenQueue: () => void;
  onOpenLyrics: () => void;
  onOpenEqualizer: () => void;
}

export const PlayerScreen: React.FC<PlayerScreenProps> = ({
  song,
  isPlaying,
  currentTime,
  repeatMode,
  isShuffle,
  playbackSpeed,
  sleepTimerMins,
  onClose,
  onTogglePlay,
  onNext,
  onPrev,
  onSeek,
  onToggleRepeat,
  onToggleShuffle,
  onToggleFavorite,
  onChangeSpeed,
  onSetSleepTimer,
  onOpenQueue,
  onOpenLyrics,
  onOpenEqualizer
}) => {
  const [showVisualizer, setShowVisualizer] = useState(false);
  const [visualizerMode, setVisualizerMode] = useState<VisualizerMode>('spectrum');
  const [abLoopStart, setAbLoopStart] = useState<number | null>(null);
  const [abLoopEnd, setAbLoopEnd] = useState<number | null>(null);
  const [showSpeedMenu, setShowSpeedMenu] = useState(false);
  const [showTimerMenu, setShowTimerMenu] = useState(false);

  if (!song) return null;

  const formatTime = (sec: number) => {
    const mins = Math.floor(sec / 60);
    const secs = Math.floor(sec % 60);
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  const progressPercent = (currentTime / song.duration) * 100;

  return (
    <div className="fixed inset-0 z-50 bg-neutral-950 flex flex-col justify-between overflow-hidden animate-fade-in">
      {/* Blurred background image layer */}
      <div className="absolute inset-0 z-0 opacity-25 filter blur-3xl scale-125 pointer-events-none">
        {song.coverUrl && (
          <img src={song.coverUrl} alt={song.title} className="w-full h-full object-cover" />
        )}
      </div>

      {/* Top Bar */}
      <div className="relative z-10 flex items-center justify-between p-4 bg-gradient-to-b from-neutral-950/80 to-transparent">
        <button
          onClick={onClose}
          className="p-2 text-neutral-300 hover:text-white rounded-full bg-neutral-900/60 backdrop-blur"
        >
          <ChevronDown className="w-6 h-6" />
        </button>
        <div className="text-center">
          <span className="text-[10px] font-bold uppercase tracking-widest text-orange-400">
            Reproduciendo
          </span>
          <p className="text-xs font-semibold text-neutral-300 truncate max-w-[200px]">
            {song.album}
          </p>
        </div>
        <button
          onClick={() => onToggleFavorite(song.id)}
          className="p-2 text-neutral-300 hover:text-red-500 rounded-full bg-neutral-900/60 backdrop-blur"
        >
          <Heart className={`w-5 h-5 ${song.isFavorite ? 'fill-red-500 text-red-500' : ''}`} />
        </button>
      </div>

      {/* Center View: Toggle between Vinyl Record & Audio Visualizer */}
      <div className="relative z-10 flex-1 flex flex-col items-center justify-center px-4">
        {/* Toggle Mode Switcher */}
        <div className="flex items-center space-x-1 p-1 bg-neutral-900/80 backdrop-blur rounded-full border border-neutral-800 text-xs mb-2">
          <button
            onClick={() => setShowVisualizer(false)}
            className={`px-3 py-1 rounded-full font-medium transition-colors ${
              !showVisualizer ? 'bg-orange-500 text-white' : 'text-neutral-400 hover:text-white'
            }`}
          >
            Vinilo Giratorio
          </button>
          <button
            onClick={() => setShowVisualizer(true)}
            className={`px-3 py-1 rounded-full font-medium transition-colors ${
              showVisualizer ? 'bg-orange-500 text-white' : 'text-neutral-400 hover:text-white'
            }`}
          >
            Visualizador Audio
          </button>
        </div>

        {/* Display Area */}
        {!showVisualizer ? (
          <VinylPlayer
            coverUrl={song.coverUrl}
            isPlaying={isPlaying}
            title={song.title}
            artist={song.artist}
            codec={song.codec}
            bitrate={song.bitrate}
          />
        ) : (
          <div className="w-full max-w-sm flex flex-col items-center space-y-3">
            <AudioVisualizer
              mode={visualizerMode}
              isPlaying={isPlaying}
              className="w-full h-56 shadow-2xl"
            />
            {/* Visualizer mode selector */}
            <div className="flex items-center justify-center space-x-1.5 bg-neutral-900/90 border border-neutral-800 rounded-full p-1 text-[11px]">
              {(['spectrum', 'bars', 'waves', 'particles', 'circle'] as VisualizerMode[]).map((mode) => (
                <button
                  key={mode}
                  onClick={() => setVisualizerMode(mode)}
                  className={`px-2.5 py-1 rounded-full capitalize ${
                    visualizerMode === mode
                      ? 'bg-orange-500 text-white font-bold'
                      : 'text-neutral-400 hover:text-white'
                  }`}
                >
                  {mode}
                </button>
              ))}
            </div>
            <div className="text-center mt-2">
              <h3 className="text-lg font-bold text-white">{song.title}</h3>
              <p className="text-xs text-neutral-400">{song.artist}</p>
            </div>
          </div>
        )}
      </div>

      {/* Bottom Player Controls & Seek Section */}
      <div className="relative z-10 p-6 bg-gradient-to-t from-neutral-950 via-neutral-950/90 to-transparent space-y-4">
        {/* Timeline Slider */}
        <div className="space-y-1">
          <input
            type="range"
            min={0}
            max={song.duration}
            value={currentTime}
            onChange={(e) => onSeek(parseFloat(e.target.value))}
            className="w-full h-1.5 bg-neutral-800 rounded-lg appearance-none cursor-pointer accent-orange-500"
          />
          <div className="flex items-center justify-between text-xs text-neutral-400 font-mono">
            <span>{formatTime(currentTime)}</span>
            <span>-{formatTime(song.duration - currentTime)}</span>
          </div>
        </div>

        {/* Main Controls Row */}
        <div className="flex items-center justify-between px-2">
          {/* Shuffle */}
          <button
            onClick={onToggleShuffle}
            className={`p-2.5 rounded-full transition-colors ${
              isShuffle ? 'text-orange-400 bg-orange-500/10' : 'text-neutral-400 hover:text-white'
            }`}
            title="Aleatorio"
          >
            <Shuffle className="w-5 h-5" />
          </button>

          {/* Previous */}
          <button
            onClick={onPrev}
            className="p-3 text-neutral-200 hover:text-white active:scale-95 transition-transform"
            title="Anterior"
          >
            <SkipBack className="w-7 h-7" />
          </button>

          {/* Play / Pause Big Button */}
          <button
            onClick={onTogglePlay}
            className="p-5 rounded-full bg-gradient-to-tr from-orange-600 to-amber-500 text-white shadow-xl shadow-orange-500/30 hover:scale-105 active:scale-95 transition-transform"
            title={isPlaying ? 'Pausar' : 'Reproducir'}
          >
            {isPlaying ? (
              <Pause className="w-8 h-8" />
            ) : (
              <Play className="w-8 h-8 fill-white ml-0.5" />
            )}
          </button>

          {/* Next */}
          <button
            onClick={onNext}
            className="p-3 text-neutral-200 hover:text-white active:scale-95 transition-transform"
            title="Siguiente"
          >
            <SkipForward className="w-7 h-7" />
          </button>

          {/* Repeat */}
          <button
            onClick={onToggleRepeat}
            className={`p-2.5 rounded-full transition-colors ${
              repeatMode !== 'off'
                ? 'text-orange-400 bg-orange-500/10'
                : 'text-neutral-400 hover:text-white'
            }`}
            title="Modo repetición"
          >
            {repeatMode === 'one' ? (
              <Repeat1 className="w-5 h-5" />
            ) : (
              <Repeat className="w-5 h-5" />
            )}
          </button>
        </div>

        {/* Secondary Bar: Queue, Lyrics, Speed, Timer, EQ */}
        <div className="flex items-center justify-around pt-2 border-t border-neutral-900 text-neutral-400 text-xs">
          {/* Queue */}
          <button
            onClick={onOpenQueue}
            className="flex flex-col items-center space-y-1 hover:text-white"
          >
            <ListMusic className="w-5 h-5 text-orange-400" />
            <span className="text-[10px]">Cola</span>
          </button>

          {/* Lyrics */}
          <button
            onClick={onOpenLyrics}
            className="flex flex-col items-center space-y-1 hover:text-white"
          >
            <FileText className="w-5 h-5 text-orange-400" />
            <span className="text-[10px]">Letras</span>
          </button>

          {/* Equalizer */}
          <button
            onClick={onOpenEqualizer}
            className="flex flex-col items-center space-y-1 hover:text-white"
          >
            <Sliders className="w-5 h-5 text-orange-400" />
            <span className="text-[10px]">Ecualizador</span>
          </button>

          {/* Speed Selector */}
          <div className="relative">
            <button
              onClick={() => setShowSpeedMenu(!showSpeedMenu)}
              className="flex flex-col items-center space-y-1 hover:text-white"
            >
              <Gauge className="w-5 h-5 text-orange-400" />
              <span className="text-[10px]">{playbackSpeed}x</span>
            </button>
            {showSpeedMenu && (
              <div className="absolute bottom-10 -left-6 bg-neutral-900 border border-neutral-800 rounded-xl p-2 shadow-xl flex flex-col space-y-1 text-xs z-50 min-w-[90px]">
                {[0.5, 0.75, 1.0, 1.25, 1.5, 2.0].map((s) => (
                  <button
                    key={s}
                    onClick={() => {
                      onChangeSpeed(s);
                      setShowSpeedMenu(false);
                    }}
                    className={`px-3 py-1 rounded text-left ${
                      playbackSpeed === s ? 'bg-orange-500 text-white font-bold' : 'hover:bg-neutral-800'
                    }`}
                  >
                    {s}x
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Sleep Timer */}
          <div className="relative">
            <button
              onClick={() => setShowTimerMenu(!showTimerMenu)}
              className="flex flex-col items-center space-y-1 hover:text-white"
            >
              <Clock className="w-5 h-5 text-orange-400" />
              <span className="text-[10px]">
                {sleepTimerMins ? `${sleepTimerMins}m` : 'Temporizador'}
              </span>
            </button>
            {showTimerMenu && (
              <div className="absolute bottom-10 right-0 bg-neutral-900 border border-neutral-800 rounded-xl p-2 shadow-xl flex flex-col space-y-1 text-xs z-50 min-w-[110px]">
                {[null, 15, 30, 45, 60].map((m) => (
                  <button
                    key={m || 'off'}
                    onClick={() => {
                      onSetSleepTimer(m);
                      setShowTimerMenu(false);
                    }}
                    className={`px-3 py-1 rounded text-left ${
                      sleepTimerMins === m ? 'bg-orange-500 text-white font-bold' : 'hover:bg-neutral-800'
                    }`}
                  >
                    {m ? `${m} minutos` : 'Desactivado'}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
