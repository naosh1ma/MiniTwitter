# Stage 1: Build static assets using Node.js
FROM node:20-alpine AS build
WORKDIR /app

# Defaults to "/" (app served at its own domain/subdomain root). Override at
# build time (see docker-compose.prod.yml) when serving under a path prefix,
# e.g. --build-arg BASE_HREF=/minitwitter/
ARG BASE_HREF=/

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production --base-href "$BASE_HREF"

# Stage 2: Serve static assets using Nginx
FROM nginx:alpine
COPY --from=build /app/dist/minitwitter-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
