# ---------- Build stage ----------
FROM oven/bun:1-alpine AS build
WORKDIR /app

# Install dependencies first (leverages Docker layer caching)
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

# Copy source and build
COPY . .
RUN bun run build

# ---------- Runtime stage ----------
FROM nginx:alpine

# Copy built assets to Nginx html directory
COPY --from=build /app/dist /usr/share/nginx/html

# Custom nginx config: proxy /api to backend
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port (for reference; Compose can map as needed)
EXPOSE 80

# Run Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]

