import React from 'react';
import { Disc, Play, Pause, Music } from 'lucide-react';

interface VinylPlayerProps {
  coverUrl: string;
  isPlaying: boolean;
  title: string;
  artist: string;
  codec: string;
  bitrate: string;
}

export const VinylPlayer: React.FC<VinylPlayerProps> = ({
  coverUrl,
  isPlaying,
  title,
  artist,
  codec,
  bitrate
}) => {
  return (
    <div className="relative flex flex-col items-center justify-center my-4 py-2">
      {/* Vinyl Turntable Base / Platter */}
      <div className="relative w-64 h-64 sm:w-80 sm:h-80 rounded-full flex items-center justify-center bg-gradient-to-tr from-neutral-900 via-neutral-950 to-neutral-900 shadow-2xl border-4 border-neutral-800/80">
        
        {/* Vinyl Record */}
        <div
          className={`relative w-60 h-60 sm:w-76 sm:h-76 rounded-full bg-neutral-950 shadow-inner flex items-center justify-center border border-neutral-800 transition-transform duration-700 ${
            isPlaying ? 'animate-spin' : ''
          }`}
          style={{ animationDuration: '3.5s' }}
        >
          {/* Groove Rings */}
          <div className="absolute inset-2 rounded-full border border-neutral-800/60 opacity-80" />
          <div className="absolute inset-6 rounded-full border border-neutral-800/50 opacity-70" />
          <div className="absolute inset-10 rounded-full border border-neutral-800/40 opacity-60" />
          <div className="absolute inset-14 rounded-full border border-neutral-800/30 opacity-50" />
          <div className="absolute inset-18 rounded-full border border-neutral-800/20 opacity-40" />

          {/* Vinyl Shiny Glare Overlay */}
          <div className="absolute inset-0 rounded-full bg-gradient-to-tr from-transparent via-white/5 to-transparent pointer-events-none" />

          {/* Center Album Art Label */}
          <div className="relative w-24 h-24 sm:w-30 sm:h-30 rounded-full overflow-hidden border-4 border-neutral-900 shadow-md flex items-center justify-center bg-neutral-900">
            {coverUrl ? (
              <img
                src={coverUrl}
                alt={title}
                className="w-full h-full object-cover"
                referrerPolicy="no-referrer"
              />
            ) : (
              <Music className="w-8 h-8 text-orange-500" />
            )}
            {/* Center Spindle Hole */}
            <div className="absolute w-4 h-4 rounded-full bg-neutral-950 border-2 border-neutral-700 shadow-inner" />
          </div>
        </div>

        {/* Tone Arm Armature */}
        <div className="absolute -top-3 -right-2 sm:-top-4 sm:-right-4 w-28 h-36 pointer-events-none z-10">
          {/* Pivot Base */}
          <div className="absolute top-0 right-4 w-8 h-8 rounded-full bg-gradient-to-b from-neutral-700 to-neutral-900 border border-neutral-600 shadow-lg" />
          {/* Tone Arm Rod */}
          <div
            className={`absolute top-4 right-7 w-2 h-28 bg-gradient-to-b from-neutral-300 via-neutral-500 to-neutral-400 rounded-full origin-top-right transition-transform duration-700 ease-in-out shadow-md ${
              isPlaying ? 'rotate-[24deg]' : 'rotate-[2deg]'
            }`}
          >
            {/* Cartridge Needle Head */}
            <div className="absolute -bottom-2 -left-1 w-4 h-6 bg-orange-600 rounded-sm shadow border border-orange-400" />
          </div>
        </div>
      </div>

      {/* Hi-Res Codec Badge */}
      <div className="mt-6 flex items-center space-x-2 px-3 py-1 rounded-full bg-neutral-900/90 border border-orange-500/30 text-xs font-mono text-orange-400">
        <span className="w-2 h-2 rounded-full bg-orange-500 animate-pulse" />
        <span className="font-semibold uppercase">{codec}</span>
        <span className="text-neutral-500">•</span>
        <span>{bitrate}</span>
      </div>

      {/* Track info overlay */}
      <div className="mt-3 text-center px-4 max-w-sm">
        <h2 className="text-xl font-bold text-white tracking-tight truncate">{title}</h2>
        <p className="text-sm text-neutral-400 mt-1 truncate">{artist}</p>
      </div>
    </div>
  );
};
