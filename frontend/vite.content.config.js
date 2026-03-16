import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    emptyOutDir: false,
    rollupOptions: {
      input: {
        content: 'src/main.jsx',
      },
      output: {
        entryFileNames: 'content.js',
        assetFileNames: 'styles.css',
        inlineDynamicImports: true,
      },
    },
  },
})