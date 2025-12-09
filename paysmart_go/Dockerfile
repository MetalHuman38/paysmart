# syntax=docker/dockerfile:1

############################
# Dev target: fast iteration
############################
FROM golang:1.25-bookworm AS dev
WORKDIR /app

# Go modules on; better caching
ENV GO111MODULE=on

# Prime the module cache
COPY go.mod go.sum ./
RUN --mount=type=cache,target=/go/pkg/mod go mod download

# Bring in the rest of the source
COPY . .

# Default command can be overridden by docker compose
CMD ["go", "run", "./cmd/paysmartd"]

############################
# Prod build: static binary
############################
FROM golang:1.22-bookworm AS build
WORKDIR /app

COPY go.mod go.sum ./

RUN --mount=type=cache,target=/go/pkg/mod go mod download
COPY . .

# Build a small, static binary
RUN --mount=type=cache,target=/root/.cache/go-build \
    CGO_ENABLED=0 go build -ldflags="-s -w" -o /out/server ./cmd/funcframework

# Minimal runtime image
FROM gcr.io/distroless/base-debian12 AS prod
WORKDIR /srv
COPY --from=build /out/server /srv/server
EXPOSE 8080
USER nonroot:nonroot
ENV PORT=8080
ENTRYPOINT ["/srv/server"]