import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
            '@api': fileURLToPath(new URL('./src/api', import.meta.url)),
            '@components': fileURLToPath(new URL('./src/components', import.meta.url)),
            '@screens': fileURLToPath(new URL('./src/screens', import.meta.url)),
            '@hooks': fileURLToPath(new URL('./src/hooks', import.meta.url)),
            '@stores': fileURLToPath(new URL('./src/stores', import.meta.url)),
            '@i18n': fileURLToPath(new URL('./src/i18n', import.meta.url)),
            '@theme': fileURLToPath(new URL('./src/theme', import.meta.url)),
            '@types': fileURLToPath(new URL('./src/types', import.meta.url)),
        },
    },
    build: {
        outDir: 'dist',
        sourcemap: true,
        rollupOptions: {
            output: {
                manualChunks: {
                    vendor: ['react', 'react-dom', 'react-router-dom'],
                    i18n: ['i18next', 'react-i18next'],
                },
            },
        },
    },
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
            '/oauth': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
        },
    },
});
