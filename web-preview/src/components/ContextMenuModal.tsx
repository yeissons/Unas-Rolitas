import React from 'react';
import { Song } from '../types';
import {
  Play,
  ListPlus,
  PlaySquare,
  Shuffle,
  Heart,
  PlusCircle,
  Edit3,
  Image,
  FileText,
  Info,
  FolderOpen,
  Trash2,
  Scissors,
  Repeat,
  Volume2,
  Share2,
  Bell,
  Ban,
  X
} from 'lucide-react';

interface ContextMenuModalProps {
  song: Song | null;
  onClose: () => void;
  onPlayNow: (song: Song) => void;
  onPlayNext: (song: Song) => void;
  onAddToQueue: (song: Song) => void;
  onToggleFavorite: (songId: string) => void;
  onActionToast: (message: string) => void;
}

export const ContextMenuModal: React.FC<ContextMenuModalProps> = ({
  song,
  onClose,
  onPlayNow,
  onPlayNext,
  onAddToQueue,
  onToggleFavorite,
  onActionToast
}) => {
  if (!song) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/70 backdrop-blur-sm p-0 sm:p-4 animate-fade-in">
      <div className="w-full max-w-md bg-neutral-900 border border-neutral-800 rounded-t-2xl sm:rounded-2xl shadow-2xl overflow-hidden max-h-[85vh] flex flex-col">
        {/* Header */}
        <div className="p-4 bg-neutral-950 border-b border-neutral-800 flex items-center justify-between">
          <div className="flex items-center space-x-3 min-w-0">
            <img
              src={song.coverUrl}
              alt={song.title}
              className="w-12 h-12 rounded-lg object-cover border border-neutral-800"
              referrerPolicy="no-referrer"
            />
            <div className="min-w-0">
              <h3 className="text-sm font-bold text-white truncate">{song.title}</h3>
              <p className="text-xs text-neutral-400 truncate">{song.artist} • {song.album}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 text-neutral-400 hover:text-white rounded-full hover:bg-neutral-800"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Categorized Options */}
        <div className="p-3 overflow-y-auto space-y-4 text-xs">
          {/* CATEGORÍA 1: REPRODUCCIÓN */}
          <div>
            <span className="text-[10px] font-bold tracking-wider text-orange-500 uppercase px-2">
              1. Reproducción
            </span>
            <div className="mt-1 space-y-0.5">
              <button
                onClick={() => { onPlayNow(song); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Play className="w-4 h-4 text-orange-400" />
                <span>Reproducir ahora</span>
              </button>
              <button
                onClick={() => { onPlayNext(song); onActionToast('Se reproducirá a continuación'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <PlaySquare className="w-4 h-4 text-neutral-400" />
                <span>Reproducir a continuación</span>
              </button>
              <button
                onClick={() => { onAddToQueue(song); onActionToast('Añadida a la cola'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <ListPlus className="w-4 h-4 text-neutral-400" />
                <span>Agregar a la cola</span>
              </button>
              <button
                onClick={() => { onActionToast('Iniciando radio de esta canción...'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Shuffle className="w-4 h-4 text-neutral-400" />
                <span>Iniciar radio aleatoria de la canción</span>
              </button>
            </div>
          </div>

          {/* CATEGORÍA 2: BIBLIOTECA */}
          <div>
            <span className="text-[10px] font-bold tracking-wider text-orange-500 uppercase px-2">
              2. Biblioteca
            </span>
            <div className="mt-1 space-y-0.5">
              <button
                onClick={() => { onToggleFavorite(song.id); onActionToast('Estado de favorito actualizado'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Heart className={`w-4 h-4 ${song.isFavorite ? 'fill-red-500 text-red-500' : 'text-neutral-400'}`} />
                <span>{song.isFavorite ? 'Quitar de Favoritos' : 'Agregar a Favoritos'}</span>
              </button>
              <button
                onClick={() => { onActionToast('Añadida a la lista de reproducción'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <PlusCircle className="w-4 h-4 text-neutral-400" />
                <span>Agregar a lista de reproducción...</span>
              </button>
            </div>
          </div>

          {/* CATEGORÍA 3: EDITAR INFORMACIÓN */}
          <div>
            <span className="text-[10px] font-bold tracking-wider text-orange-500 uppercase px-2">
              3. Editar información
            </span>
            <div className="mt-1 space-y-0.5">
              <button
                onClick={() => { onActionToast('Editor de etiquetas ID3 abierto'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Edit3 className="w-4 h-4 text-neutral-400" />
                <span>Editar etiquetas ID3 (Título/Artista/Álbum)</span>
              </button>
              <button
                onClick={() => { onActionToast('Selector de carátula abierto'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Image className="w-4 h-4 text-neutral-400" />
                <span>Cambiar carátula de álbum</span>
              </button>
              <button
                onClick={() => { onActionToast('Buscador de letras activado'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <FileText className="w-4 h-4 text-neutral-400" />
                <span>Buscar / Vincular archivo de letras .LRC</span>
              </button>
            </div>
          </div>

          {/* CATEGORÍA 4: ARCHIVO */}
          <div>
            <span className="text-[10px] font-bold tracking-wider text-orange-500 uppercase px-2">
              4. Archivo
            </span>
            <div className="mt-1 space-y-0.5">
              <button
                onClick={() => { alert(`Ruta: ${song.filePath}\nFormato: ${song.codec} (${song.bitrate})\nTamaño: ${song.fileSize}`); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Info className="w-4 h-4 text-neutral-400" />
                <span>Ver detalles del archivo audio</span>
              </button>
              <button
                onClick={() => { onActionToast('Ubicación de archivo: ' + song.filePath); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <FolderOpen className="w-4 h-4 text-neutral-400" />
                <span>Abrir carpeta contenedora</span>
              </button>
              <button
                onClick={() => { onActionToast('Solicitud de eliminación enviada'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-red-400 hover:text-red-300"
              >
                <Trash2 className="w-4 h-4 text-red-400" />
                <span>Eliminar del almacenamiento</span>
              </button>
            </div>
          </div>

          {/* CATEGORÍA 5: AUDIO & HERRAMIENTAS */}
          <div>
            <span className="text-[10px] font-bold tracking-wider text-orange-500 uppercase px-2">
              5. Audio
            </span>
            <div className="mt-1 space-y-0.5">
              <button
                onClick={() => { onActionToast('Abriendo Recortador de Audio...'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Scissors className="w-4 h-4 text-neutral-400" />
                <span>Recortar / Cortar audio</span>
              </button>
              <button
                onClick={() => { onActionToast('Abriendo Conversor de Formatos...'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Repeat className="w-4 h-4 text-neutral-400" />
                <span>Convertir formato (MP3, FLAC, AAC, WAV)</span>
              </button>
              <button
                onClick={() => { onActionToast('Normalización ReplayGain aplicada'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Volume2 className="w-4 h-4 text-neutral-400" />
                <span>Normalizar volumen ReplayGain</span>
              </button>
            </div>
          </div>

          {/* CATEGORÍA 6: MÁS OPCIONES */}
          <div>
            <span className="text-[10px] font-bold tracking-wider text-orange-500 uppercase px-2">
              6. Más opciones
            </span>
            <div className="mt-1 space-y-0.5">
              <button
                onClick={() => { onActionToast('Enlace de canción copiado al portapapeles'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Share2 className="w-4 h-4 text-neutral-400" />
                <span>Compartir canción</span>
              </button>
              <button
                onClick={() => { onActionToast('Establecida como tono de llamada del sistema'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Bell className="w-4 h-4 text-neutral-400" />
                <span>Establecer como tono de llamada</span>
              </button>
              <button
                onClick={() => { onActionToast('Carpeta añadida a lista negra de escaneo'); onClose(); }}
                className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg hover:bg-neutral-800 text-neutral-200 hover:text-white"
              >
                <Ban className="w-4 h-4 text-neutral-400" />
                <span>Excluir esta carpeta del escáner</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
