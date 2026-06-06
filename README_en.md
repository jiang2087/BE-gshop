# G-Shop - E-Commerce Website with RAG Chatbot

This README summarizes the project from `DATN_NguyenVanHauGiang_2022604193.docx` and includes diagrams from the `images` directory.

## Overview

G-Shop is an e-commerce website for technology products. It integrates an AI chatbot based on RAG (Retrieval-Augmented Generation) to help users search for products, view product details, manage carts, place orders, apply vouchers, track order history, and ask product-related questions.

The RAG chatbot retrieves product data, knowledge documents, and dynamic business data before generating answers. This approach reduces inaccurate responses, improves information freshness, and keeps answers grounded in real system data.

## Objectives

- Research and apply RAG architecture to an AI chatbot for an e-commerce website.
- Build a chatbot that supports product consultation, frequently asked questions, pricing, configuration, warranty policies, and order status queries.
- Integrate semantic retrieval with a vector database.
- Evaluate the system by answer accuracy, response time, context retention, and user experience.

## Main Features

### Customers

- Register, log in, and authenticate accounts.
- Browse product lists, product details, variants, prices, and reviews.
- Search for products by keyword or through the RAG chatbot.
- Add products to a wishlist.
- Add, update, and remove products in the cart.
- Place orders, pay for orders, and apply vouchers or discount codes.
- Manage shipping addresses.
- Track order status and view purchase history.
- Chat with the AI chatbot for product consultation, product comparison, and related information.

### Administrators

- View dashboard statistics for revenue, orders, and best-selling products.
- Manage users, products, product variants, categories, and inventory.
- Manage orders, vouchers, discount codes, and reviews.
- Manage RAG chatbot data, including document uploads, data ingestion, retrieval testing, and AI response checks.

## System Architecture

![G-Shop system architecture](images/Ghop_architect.png)

The system is organized into multiple layers:

- Frontend handles the e-commerce website interface and chatbot UI.
- Spring Boot backend provides RESTful APIs, business logic, and chatbot orchestration.
- Security layer authenticates requests with JWT/Auth Filter before routing them to controllers.
- Business services handle Auth, Product, Cart, Order, Voucher, Discount, Review, Address, and Wishlist logic.
- JPA repositories store and retrieve business data in MySQL.
- RAG/AI services handle chatbot queries, data ingestion, retrieval, memory, and tool calling.
- Redis stores conversation memory to preserve chat context.
- Qdrant stores vector embeddings and supports semantic search.
- Ollama or OpenAI is used for embeddings or response generation, depending on configuration.

![Database model](images/database.png)

## RAG Flow

### Data Ingestion

![RAG data ingestion flow](images/ingestion.png)

1. Product data or knowledge documents are normalized into text.
2. The content is split into smaller chunks.
3. The system creates embeddings for each chunk.
4. Vector embeddings and metadata are stored in Qdrant.

### Chatbot Architecture

![RAG chatbot flow](images/RAG.png)

1. The user sends a question through the chatbot.
2. The backend stores conversation history in Redis.
3. `RagOrchestrator` coordinates query analysis, data retrieval, and prompt construction.
4. The system creates an embedding for the question and performs semantic search in Qdrant.
5. Hybrid reranking selects the most relevant context.
6. The prompt combines the question, conversation history, retrieved context, and tool-calling data when needed.
7. The LLM generates the final answer for the user.

## Technologies

- Backend: Java 21, Spring Boot, Spring Data JPA, RESTful API.
- Frontend: Next.js, React, Node.js, npm.
- Business database: MySQL.
- Vector database: Qdrant.
- Chat memory: Redis.
- AI/Embedding: Ollama or OpenAI API, depending on configuration.
- Supporting services: Docker.
- Security: JWT, refresh tokens, role-based authorization.

## Environment Requirements

- JDK 21.
- Node.js and npm.
- MySQL.
- Redis.
- Qdrant.
- Ollama running at `http://localhost:11434` if Ollama is used.
- Docker if Redis or Qdrant runs in containers.

Check Java:

```bash
java -version
```

Check Node.js and npm:

```bash
node -v
npm -v
```

## Configuration

Configure the required environment variables before running the system:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
API_KEY=
```

Depending on the actual source code, additional configuration may be required for Redis, Qdrant, Ollama/OpenAI, and security settings.

## Running the System

### Backend

1. Install and start MySQL, Redis, Qdrant, and Ollama.
2. Pull the embedding model for Ollama if the system uses Ollama.
3. Open the project in IntelliJ IDEA.
4. Select `Build Project` or `Run` to start the backend.

The backend runs by default at:

```text
http://localhost:8080
```

According to the project report, the system follows a model where the frontend can be integrated into the backend. Therefore, building or running the backend may also start or serve the frontend depending on project configuration.

### Frontend

If running the frontend separately:

```bash
npm install
npm run dev
```

The frontend runs by default at:

```text
http://localhost:3000
```

## Testing and Evaluation

Testing focuses on the main flows: shopping, RAG chatbot, and administration features.

Reported results:

- The RAG chatbot achieved an average correct-answer rate of 86.2% across 65 test questions.
- Average response time was about 2.6 seconds in the local test environment.
- Out-of-scope questions and queries that require dynamic data had lower accuracy because they depend on data loaded into Qdrant, intent detection, and tool-calling stability.

## Achievements

- Completed core e-commerce features: registration, login, product browsing, cart, ordering, vouchers, addresses, and order history.
- Integrated the RAG chatbot into the user interface.
- Enabled the chatbot to provide consultation, compare products, and answer based on product data, knowledge documents, and conversation context.
- Built an administration module for dashboard, user management, order management, product management, and RAG data management.
- Designed a separated architecture for sales business logic, data storage, semantic retrieval, and AI response generation.

## Limitations

- Testing is mainly focused on the local environment and does not deeply evaluate load handling with many concurrent users.
- Chatbot response quality depends on data loaded into Qdrant, embedding quality, and the ability to update product data in real time.
- Some business flows such as shipping, order notifications, and automated RAG evaluation still need more work before production deployment.

## Future Development

- Add performance testing, security testing, and an automated evaluation suite for the RAG chatbot.
- Integrate shipping APIs, email/SMS notifications, and order status synchronization.
- Develop a mobile version or PWA.
- Extend the RAG data administration module to track ingestion history, ingestion errors, and retrieval quality.

## Author

- Student: Nguyen Van Hau Giang.
- Topic: Applying RAG architecture to chatbot development for an e-commerce website.
- University: Hanoi University of Industry.
- Year: 2026.
