import React, { useState } from 'react';
import { Song, Lyrics } from '../types';
import { MOCK_LYRICS } from '../data/mockData';
import {
  X,
  Clock,
  Search,
  Upload,
  Download,
  Type,
  Repeat,
  Copy,
  Share2,
  Check
} from 'lucide-react';

interface LyricsScreenProps {
  song: Song | null;
  currentTime: number;
  onClose: () => void;
  onActionToast: (msg: string) => void;
}

export const LyricsScreen: React.FC<LyricsScreenProps> = ({
  song,
  currentTime,
  onClose,
  onActionToast
}) => {
  const [delayOffset, setDelayOffset] = useState<number>(0);
  const [fontSize, setFontSize] = useState<'sm' | 'md' | 'lg'>('md');
  const [copied, setCopied] = useState(false);

  if (!song) return null;

  const lyricsData: Lyrics | undefined = MOCK_LYRICS[song.id] || {
    songId: song.id,
    isSynced: false,
    source: 'Texto Plano',
    lines: [
      { timestamp: 0, text: `Letras para "${song.title}" de ${song.artist}` },
      { timestamp: 10, text: 'No se encontraron letras LRC sincronizadas automáticamente.' },
      { timestamp: 20, text: 'Haz clic en "Buscar letras" o "Importar .LRC" para sincronizar.' }
    ]
  };

  const adjustedTime = currentTime + delayOffset;

  // Find active lyric index
  let activeIndex = 0;
  for (let i = 0; i < lyricsData.lines.length; i++) {
    if (adjustedTime >= lyricsData.lines[i].timestamp) {
      activeIndex = i;
    }
  }

  const handleCopy = () => {
    const text = lyricsData.lines.map((l) => l.text).join('\n');
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 bg-neutral-950 flex flex-col animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between p-4 bg-neutral-900 border-b border-neutral-800">
        <div>
          <h2 className="text-base font-bold text-white">Letras Sincronizadas (Karaoke)</h2>
          <p className="text-xs text-orange-400 font-mono">{song.title} - {song.artist}</p>
        </div>
        <button
          onClick={onClose}
          className="p-1.5 text-neutral-400 hover:text-white rounded-lg hover:bg-neutral-800"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      {/* Control Bar: Offset delay, Font size, Search & Import */}
      <div className="flex flex-wrap items-center justify-between px-4 py-2.5 bg-neutral-900/80 border-b border-neutral-800 gap-2 text-xs">
        {/* Delay Offset Controls */}
        <div className="flex items-center space-x-2 bg-neutral-950 px-3 py-1.5 rounded-lg border border-neutral-800">
          <Clock className="w-3.5 h-3.5 text-orange-400" />
          <span className="text-neutral-400">Ajuste de Retraso:</span>
          <button
            onClick={() => setDelayOffset((d) => d - 0.5)}
            className="px-1.5 py-0.5 bg-neutral-800 hover:bg-neutral-700 rounded text-orange-400 font-bold"
          >
            -0.5s
          </button>
          <span className="font-mono text-white min-w-[40px] text-center">
            {delayOffset > 0 ? `+${delayOffset}` : delayOffset}s
          </span>
          <button
            onClick={() => setDelayOffset((d) => d + 0.5)}
            className="px-1.5 py-0.5 bg-neutral-800 hover:bg-neutral-700 rounded text-orange-400 font-bold"
          >
            +0.5s
          </button>
        </div>

        {/* Font size */}
        <div className="flex items-center space-x-1 bg-neutral-950 px-2 py-1.5 rounded-lg border border-neutral-800">
          <Type className="w-3.5 h-3.5 text-neutral-400" />
          {(['sm', 'md', 'lg'] as const).map((size) => (
            <button
              key={size}
              onClick={() => setFontSize(size)}
              className={`px-2 py-0.5 rounded text-[10px] uppercase font-bold ${
                fontSize === size ? 'bg-orange-500 text-white' : 'text-neutral-400'
              }`}
            >
              {size}
            </button>
          ))}
        </div>

        {/* Actions */}
        <div className="flex items-center space-x-1.5">
          <button
            onClick={() => onActionToast('Buscando letras en servidores de Musixmatch/LRCLIB...')}
            className="flex items-center space-x-1 px-2.5 py-1.5 bg-orange-500/20 text-orange-400 rounded-lg hover:bg-orange-500/30"
          >
            <Search className="w-3.5 h-3.5" />
            <span>Buscar Online</span>
          </button>
          <button
            onClick={() => onActionToast('Selecciona un archivo .LRC de tu almacenamiento')}
            className="p-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-300 rounded-lg"
            title="Importar .LRC"
          >
            <Upload className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={handleCopy}
            className="p-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-300 rounded-lg"
            title="Copiar Letras"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-green-400" /> : <Copy className="w-3.5 h-3.5" />}
          </button>
        </div>
      </div>

      {/* Synchronized Lyrics Container */}
      <div className="flex-1 overflow-y-auto p-6 text-center space-y-6 flex flex-col items-center justify-center">
        {lyricsData.lines.map((line, idx) => {
          const isActive = idx === activeIndex;
          const isPast = idx < activeIndex;

          let sizeClass = 'text-base sm:text-lg';
          if (fontSize === 'sm') sizeClass = 'text-sm sm:text-base';
          if (fontSize === 'lg') sizeClass = 'text-lg sm:text-2xl';

          return (
            <div
              key={idx}
              className={`transition-all duration-300 max-w-lg ${
                isActive
                  ? `${sizeClass} font-extrabold text-orange-400 scale-105 drop-shadow-md`
                  : isPast
                  ? 'text-neutral-600 text-xs sm:text-sm'
                  : 'text-neutral-400 text-xs sm:text-sm opacity-60'
              }`}
            >
              <p>{line.text}</p>
              {line.translation && (
                <p className="text-xs text-amber-500/70 font-normal mt-0.5">{line.translation}</p>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
