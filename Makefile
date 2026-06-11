.PHONY: help up down logs build backend-test frontend-build clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

up: ## Start the full stack (db + api + web) with Docker Compose
	docker compose up --build

down: ## Stop and remove containers
	docker compose down

logs: ## Tail logs from all services
	docker compose logs -f

build: ## Build all Docker images
	docker compose build

backend-test: ## Run backend unit tests
	cd backend && mvn -B verify

frontend-build: ## Lint and build the frontend
	cd frontend && npm install && npm run lint && npm run build

clean: ## Remove build artifacts
	cd backend && mvn -q clean || true
	rm -rf frontend/dist frontend/node_modules
