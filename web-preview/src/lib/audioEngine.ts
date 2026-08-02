import { AudioSettingsState } from '../types';

export class WebAudioEngine {
  private ctx: AudioContext | null = null;
  private masterGain: GainNode | null = null;
  private eqFilters: BiquadFilterNode[] = [];
  private bassFilter: BiquadFilterNode | null = null;
  private analyserNode: AnalyserNode | null = null;
  private isPlaying: boolean = false;
  private isSynthesizing: boolean = false;
  private animationTimer: number | null = null;
  private currentFreq: number = 220;

  // Frequencies for 10-band EQ
  public static readonly EQ_FREQUENCIES = [31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000];

  public init() {
    if (this.ctx) return;
    try {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      this.ctx = new AudioCtx();

      this.masterGain = this.ctx.createGain();
      this.analyserNode = this.ctx.createAnalyser();
      this.analyserNode.fftSize = 64;

      // Create 10 biquad filters for EQ
      this.eqFilters = WebAudioEngine.EQ_FREQUENCIES.map((freq) => {
        const filter = this.ctx!.createBiquadFilter();
        filter.type = 'peaking';
        filter.frequency.value = freq;
        filter.Q.value = 1.4;
        filter.gain.value = 0;
        return filter;
      });

      // Create Bass Boost filter (lowshelf)
      this.bassFilter = this.ctx.createBiquadFilter();
      this.bassFilter.type = 'lowshelf';
      this.bassFilter.frequency.value = 150;
      this.bassFilter.gain.value = 0;

      // Chain nodes: Synth/Audio Source -> BassFilter -> EQ Filter 0..9 -> MasterGain -> Analyser -> Destination
      let lastNode: AudioNode = this.bassFilter;
      this.eqFilters.forEach((filter) => {
        lastNode.connect(filter);
        lastNode = filter;
      });

      lastNode.connect(this.masterGain);
      this.masterGain.connect(this.analyserNode);
      this.analyserNode.connect(this.ctx.destination);
    } catch (e) {
      console.warn("Web Audio API not supported in this browser environment", e);
    }
  }

  public updateAudioSettings(settings: AudioSettingsState) {
    if (!this.ctx) this.init();
    if (!this.ctx) return;

    // Volume
    if (this.masterGain) {
      this.masterGain.gain.setValueAtTime(settings.volume, this.ctx.currentTime);
    }

    // EQ
    this.eqFilters.forEach((filter, index) => {
      const gainValue = settings.eqEnabled ? (settings.bandGains[index] || 0) : 0;
      filter.gain.setValueAtTime(gainValue, this.ctx!.currentTime);
    });

    // Bass Boost
    if (this.bassFilter) {
      const bassDb = settings.eqEnabled ? (settings.bassBoost / 100) * 12 : 0;
      this.bassFilter.gain.setValueAtTime(bassDb, this.ctx.currentTime);
    }
  }

  public startSynthesis(songId: string) {
    if (!this.ctx) this.init();
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
    this.stopSynthesis();
    this.isSynthesizing = true;
    this.isPlaying = true;

    // Musical scale frequency generators for pleasant audio feedback in browser preview
    const baseFreqs = [220, 246.94, 261.63, 293.66, 329.63, 349.23, 392.00, 440.00];
    let noteIdx = 0;

    const playPulse = () => {
      if (!this.isSynthesizing || !this.ctx || !this.bassFilter) return;

      try {
        const osc = this.ctx.createOscillator();
        const noteGain = this.ctx.createGain();

        const freq = baseFreqs[noteIdx % baseFreqs.length];
        this.currentFreq = freq;
        noteIdx = (noteIdx + 1) % baseFreqs.length;

        osc.type = songId.includes('1') || songId.includes('6') ? 'sawtooth' : 'sine';
        osc.frequency.setValueAtTime(freq, this.ctx.currentTime);

        noteGain.gain.setValueAtTime(0.08, this.ctx.currentTime);
        noteGain.gain.exponentialRampToValueAtTime(0.0001, this.ctx.currentTime + 0.4);

        osc.connect(noteGain);
        noteGain.connect(this.bassFilter);

        osc.start(this.ctx.currentTime);
        osc.stop(this.ctx.currentTime + 0.45);
      } catch (e) {
        // Safe catch for closed ctx
      }
    };

    playPulse();
    this.animationTimer = window.setInterval(playPulse, 450);
  }

  public stopSynthesis() {
    this.isSynthesizing = false;
    this.isPlaying = false;
    if (this.animationTimer !== null) {
      clearInterval(this.animationTimer);
      this.animationTimer = null;
    }
  }

  public getByteFrequencyData(): Uint8Array {
    if (!this.analyserNode) return new Uint8Array(32);
    const buffer = new Uint8Array(this.analyserNode.frequencyBinCount);
    if (this.isPlaying) {
      this.analyserNode.getByteFrequencyData(buffer);
      // Fallback synthetic wave generator if frequency buffer is zero
      if (buffer.every((v) => v === 0)) {
        for (let i = 0; i < buffer.length; i++) {
          buffer[i] = Math.floor(60 + Math.sin(Date.now() / 150 + i) * 50 + Math.random() * 30);
        }
      }
    }
    return buffer;
  }
}

export const audioEngine = new WebAudioEngine();
