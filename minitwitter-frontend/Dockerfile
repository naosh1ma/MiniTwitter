# Stage 1: Build static assets using Node.js
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY .. .
RUN npm run build -- --configuration production

# Stage 2: Serve static assets using Nginx
FROM nginx:alpine
COPY --from=build /app/dist/mini-twitter/browser /usr/share/nginx/html
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
