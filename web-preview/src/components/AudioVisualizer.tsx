import React, { useEffect, useRef } from 'react';
import { VisualizerMode } from '../types';
import { audioEngine } from '../lib/audioEngine';

interface AudioVisualizerProps {
  mode: VisualizerMode;
  isPlaying: boolean;
  className?: string;
}

export const AudioVisualizer: React.FC<AudioVisualizerProps> = ({
  mode,
  isPlaying,
  className = ''
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    let animationFrameId: number;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let particles: { x: number; y: number; radius: number; vx: number; vy: number; color: string }[] = [];
    for (let i = 0; i < 30; i++) {
      particles.push({
        x: Math.random() * 300,
        y: Math.random() * 200,
        radius: Math.random() * 3 + 1,
        vx: (Math.random() - 0.5) * 1.5,
        vy: (Math.random() - 0.5) * 1.5,
        color: i % 2 === 0 ? '#ff6b35' : '#ff9f1c'
      });
    }

    const render = () => {
      const width = canvas.width;
      const height = canvas.height;
      ctx.clearRect(0, 0, width, height);

      const data = audioEngine.getByteFrequencyData();

      if (mode === 'bars' || mode === 'spectrum') {
        const barWidth = (width / data.length) * 1.8;
        let x = 0;

        for (let i = 0; i < data.length; i++) {
          const barHeight = (data[i] / 255) * height * 0.85;
          const gradient = ctx.createLinearGradient(0, height, 0, height - barHeight);
          gradient.addColorStop(0, '#ff6b35');
          gradient.addColorStop(1, '#ffa500');

          ctx.fillStyle = gradient;
          ctx.fillRect(x, height - barHeight, barWidth - 2, barHeight);

          x += barWidth;
        }
      } else if (mode === 'waves') {
        ctx.beginPath();
        ctx.lineWidth = 3;
        ctx.strokeStyle = '#ff6b35';

        const sliceWidth = width / data.length;
        let x = 0;

        for (let i = 0; i < data.length; i++) {
          const v = data[i] / 128.0;
          const y = (v * height) / 2;

          if (i === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }

          x += sliceWidth;
        }

        ctx.lineTo(width, height / 2);
        ctx.stroke();
      } else if (mode === 'particles') {
        particles.forEach((p, index) => {
          const freqVal = data[index % data.length] || 10;
          p.x += p.vx * (freqVal / 50);
          p.y += p.vy * (freqVal / 50);

          if (p.x < 0) p.x = width;
          if (p.x > width) p.x = 0;
          if (p.y < 0) p.y = height;
          if (p.y > height) p.y = 0;

          ctx.beginPath();
          ctx.arc(p.x, p.y, p.radius * (1 + freqVal / 80), 0, Math.PI * 2);
          ctx.fillStyle = p.color;
          ctx.shadowBlur = 10;
          ctx.shadowColor = '#ff6b35';
          ctx.fill();
        });
      } else if (mode === 'circle') {
        const centerX = width / 2;
        const centerY = height / 2;
        const radius = Math.min(width, height) * 0.28;

        for (let i = 0; i < data.length; i++) {
          const angle = (i / data.length) * Math.PI * 2;
          const barLen = (data[i] / 255) * 45;

          const x1 = centerX + Math.cos(angle) * radius;
          const y1 = centerY + Math.sin(angle) * radius;
          const x2 = centerX + Math.cos(angle) * (radius + barLen);
          const y2 = centerY + Math.sin(angle) * (radius + barLen);

          ctx.beginPath();
          ctx.moveTo(x1, y1);
          ctx.lineTo(x2, y2);
          ctx.lineWidth = 3;
          ctx.strokeStyle = `hsl(${(i * 12) % 360}, 90%, 60%)`;
          ctx.stroke();
        }
      }

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      cancelAnimationFrame(animationFrameId);
    };
  }, [mode, isPlaying]);

  return (
    <div className={`relative flex items-center justify-center bg-neutral-950/80 rounded-2xl border border-neutral-800/80 overflow-hidden ${className}`}>
      <canvas
        ref={canvasRef}
        width={320}
        height={220}
        className="w-full h-full object-contain"
      />
    </div>
  );
};
