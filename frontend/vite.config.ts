// vite.config.ts
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    target: 'es2020',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom', 'react-router-dom', 'axios'],
        },
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
    exclude: ['node_modules', 'src/api-client/**'], // <-- exclude auto-generated API
    coverage: {
      provider: 'istanbul',
      reporter: ['text', 'lcov'],
      reportsDirectory: 'coverage',
      exclude: ['src/api-client/**'], // <-- also exclude from coverage
    },
    reporters: [
      'default',
      ['junit', { outputFile: 'test-results/results.xml' }],
    ],
  },
});
