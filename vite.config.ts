import path from "node:path";
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react-swc";
import { defineConfig } from "vite";

// https://vite.dev/config/
export default defineConfig({
  base: "./",
  server: {
    host: "0.0.0.0",
    port: 5173,
    allowedHosts: true,
    hmr: {
      overlay: false,
    },
    proxy: {
      "/api/opensky": {
        target: "https://opensky-network.org",
        changeOrigin: true,
        rewrite: (p) => p.replace("/api/opensky", "/api"),
      },
      "/api/opensky-auth": {
        target: "https://auth.opensky-network.org",
        changeOrigin: true,
        rewrite: (p) => p.replace("/api/opensky-auth", ""),
      },
    },
  },
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@/convex": path.resolve(__dirname, "./convex"),
      "@": path.resolve(__dirname, "./src"),
    },
    dedupe: [
      "react",
      "react-dom",
      "react/jsx-runtime",
      "react/jsx-dev-runtime",
    ],
  },
  build: {
    chunkSizeWarningLimit: 1000,
  },
});
