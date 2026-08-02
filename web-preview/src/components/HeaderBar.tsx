import React from 'react';
import { TabType, SortField, SortOrder, ViewMode } from '../types';
import {
  Search,
  Menu,
  Shuffle,
  ArrowUpDown,
  Music2,
  Disc3,
  Sliders,
  Wrench,
  Settings,
  Smartphone,
  ListMusic
} from 'lucide-react';

interface HeaderBarProps {
  currentTab: TabType;
  onSelectTab: (tab: TabType) => void;
  searchQuery: string;
  onSearchChange: (q: string) => void;
  sortField: SortField;
  sortOrder: SortOrder;
  onSortChange: (field: SortField) => void;
  onShuffleAll: () => void;
  currentView: ViewMode;
  onNavigateView: (view: ViewMode) => void;
  onOpenDrawer: () => void;
}

export const TABS: { id: TabType; label: string }[] = [
  { id: 'songs', label: 'Canciones' },
  { id: 'playlists', label: 'Listas' },
  { id: 'folders', label: 'Carpetas' },
  { id: 'albums', label: 'Álbumes' },
  { id: 'artists', label: 'Artistas' },
  { id: 'genres', label: 'Géneros' },
  { id: 'favorites', label: 'Favoritos' },
  { id: 'most_played', label: 'Más reproducidas' },
  { id: 'recently_added', label: 'Recientes' },
  { id: 'history', label: 'Historial' },
  { id: 'downloads', label: 'Descargadas' },
  { id: 'podcasts', label: 'Podcasts' },
  { id: 'audiobooks', label: 'Audiolibros' },
];

export const HeaderBar: React.FC<HeaderBarProps> = ({
  currentTab,
  onSelectTab,
  searchQuery,
  onSearchChange,
  sortField,
  sortOrder,
  onSortChange,
  onShuffleAll,
  currentView,
  onNavigateView,
  onOpenDrawer
}) => {
  return (
    <header className="sticky top-0 z-30 bg-neutral-950/95 backdrop-blur-md border-b border-neutral-800/80">
      {/* Top Bar */}
      <div className="flex items-center justify-between px-3 sm:px-4 py-2.5">
        {/* Left: Hamburger & Logo */}
        <div className="flex items-center space-x-2.5">
          <button
            onClick={onOpenDrawer}
            className="p-2 text-neutral-300 hover:text-white rounded-lg hover:bg-neutral-900 transition-colors"
            title="Menú"
          >
            <Menu className="w-5 h-5" />
          </button>
          <div
            onClick={() => onNavigateView('library')}
            className="flex items-center space-x-2 cursor-pointer group"
          >
            <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-orange-600 to-amber-500 flex items-center justify-center shadow-md shadow-orange-500/20 group-hover:scale-105 transition-transform">
              <Disc3 className="w-5 h-5 text-white animate-spin-slow" />
            </div>
            <span className="font-bold text-base tracking-tight text-white group-hover:text-orange-400 transition-colors">
              ¿Unas Rolitas?
            </span>
          </div>
        </div>

        {/* Center: Search input */}
        <div className="flex-1 max-w-xs sm:max-w-sm mx-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-2.5 w-4 h-4 text-neutral-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
              placeholder="Buscar canción, artista o álbum..."
              className="w-full bg-neutral-900 border border-neutral-800 rounded-full pl-8 pr-3 py-1.5 text-xs text-white placeholder-neutral-500 focus:outline-none focus:border-orange-500/60 transition-colors"
            />
          </div>
        </div>

        {/* Right: Quick View Shortcut Buttons */}
        <div className="flex items-center space-x-1 sm:space-x-1.5">
          <button
            onClick={() => onNavigateView('equalizer')}
            className={`p-2 rounded-lg transition-colors ${
              currentView === 'equalizer'
                ? 'bg-orange-500/20 text-orange-400 border border-orange-500/30'
                : 'text-neutral-400 hover:text-white hover:bg-neutral-900'
            }`}
            title="Ecualizador DSP"
          >
            <Sliders className="w-4 h-4" />
          </button>
          <button
            onClick={() => onNavigateView('audiotools')}
            className={`p-2 rounded-lg transition-colors ${
              currentView === 'audiotools'
                ? 'bg-orange-500/20 text-orange-400 border border-orange-500/30'
                : 'text-neutral-400 hover:text-white hover:bg-neutral-900'
            }`}
            title="Herramientas Audio"
          >
            <Wrench className="w-4 h-4" />
          </button>
          <button
            onClick={() => onNavigateView('settings')}
            className={`p-2 rounded-lg transition-colors ${
              currentView === 'settings'
                ? 'bg-orange-500/20 text-orange-400 border border-orange-500/30'
                : 'text-neutral-400 hover:text-white hover:bg-neutral-900'
            }`}
            title="Ajustes"
          >
            <Settings className="w-4 h-4" />
          </button>
          <button
            onClick={() => onNavigateView('android-info')}
            className={`p-2 rounded-lg transition-colors ${
              currentView === 'android-info'
                ? 'bg-green-500/20 text-green-400 border border-green-500/30'
                : 'text-neutral-400 hover:text-white hover:bg-neutral-900'
            }`}
            title="Estado Android Nativo"
          >
            <Smartphone className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Tabs & Controls row (visible on Library View) */}
      {currentView === 'library' && (
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between border-t border-neutral-900 px-3 py-1.5 gap-2">
          {/* Scrollable Tabs */}
          <div className="flex items-center space-x-1 overflow-x-auto no-scrollbar py-0.5 text-xs">
            {TABS.map((tab) => (
              <button
                key={tab.id}
                onClick={() => onSelectTab(tab.id)}
                className={`px-3 py-1.5 rounded-full whitespace-nowrap transition-all font-medium ${
                  currentTab === tab.id
                    ? 'bg-orange-600 text-white shadow-sm shadow-orange-600/30 font-semibold'
                    : 'bg-neutral-900/80 text-neutral-400 hover:text-white hover:bg-neutral-800'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* Action Row: Sort & Shuffle All */}
          <div className="flex items-center justify-end space-x-2 flex-shrink-0">
            {/* Sort selector */}
            <div className="flex items-center space-x-1 bg-neutral-900 border border-neutral-800 rounded-lg px-2 py-1 text-xs text-neutral-300">
              <ArrowUpDown className="w-3.5 h-3.5 text-orange-400" />
              <select
                value={sortField}
                onChange={(e) => onSortChange(e.target.value as SortField)}
                className="bg-transparent text-xs text-neutral-200 border-none focus:outline-none cursor-pointer"
              >
                <option value="title" className="bg-neutral-900">Nombre {sortOrder === 'asc' ? 'A-Z' : 'Z-A'}</option>
                <option value="artist" className="bg-neutral-900">Artista</option>
                <option value="album" className="bg-neutral-900">Álbum</option>
                <option value="duration" className="bg-neutral-900">Duración</option>
                <option value="playCount" className="bg-neutral-900">Más reproducidas</option>
                <option value="fileSize" className="bg-neutral-900">Tamaño</option>
              </select>
            </div>

            {/* Shuffle All */}
            <button
              onClick={onShuffleAll}
              className="flex items-center space-x-1.5 px-3 py-1 rounded-lg bg-orange-500/20 text-orange-400 hover:bg-orange-500/30 border border-orange-500/30 text-xs font-semibold transition-colors"
            >
              <Shuffle className="w-3.5 h-3.5" />
              <span>Aleatorio</span>
            </button>
          </div>
        </div>
      )}
    </header>
  );
};
