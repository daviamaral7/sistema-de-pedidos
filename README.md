# 📦 Sistema de Pedidos

API REST para gerenciamento de pedidos, desenvolvida com **Java 25** e **Spring Boot 4**.

> Projeto de portfólio com foco em boas práticas de desenvolvimento backend e modelagem de domínio: camadas bem definidas, validação de dados, persistência com JPA e ambiente dockerizado.

---

## 🚀 Tecnologias

| Tecnologia                  | Versão | Função                                 |
|-----------------------------|--------|----------------------------------------|
| Java                        | 25     | Linguagem principal                    |
| Spring Boot                 | 4.0.5  | Framework web                          |
| Spring Data JPA             | —      | Persistência de dados                  |
| Spring Validation           | —      | Validação de entradas                  |
| PostgreSQL                  | 16     | Banco de dados relacional              |
| Lombok                      | —      | Redução de boilerplate                 |
| Docker / Docker Compose     | —      | Containerização do ambiente            |
| dotenv (springboot4-dotenv) | 5.1.0  | Gerenciamento de variáveis de ambiente |

---

## 📋 Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/)
- [Java 25+](https://adoptium.net/)
- [Maven](https://maven.apache.org/) (ou use o `./mvnw` incluso no projeto)

---

## ⚙️ Como rodar o projeto

### 1. Clone o repositório

```bash
git clone https://github.com/daviamaral7/sistema-de-pedidos.git
cd sistema-de-pedidos
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto com base no exemplo abaixo:

```env
# Banco de dados
POSTGRES_DB=sistema_pedidos
POSTGRES_USER=postgres
POSTGRES_PASSWORD=sua_senha_aqui

# PgAdmin (interface visual do banco)
PGADMIN_DEFAULT_EMAIL=admin@admin.com
PGADMIN_DEFAULT_PASSWORD=admin
```

### 3. Suba o banco de dados com Docker

```bash
docker compose up -d
```

Isso inicializa:
- **PostgreSQL** na porta `5432`
- **PgAdmin** na porta `8081` → acesse em [http://localhost:8081](http://localhost:8081)

### 4. Rode a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

---

## 🗂️ Estrutura do projeto

```
src/
└── main/
    └── java/com/davi/sistema_de_pedidos/
        ├── controller/    # Endpoints REST
        ├── dto/           # Objetos de transferência de dados
        ├── exceptions/    # Tratamento de erros e exceções customizadas
        ├── model/         # Entidades JPA
        ├── repository/    # Acesso ao banco via JPA
        ├── service/       # Regras de negócio
        └── util/          # Classes utilitárias e helpers 
```

---

## 🔌 Endpoints da API

**Base URL**: http://localhost:8080

### Customers

| Método   | Rota              | Descrição               | Status           |
|----------|-------------------|-------------------------|------------------|
| `POST`   | `/customers`      | Cria um novo cliente    | `201 Created`    |
| `GET`    | `/customers`      | Lista todos os clientes | `200 OK`         |
| `GET`    | `/customers/{id}` | Busca cliente por ID    | `200 OK`         |
| `PUT`    | `/customers/{id}` | Atualiza um cliente     | `200 OK`         |
| `DELETE` | `/customers/{id}` | Remove um cliente       | `204 No Content` |

### Orders

| Método | Rota                    | Descrição                            | Status        |
|--------|-------------------------|--------------------------------------|---------------|
| `POST` | `/orders`               | Cria um novo pedido                  | `201 Created` |
| `GET`  | `/orders/{id}`          | Busca pedido por ID                  | `200 OK`      |
| `GET`  | `/orders/customer/{id}` | Lista todos os pedidos de um cliente | `200 OK`      |
| `PUT`  | `/orders/{id}/pay`      | Realiza o pagamento do pedido        | `200 OK`      |
| `PUT`  | `/orders/{id}/cancel`   | Cancela um pedido                    | `200 OK`      |

### Products

| Método   | Rota             | Descrição               | Status           |
|----------|------------------|-------------------------|------------------|
| `POST`   | `/products`      | Cria um novo produto    | `201 Created`    |
| `GET`    | `/products`      | Lista todos os produtos | `200 OK`         |
| `GET`    | `/products/{id}` | Busca produto por ID    | `200 OK`         |
| `PUT`    | `/products/{id}` | Atualiza um produto     | `200 OK`         |
| `DELETE` | `/products/{id}` | Remove um produto       | `204 No Content` |

### Exemplo de requisição — criar pedido

```http
POST /orders
Content-Type: application/json

{
  "customerId": "UUID",
  "items": [
    {
      "productId": "UUID",
      "quantity": 1
    }
  ]
}
```

### Exemplo de resposta

```json
{
  "orderId": "UUID",
  "customer": {
    "id": "UUID",
    "name": "João Silva"
  },
  "orderDate": "2026-04-17T10:30:00",
  "orderStatus": "CREATED",
  "total": 3500.00,
  "purchasedItems": [
    {
      "productId": "UUID",
      "productName": "Notebook",
      "quantity": 1,
      "priceAtPurchase": 3500.00
    }
  ]
}
```
---
## 🧠 Regras de negócio

- Pedido inicia com status `CREATED`
- Apenas pedidos com status `CREATED` podem ser pagos ou cancelados
- Não é possível pagar um pedido cancelado
- Não é possível cancelar um pedido já pago
- O preço do produto é fixado no momento da compra (`priceAtPurchase`)
- Um pedido deve possuir pelo menos um item
- A quantidade de itens deve ser maior que zero
---

## 🧪 Testes

```bash
./mvnw test
```

- `CustomerControllerTest`: cobre criacao, listagem, busca por ID, atualizacao, remocao, validacao de payload, `404` para cliente inexistente e `409` para email ja cadastrado.
- `OrderControllerTest`: cobre criacao de pedido, busca por ID, listagem por customer, pagamento, cancelamento, validacao de payload, `404` para pedido/customer inexistente e `409` para transicoes invalidas de status.
- `ProductControllerTest`: cobre criacao, listagem, busca por ID, atualizacao, remocao, validacao de payload e `404` para produto inexistente.
- `CustomerServiceTest`: valida persistencia, busca, atualizacao, remocao, regras de email unico e erros `404`/`409`.
- `OrderServiceTest`: valida criacao de pedidos com multiplos itens, calculo de total, snapshot de `priceAtPurchase`, busca por pedido/customer, pagamento, cancelamento e regras de negocio centrais do fluxo de pedidos.
- `ProductServiceTest`: valida persistencia, busca, atualizacao, remocao e erros `404` para produto inexistente.

---

## 🐳 Serviços Docker

| Serviço    | Porta  | Descrição                     |
|------------|--------|-------------------------------|
| PostgreSQL | `5432` | Banco de dados principal      |
| PgAdmin    | `8081` | Interface visual para o banco |

---

## 📌 Aprendizados e decisões técnicas

- **Camadas separadas (Controller → Service → Repository):** mantém o código organizado e facilita testes
- **Bean Validation:** validação das entradas diretamente nas DTOs com anotações (`@NotNull`, `@NotBlank`, etc.)
- **dotenv para variáveis sensíveis:** nenhuma senha ou configuração fica hardcoded no código
- **Docker Compose:** ambiente reproduzível em qualquer máquina sem instalar PostgreSQL localmente

---

## 🤝 Autor

**Davi Amaral**
[github.com/daviamaral7](https://github.com/daviamaral7)
