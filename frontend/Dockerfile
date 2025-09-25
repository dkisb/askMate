# ---------- Build stage ----------
FROM node:22-alpine AS build
WORKDIR /app

# Install dependencies first (leverages Docker layer caching)
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# ---------- Runtime stage ----------
FROM nginx:alpine

# Copy built assets to Nginx html directory
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port (for reference; Compose can map as needed)
EXPOSE 80

# Run Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]

