import React from 'react';
import { AudioSettingsState } from '../types';
import {
  X,
  Database,
  PlayCircle,
  Sliders,
  Palette,
  Eye,
  FileText,
  Tag,
  Wrench,
  ShieldCheck,
  Cpu,
  Info,
  ChevronRight,
  CheckCircle,
  HardDrive
} from 'lucide-react';

interface ControlCenterScreenProps {
  settings: AudioSettingsState;
  onClose: () => void;
  onActionToast: (msg: string) => void;
}

export const ControlCenterScreen: React.FC<ControlCenterScreenProps> = ({
  settings,
  onClose,
  onActionToast
}) => {
  const cards = [
    {
      id: 'library',
      icon: Database,
      title: '1. Biblioteca & Escáner',
      description: 'Gestión de rutas MediaStore, listas negras y carpetas excluidas',
      status: 'Escaneo automático activo (SDK 34)',
      color: 'text-orange-400'
    },
    {
      id: 'playback',
      icon: PlayCircle,
      title: '2. Reproducción & Fundido',
      description: 'Crossfade de canciones, reproducción sin pausas (Gapless) y buffer',
      status: `Sin pausas: ${settings.gaplessPlayback ? 'SÍ' : 'NO'} | Vel: ${settings.playbackSpeed}x`,
      color: 'text-amber-400'
    },
    {
      id: 'audio',
      icon: Sliders,
      title: '3. Audio & DSP',
      description: 'Ecualizador de 10 bandas, ReplayGain, BassBoost y Surround 3D',
      status: `EQ: ${settings.eqEnabled ? settings.presetName : 'Apagado'} | Bass: ${settings.bassBoost}%`,
      color: 'text-orange-500'
    },
    {
      id: 'appearance',
      icon: Palette,
      title: '4. Apariencia & Tema',
      description: 'Tema oscuro "Noche de Vinilo", fuentes dinámicas y acento de color',
      status: 'Modo Oscuro OLED Premium',
      color: 'text-amber-500'
    },
    {
      id: 'visualizer',
      icon: Eye,
      title: '5. Visualizadores de Audio',
      description: 'Sensibilidad de renderizado por Canvas, reactividad y FPS',
      status: '60 FPS / FFT Size 64',
      color: 'text-orange-400'
    },
    {
      id: 'lyrics',
      icon: FileText,
      title: '6. Letras & Karaoke',
      description: 'Proveedor de letras LRC, ajuste de compensación y fuentes',
      status: 'LRCLIB / Musixmatch Sync',
      color: 'text-amber-400'
    },
    {
      id: 'metadata',
      icon: Tag,
      title: '7. Metadatos & Etiquetas',
      description: 'Editor de etiquetas ID3 v2.4, descarga de carátulas en HD',
      status: 'Motor TagLib Nativo',
      color: 'text-orange-500'
    },
    {
      id: 'tools',
      icon: Wrench,
      title: '8. Herramientas de Audio',
      description: 'Conversor por lotes, recortador de tonos y extractor',
      status: 'FFmpeg / MediaCodec Pro',
      color: 'text-amber-500'
    },
    {
      id: 'backup',
      icon: ShieldCheck,
      title: '9. Copias de Seguridad',
      description: 'Exportar e importar listas de reproducción, ecualizadores y ajustes',
      status: 'Respaldado localmente',
      color: 'text-green-400'
    },
    {
      id: 'advanced',
      icon: Cpu,
      title: '10. Avanzado & MediaSession',
      description: 'Integración Bluetooth, controles de auriculares y WakeLock',
      status: 'Media3 Service Conectado',
      color: 'text-orange-400'
    },
    {
      id: 'about',
      icon: Info,
      title: '11. Acerca de «¿Unas Rolitas?»',
      description: 'Versión 1.0.0 Nativa Android | Creado con Kotlin & Jetpack Compose',
      status: 'Build assembleDebug Ok',
      color: 'text-amber-400'
    }
  ];

  return (
    <div className="fixed inset-0 z-50 bg-neutral-950 flex flex-col overflow-y-auto animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between p-4 bg-neutral-900 border-b border-neutral-800">
        <div>
          <h2 className="text-lg font-bold text-white">Centro de Control & Ajustes Inteligentes</h2>
          <p className="text-xs text-neutral-400">¿Unas Rolitas? — Configuración del Sistema</p>
        </div>
        <button
          onClick={onClose}
          className="p-1.5 text-neutral-400 hover:text-white rounded-lg hover:bg-neutral-800"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      {/* Grid of Smart Setting Cards */}
      <div className="p-4 max-w-4xl mx-auto w-full grid grid-cols-1 md:grid-cols-2 gap-3.5">
        {cards.map((card) => {
          const Icon = card.icon;
          return (
            <div
              key={card.id}
              onClick={() => onActionToast(`Ajuste "${card.title}" configurado`)}
              className="bg-neutral-900/90 border border-neutral-800/90 hover:border-orange-500/50 rounded-2xl p-4 flex items-center justify-between transition-all cursor-pointer hover:bg-neutral-800/80 group shadow-md"
            >
              <div className="flex items-start space-x-3.5 min-w-0">
                <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800 flex-shrink-0 group-hover:scale-110 transition-transform">
                  <Icon className={`w-5 h-5 ${card.color}`} />
                </div>
                <div className="min-w-0">
                  <h3 className="text-sm font-bold text-white group-hover:text-orange-400 transition-colors truncate">
                    {card.title}
                  </h3>
                  <p className="text-xs text-neutral-400 mt-0.5 line-clamp-1">{card.description}</p>
                  <div className="mt-2 flex items-center space-x-1.5">
                    <CheckCircle className="w-3.5 h-3.5 text-green-400 flex-shrink-0" />
                    <span className="text-[11px] font-mono text-neutral-300 truncate">
                      {card.status}
                    </span>
                  </div>
                </div>
              </div>
              <ChevronRight className="w-5 h-5 text-neutral-600 group-hover:text-white group-hover:translate-x-1 transition-all flex-shrink-0 ml-2" />
            </div>
          );
        })}
      </div>
    </div>
  );
};
