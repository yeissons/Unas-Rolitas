import React, { useState, useEffect } from 'react';
import { Song, Playlist, TabType, SortField, SortOrder, AudioSettingsState, ViewMode } from './types';
import { INITIAL_SONGS, INITIAL_PLAYLISTS, EQUALIZER_PRESETS } from './data/mockData';
import { audioEngine } from './lib/audioEngine';
import { HeaderBar } from './components/HeaderBar';
import { BottomDockPlayer } from './components/BottomDockPlayer';
import { ContextMenuModal } from './components/ContextMenuModal';
import { LibraryScreen } from './screens/LibraryScreen';
import { PlayerScreen } from './screens/PlayerScreen';
import { QueueScreen } from './screens/QueueScreen';
import { LyricsScreen } from './screens/LyricsScreen';
import { EqualizerScreen } from './screens/EqualizerScreen';
import { AudioToolsScreen } from './screens/AudioToolsScreen';
import { ControlCenterScreen } from './screens/ControlCenterScreen';
import { AndroidStatusModal } from './screens/AndroidStatusModal';

export default function App() {
  // State
  const [songs, setSongs] = useState<Song[]>(INITIAL_SONGS);
  const [playlists, setPlaylists] = useState<Playlist[]>(INITIAL_PLAYLISTS);
  const [currentTab, setCurrentTab] = useState<TabType>('songs');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [sortField, setSortField] = useState<SortField>('title');
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc');

  // Playback state
  const [queue, setQueue] = useState<Song[]>(INITIAL_SONGS);
  const [currentSongIndex, setCurrentSongIndex] = useState<number>(0);
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [repeatMode, setRepeatMode] = useState<'off' | 'all' | 'one'>('off');
  const [isShuffle, setIsShuffle] = useState<boolean>(false);
  const [sleepTimerMins, setSleepTimerMins] = useState<number | null>(null);

  // Audio DSP Settings
  const [audioSettings, setAudioSettings] = useState<AudioSettingsState>({
    eqEnabled: true,
    presetName: 'Salsa & Cumbia',
    bandGains: [4, 2, 0, 1, 3, 4, 5, 4, 3, 2],
    preampDb: 0,
    bassBoost: 35,
    virtualizer: 20,
    reverbPreset: 'None',
    playbackSpeed: 1.0,
    pitchPreserved: true,
    crossfadeDurationSec: 2,
    gaplessPlayback: true,
    replayGain: false,
    volume: 0.85
  });

  // View Modals / Screens
  const [currentView, setCurrentView] = useState<ViewMode>('library');
  const [contextSong, setContextSong] = useState<Song | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const currentSong = queue[currentSongIndex] || songs[0] || null;

  // Sync audio DSP settings to Web Audio Engine
  useEffect(() => {
    audioEngine.updateAudioSettings(audioSettings);
  }, [audioSettings]);

  // Playback timer & synth update
  useEffect(() => {
    let timer: number;
    if (isPlaying && currentSong) {
      audioEngine.startSynthesis(currentSong.id);
      timer = window.setInterval(() => {
        setCurrentTime((t) => {
          if (t >= currentSong.duration) {
            handleNextSong();
            return 0;
          }
          return t + 1;
        });
      }, 1000);
    } else {
      audioEngine.stopSynthesis();
    }

    return () => {
      clearInterval(timer);
    };
  }, [isPlaying, currentSongIndex, currentSong]);

  // Toast notification helper
  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage(null);
    }, 3000);
  };

  // Playback Handlers
  const handleTogglePlay = () => {
    setIsPlaying((prev) => !prev);
  };

  const handleNextSong = () => {
    if (repeatMode === 'one') {
      setCurrentTime(0);
      return;
    }
    if (isShuffle) {
      const nextIdx = Math.floor(Math.random() * queue.length);
      setCurrentSongIndex(nextIdx);
    } else {
      setCurrentSongIndex((prev) => (prev + 1) % queue.length);
    }
    setCurrentTime(0);
  };

  const handlePrevSong = () => {
    if (currentTime > 3) {
      setCurrentTime(0);
      return;
    }
    setCurrentSongIndex((prev) => (prev - 1 + queue.length) % queue.length);
    setCurrentTime(0);
  };

  const handleSelectSong = (song: Song) => {
    const idx = queue.findIndex((s) => s.id === song.id);
    if (idx !== -1) {
      setCurrentSongIndex(idx);
    } else {
      setQueue([song, ...queue]);
      setCurrentSongIndex(0);
    }
    setCurrentTime(0);
    setIsPlaying(true);
    setCurrentView('player');
  };

  const handleToggleFavorite = (songId: string, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    setSongs((prev) =>
      prev.map((s) => (s.id === songId ? { ...s, isFavorite: !s.isFavorite } : s))
    );
    setQueue((prev) =>
      prev.map((s) => (s.id === songId ? { ...s, isFavorite: !s.isFavorite } : s))
    );
  };

  const handleShuffleAll = () => {
    const shuffled = [...songs].sort(() => Math.random() - 0.5);
    setQueue(shuffled);
    setCurrentSongIndex(0);
    setCurrentTime(0);
    setIsPlaying(true);
    showToast('Reproduciendo todas las rolitas en aleatorio');
  };

  // Search & Filter
  const searchedSongs = songs.filter((s) => {
    const q = searchQuery.toLowerCase();
    return (
      s.title.toLowerCase().includes(q) ||
      s.artist.toLowerCase().includes(q) ||
      s.album.toLowerCase().includes(q)
    );
  });

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col font-sans select-none antialiased">
      {/* Toast Popup */}
      {toastMessage && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 px-4 py-2 bg-gradient-to-r from-orange-600 to-amber-500 text-white font-bold text-xs rounded-full shadow-2xl animate-fade-in border border-orange-400">
          {toastMessage}
        </div>
      )}

      {/* Main Header Bar */}
      <HeaderBar
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        sortField={sortField}
        sortOrder={sortOrder}
        onSortChange={setSortField}
        onShuffleAll={handleShuffleAll}
        currentView={currentView}
        onNavigateView={setCurrentView}
        onOpenDrawer={() => setCurrentView('settings')}
      />

      {/* Main Content Area */}
      <main className="flex-1 pb-20">
        {currentView === 'library' && (
          <LibraryScreen
            currentTab={currentTab}
            songs={searchedSongs}
            playlists={playlists}
            currentSong={currentSong}
            isPlaying={isPlaying}
            onSelectSong={handleSelectSong}
            onToggleFavorite={handleToggleFavorite}
            onOpenContextMenu={(song, e) => {
              e.stopPropagation();
              setContextSong(song);
            }}
            onSelectPlaylist={(pl) => {
              const plSongs = songs.filter((s) => pl.songIds.includes(s.id));
              if (plSongs.length > 0) {
                setQueue(plSongs);
                setCurrentSongIndex(0);
                setIsPlaying(true);
                showToast(`Lista "${pl.name}" cargada`);
              }
            }}
          />
        )}
      </main>

      {/* Persistent Bottom Player Dock */}
      {currentView === 'library' && currentSong && (
        <BottomDockPlayer
          currentSong={currentSong}
          isPlaying={isPlaying}
          currentTime={currentTime}
          onTogglePlay={handleTogglePlay}
          onNext={handleNextSong}
          onOpenPlayer={() => setCurrentView('player')}
          onOpenQueue={() => setCurrentView('queue')}
          onOpenLyrics={() => setCurrentView('lyrics')}
        />
      )}

      {/* Full Screen Player */}
      {currentView === 'player' && (
        <PlayerScreen
          song={currentSong}
          isPlaying={isPlaying}
          currentTime={currentTime}
          repeatMode={repeatMode}
          isShuffle={isShuffle}
          playbackSpeed={audioSettings.playbackSpeed}
          sleepTimerMins={sleepTimerMins}
          onClose={() => setCurrentView('library')}
          onTogglePlay={handleTogglePlay}
          onNext={handleNextSong}
          onPrev={handlePrevSong}
          onSeek={(sec) => setCurrentTime(sec)}
          onToggleRepeat={() =>
            setRepeatMode((r) => (r === 'off' ? 'all' : r === 'all' ? 'one' : 'off'))
          }
          onToggleShuffle={() => setIsShuffle(!isShuffle)}
          onToggleFavorite={handleToggleFavorite}
          onChangeSpeed={(s) => setAudioSettings({ ...audioSettings, playbackSpeed: s })}
          onSetSleepTimer={setSleepTimerMins}
          onOpenQueue={() => setCurrentView('queue')}
          onOpenLyrics={() => setCurrentView('lyrics')}
          onOpenEqualizer={() => setCurrentView('equalizer')}
        />
      )}

      {/* Queue Drawer */}
      {currentView === 'queue' && (
        <QueueScreen
          queue={queue}
          currentSongIndex={currentSongIndex}
          isPlaying={isPlaying}
          onClose={() => setCurrentView('library')}
          onSelectSongIndex={(idx) => {
            setCurrentSongIndex(idx);
            setCurrentTime(0);
            setIsPlaying(true);
          }}
          onRemoveFromQueue={(idx) => {
            const newQ = [...queue];
            newQ.splice(idx, 1);
            setQueue(newQ);
            showToast('Canción quitada de la cola');
          }}
          onClearQueue={() => {
            setQueue([]);
            setIsPlaying(false);
            showToast('Cola vaciada');
          }}
          onShuffleQueue={() => {
            setQueue([...queue].sort(() => Math.random() - 0.5));
            showToast('Cola aleatorizada');
          }}
          onSaveAsPlaylist={() => {
            showToast('Cola guardada como lista de reproducción');
          }}
        />
      )}

      {/* Lyrics Screen */}
      {currentView === 'lyrics' && (
        <LyricsScreen
          song={currentSong}
          currentTime={currentTime}
          onClose={() => setCurrentView('library')}
          onActionToast={showToast}
        />
      )}

      {/* Equalizer & DSP */}
      {currentView === 'equalizer' && (
        <EqualizerScreen
          settings={audioSettings}
          onChangeSettings={setAudioSettings}
          onClose={() => setCurrentView('library')}
        />
      )}

      {/* Audio Tools */}
      {currentView === 'audiotools' && (
        <AudioToolsScreen
          songs={songs}
          onClose={() => setCurrentView('library')}
          onActionToast={showToast}
        />
      )}

      {/* Control Center & Settings */}
      {currentView === 'settings' && (
        <ControlCenterScreen
          settings={audioSettings}
          onClose={() => setCurrentView('library')}
          onActionToast={showToast}
        />
      )}

      {/* Native Android Project Audit Modal */}
      {currentView === 'android-info' && (
        <AndroidStatusModal onClose={() => setCurrentView('library')} />
      )}

      {/* Context Menu Modal */}
      {contextSong && (
        <ContextMenuModal
          song={contextSong}
          onClose={() => setContextSong(null)}
          onPlayNow={(song) => handleSelectSong(song)}
          onPlayNext={(song) => {
            const newQ = [...queue];
            newQ.splice(currentSongIndex + 1, 0, song);
            setQueue(newQ);
          }}
          onAddToQueue={(song) => setQueue([...queue, song])}
          onToggleFavorite={handleToggleFavorite}
          onActionToast={showToast}
        />
      )}
    </div>
  );
}
