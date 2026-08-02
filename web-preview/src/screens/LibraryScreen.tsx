import React from 'react';
import { Song, Playlist, TabType } from '../types';
import { SongRow } from '../components/SongRow';
import { Music, Folder, Disc, Mic2, Heart, ListMusic, Download, Radio, BookOpen } from 'lucide-react';

interface LibraryScreenProps {
  currentTab: TabType;
  songs: Song[];
  playlists: Playlist[];
  currentSong: Song | null;
  isPlaying: boolean;
  onSelectSong: (song: Song) => void;
  onToggleFavorite: (songId: string, e: React.MouseEvent) => void;
  onOpenContextMenu: (song: Song, e: React.MouseEvent) => void;
  onSelectPlaylist: (playlist: Playlist) => void;
}

export const LibraryScreen: React.FC<LibraryScreenProps> = ({
  currentTab,
  songs,
  playlists,
  currentSong,
  isPlaying,
  onSelectSong,
  onToggleFavorite,
  onOpenContextMenu,
  onSelectPlaylist
}) => {
  // Filtering based on active tab
  let filteredSongs = [...songs];

  if (currentTab === 'favorites') {
    filteredSongs = songs.filter((s) => s.isFavorite);
  } else if (currentTab === 'downloads') {
    filteredSongs = songs.filter((s) => s.downloaded);
  } else if (currentTab === 'most_played') {
    filteredSongs.sort((a, b) => b.playCount - a.playCount);
  } else if (currentTab === 'recently_added') {
    filteredSongs.sort((a, b) => b.year - a.year);
  }

  // Render Playlists Grid
  if (currentTab === 'playlists') {
    return (
      <div className="p-4 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
        {playlists.map((pl) => (
          <div
            key={pl.id}
            onClick={() => onSelectPlaylist(pl)}
            className="group bg-neutral-900 border border-neutral-800/80 hover:border-orange-500/50 rounded-2xl p-4 transition-all cursor-pointer hover:bg-neutral-800/80 shadow-lg flex items-center space-x-4"
          >
            <div className="w-16 h-16 rounded-xl overflow-hidden bg-neutral-950 flex-shrink-0 border border-neutral-800">
              {pl.coverUrl ? (
                <img src={pl.coverUrl} alt={pl.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform" />
              ) : (
                <ListMusic className="w-8 h-8 text-orange-400 m-auto" />
              )}
            </div>
            <div className="min-w-0">
              <h3 className="font-bold text-sm text-white group-hover:text-orange-400 transition-colors truncate">
                {pl.name}
              </h3>
              <p className="text-xs text-neutral-400 mt-1 line-clamp-1">{pl.description}</p>
              <span className="inline-block mt-2 text-[10px] font-mono text-orange-400 font-semibold px-2 py-0.5 bg-orange-500/10 rounded border border-orange-500/20">
                {pl.songIds.length} rolitas
              </span>
            </div>
          </div>
        ))}
      </div>
    );
  }

  // Render Folders Grid
  if (currentTab === 'folders') {
    const folders = [
      { name: 'Cumbia', path: '/storage/emulated/0/Music/Cumbia', count: 4 },
      { name: 'Acústico', path: '/storage/emulated/0/Music/Acustico', count: 6 },
      { name: 'Synthwave', path: '/storage/emulated/0/Music/Synthwave', count: 8 },
      { name: 'Salsa', path: '/storage/emulated/0/Music/Salsa', count: 12 },
      { name: 'Lofi', path: '/storage/emulated/0/Music/Lofi', count: 15 }
    ];

    return (
      <div className="p-4 grid grid-cols-1 sm:grid-cols-2 gap-3">
        {folders.map((f, idx) => (
          <div
            key={idx}
            className="p-3.5 bg-neutral-900 border border-neutral-800 hover:border-orange-500/40 rounded-xl flex items-center space-x-3 cursor-pointer hover:bg-neutral-800 transition-colors"
          >
            <Folder className="w-6 h-6 text-orange-400 flex-shrink-0" />
            <div className="min-w-0">
              <h4 className="text-xs font-bold text-white truncate">{f.name}</h4>
              <p className="text-[11px] font-mono text-neutral-500 truncate">{f.path}</p>
            </div>
            <span className="text-[10px] font-mono text-neutral-400 ml-auto flex-shrink-0">
              {f.count} archivos
            </span>
          </div>
        ))}
      </div>
    );
  }

  // Default Songs list
  if (filteredSongs.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center px-4">
        <Music className="w-12 h-12 text-neutral-600 mb-3" />
        <h3 className="text-sm font-bold text-neutral-300">No hay canciones en esta categoría</h3>
        <p className="text-xs text-neutral-500 mt-1">Explora la biblioteca o cambia el filtro de búsqueda</p>
      </div>
    );
  }

  return (
    <div className="p-3 sm:p-4 space-y-2 pb-24 max-w-7xl mx-auto">
      {filteredSongs.map((song) => (
        <SongRow
          key={song.id}
          song={song}
          isCurrent={currentSong?.id === song.id}
          isPlaying={isPlaying}
          onSelect={() => onSelectSong(song)}
          onToggleFavorite={onToggleFavorite}
          onOpenContextMenu={onOpenContextMenu}
        />
      ))}
    </div>
  );
};
