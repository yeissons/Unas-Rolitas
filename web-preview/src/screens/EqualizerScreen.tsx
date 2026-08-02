import React from 'react';
import { AudioSettingsState } from '../types';
import { EQUALIZER_PRESETS } from '../data/mockData';
import { WebAudioEngine } from '../lib/audioEngine';
import {
  X,
  Sliders,
  Volume2,
  Sparkles,
  Zap,
  Activity,
  Check
} from 'lucide-react';

interface EqualizerScreenProps {
  settings: AudioSettingsState;
  onChangeSettings: (newSettings: AudioSettingsState) => void;
  onClose: () => void;
}

export const EqualizerScreen: React.FC<EqualizerScreenProps> = ({
  settings,
  onChangeSettings,
  onClose
}) => {
  const frequencies = WebAudioEngine.EQ_FREQUENCIES; // [31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000]

  const handleToggleEq = () => {
    onChangeSettings({ ...settings, eqEnabled: !settings.eqEnabled });
  };

  const handleSelectPreset = (presetName: string) => {
    const preset = EQUALIZER_PRESETS.find((p) => p.name === presetName);
    if (preset) {
      onChangeSettings({
        ...settings,
        presetName,
        bandGains: [...preset.gains]
      });
    }
  };

  const handleBandGainChange = (index: number, val: number) => {
    const newGains = [...settings.bandGains];
    newGains[index] = val;
    onChangeSettings({
      ...settings,
      presetName: 'Personalizado',
      bandGains: newGains
    });
  };

  const handleBassBoostChange = (val: number) => {
    onChangeSettings({ ...settings, bassBoost: val });
  };

  const handleVirtualizerChange = (val: number) => {
    onChangeSettings({ ...settings, virtualizer: val });
  };

  const handlePreampChange = (val: number) => {
    onChangeSettings({ ...settings, preampDb: val });
  };

  return (
    <div className="fixed inset-0 z-50 bg-neutral-950 flex flex-col overflow-y-auto animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between p-4 bg-neutral-900 border-b border-neutral-800">
        <div className="flex items-center space-x-2">
          <Sliders className="w-5 h-5 text-orange-400" />
          <h2 className="text-lg font-bold text-white">Ecualizador Gráfico & DSP</h2>
        </div>
        <div className="flex items-center space-x-3">
          {/* Main EQ Switch */}
          <button
            onClick={handleToggleEq}
            className={`px-3 py-1.5 rounded-full text-xs font-bold transition-all flex items-center space-x-1.5 ${
              settings.eqEnabled
                ? 'bg-orange-500 text-white shadow-md shadow-orange-500/30'
                : 'bg-neutral-800 text-neutral-400'
            }`}
          >
            <span>{settings.eqEnabled ? 'ACTIVADO' : 'DESACTIVADO'}</span>
          </button>
          <button
            onClick={onClose}
            className="p-1.5 text-neutral-400 hover:text-white rounded-lg hover:bg-neutral-800"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Honest Classification Badge */}
      <div className="mx-4 mt-3 p-2.5 rounded-xl bg-orange-500/10 border border-orange-500/30 text-xs text-orange-300 flex items-center space-x-2">
        <Activity className="w-4 h-4 text-orange-400 flex-shrink-0 animate-pulse" />
        <span>
          <strong>UI COMPLETA + LÓGICA REAL (WebAudio API)</strong> — En Android se vincula a
          <code className="text-amber-400 font-mono ml-1">android.media.audiofx.Equalizer</code> & Media3.
        </span>
      </div>

      <div className="p-4 space-y-6 max-w-4xl mx-auto w-full">
        {/* Presets Horizontal Bar */}
        <div>
          <label className="text-xs font-bold text-neutral-400 uppercase tracking-wider block mb-2">
            Perfiles & Presets de Audio
          </label>
          <div className="flex space-x-2 overflow-x-auto no-scrollbar py-1">
            {EQUALIZER_PRESETS.map((preset) => (
              <button
                key={preset.name}
                onClick={() => handleSelectPreset(preset.name)}
                className={`px-3 py-1.5 rounded-xl whitespace-nowrap text-xs font-semibold transition-all ${
                  settings.presetName === preset.name
                    ? 'bg-gradient-to-r from-orange-600 to-amber-500 text-white shadow-md'
                    : 'bg-neutral-900 border border-neutral-800 text-neutral-300 hover:bg-neutral-800'
                }`}
              >
                {preset.name}
              </button>
            ))}
          </div>
        </div>

        {/* 10-Band Graphic Equalizer Sliders */}
        <div className="bg-neutral-900/80 border border-neutral-800/80 rounded-2xl p-4 sm:p-6 shadow-xl">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-bold text-neutral-300 uppercase tracking-wider">
              Bajas Frecuencias (Graves) ↔ Altas (Agudos)
            </span>
            <span className="text-xs text-orange-400 font-mono font-semibold">
              Rango: -12dB a +12dB
            </span>
          </div>

          <div className="grid grid-cols-5 sm:grid-cols-10 gap-2 sm:gap-3 items-end h-64 sm:h-72 pt-4 pb-2">
            {frequencies.map((freq, idx) => {
              const gain = settings.bandGains[idx] || 0;
              const formattedFreq = freq >= 1000 ? `${freq / 1000}k` : `${freq}`;

              return (
                <div key={freq} className="flex flex-col items-center justify-between h-full group">
                  {/* Gain Value */}
                  <span className="text-[10px] font-mono font-semibold text-orange-400 mb-1">
                    {gain > 0 ? `+${gain}` : gain}dB
                  </span>

                  {/* Vertical Slider */}
                  <div className="relative flex-1 flex items-center justify-center w-full">
                    <input
                      type="range"
                      min={-12}
                      max={12}
                      step={0.5}
                      value={gain}
                      disabled={!settings.eqEnabled}
                      onChange={(e) => handleBandGainChange(idx, parseFloat(e.target.value))}
                      className="h-full w-2 bg-neutral-800 rounded-lg appearance-none cursor-pointer accent-orange-500 writing-vertical-slider"
                      style={{
                        writingMode: 'vertical-lr',
                        direction: 'rtl'
                      }}
                    />
                  </div>

                  {/* Frequency Label */}
                  <span className="text-[10px] font-bold text-neutral-400 mt-2">
                    {formattedFreq}Hz
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        {/* DSP Effects Controls: Bass Boost, Virtualizer 3D, Preamp */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {/* Bass Boost */}
          <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-4 flex flex-col justify-between">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-white flex items-center gap-1.5">
                <Zap className="w-4 h-4 text-orange-500" />
                Bass Boost (Refuerzo de Graves)
              </span>
              <span className="text-xs font-mono text-orange-400 font-bold">{settings.bassBoost}%</span>
            </div>
            <input
              type="range"
              min={0}
              max={100}
              value={settings.bassBoost}
              onChange={(e) => handleBassBoostChange(parseInt(e.target.value))}
              className="w-full accent-orange-500 my-2 cursor-pointer"
            />
            <span className="text-[10px] text-neutral-500">Aumenta la potencia de sub-graves</span>
          </div>

          {/* Virtualizer 3D Surround */}
          <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-4 flex flex-col justify-between">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-white flex items-center gap-1.5">
                <Sparkles className="w-4 h-4 text-amber-500" />
                Virtualizador 3D Surround
              </span>
              <span className="text-xs font-mono text-amber-400 font-bold">{settings.virtualizer}%</span>
            </div>
            <input
              type="range"
              min={0}
              max={100}
              value={settings.virtualizer}
              onChange={(e) => handleVirtualizerChange(parseInt(e.target.value))}
              className="w-full accent-amber-500 my-2 cursor-pointer"
            />
            <span className="text-[10px] text-neutral-500">Simulación de sonido envolvente para audífonos</span>
          </div>

          {/* Preamp */}
          <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-4 flex flex-col justify-between">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-white flex items-center gap-1.5">
                <Volume2 className="w-4 h-4 text-orange-400" />
                Preamplificador (Preamp)
              </span>
              <span className="text-xs font-mono text-orange-400 font-bold">{settings.preampDb} dB</span>
            </div>
            <input
              type="range"
              min={-6}
              max={6}
              step={0.5}
              value={settings.preampDb}
              onChange={(e) => handlePreampChange(parseFloat(e.target.value))}
              className="w-full accent-orange-500 my-2 cursor-pointer"
            />
            <span className="text-[10px] text-neutral-500">Ganancia previa al procesamiento DSP</span>
          </div>
        </div>
      </div>
    </div>
  );
};
