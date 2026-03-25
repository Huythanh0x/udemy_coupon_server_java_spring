FROM node:20-alpine AS deps
WORKDIR /app

# Install dependencies first (better layer caching)
COPY package.json package-lock.json ./
RUN npm ci

FROM node:20-alpine AS builder
WORKDIR /app

COPY --from=deps /app/node_modules ./node_modules
COPY . .

ARG NEXT_PUBLIC_API_URL=""
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NODE_ENV=production

# Next.js can run without a public/ dir, but our Dockerfile copies it.
# Create an empty one when missing to keep the image build stable.
RUN mkdir -p public

RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production

# Keep runtime deps (Next.js needs them for next start)
COPY --from=builder /app/package.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

EXPOSE 3000

# Use shell form for PORT env substitution.
CMD sh -c "npm start -- -p ${PORT:-3000} -H 0.0.0.0"

