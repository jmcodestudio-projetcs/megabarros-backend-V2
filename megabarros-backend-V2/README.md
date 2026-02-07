# MegaBarros Backend V2

Backend Java Spring Boot para autenticação e gestão de Seguradora, Produto, Apólice, Cliente e Corretor.

## Requisitos
- Java 21
- Maven 3.9+
- Docker (para Testcontainers)
- PostgreSQL 15+

## Perfis e Segurança
- **Perfis disponíveis:** `ADMIN`, `USUARIO`, `CORRETOR`
- **Autenticação:** JWT (Access Token + Refresh Token)
- **CORS:** Configurado para `http://localhost:5173` (frontend)
- Filtro `JwtAuthenticationFilter` popula `SecurityContext` com principal contendo `userId`, `email`, `role`

## Variáveis JWT (testes e dev)
Defina via propriedades ou variáveis de ambiente:
- `JWT_ISSUER=megabarros-v2`
- `JWT_AUDIENCE=megabarros-frontend`
- `JWT_SECRET=test-secret-32-bytes-minimum-1234567890`
- `JWT_ACCESS_EXP_SECONDS=3600` (1 hora)
- `JWT_REFRESH_EXP_SECONDS=1209600` (14 dias)

## Build e Testes
- Compilar: `mvn clean compile`
- Testes: `mvn clean test`
    - Testcontainers irá subir PostgreSQL em container
    - Migrations (Flyway) são aplicadas no schema `public`

## Execução local
- `mvn spring-boot:run`
- Backend: `http://localhost:8080`

---

# 📘 Guia para Frontend

## Base URL
```
http://localhost:8080
```

## 🔐 Autenticação

### POST `/auth/login`
**Acesso:** Público

Autentica usuário e retorna tokens JWT.

**Request:**
```json
{
  "email": "admin@example.com",
  "senha": "senha123"
}
```

**Response (200):**
```json
{
  "userId": 1,
  "email": "admin@example.com",
  "role": "ADMIN",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Erros:**
- `401 Unauthorized`: Credenciais inválidas `{"error": "invalid_credentials"}`
- `429 Too Many Requests`: Muitas tentativas `{"error": "too_many_attempts"}`

---

### POST `/auth/refresh`
**Acesso:** Público

Renova o access token usando refresh token.

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200):**
```json
{
  "userId": 1,
  "email": "admin@example.com",
  "role": "ADMIN",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Erros:**
- `401 Unauthorized`: Token inválido `{"error": "invalid_refresh_token"}`

---

### POST `/auth/change-password`
**Acesso:** Autenticado

Altera a senha do usuário autenticado.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "currentPassword": "senha123",
  "newPassword": "novaSenha@456"
}
```

**Response (200):** Vazio

**Erros:**
- `400 Bad Request`: Senha fraca `{"error": "weak_password", "message": "..."}`
- `401 Unauthorized`: Senha atual incorreta

---

## 🏢 Seguradoras

### GET `/api/seguradoras`
**Acesso:** Público

Lista todas as seguradoras com seus produtos e contagens de apólices.

**Response (200):**
```json
[
  {
    "idSeguradora": 1,
    "nomeSeguradora": "Seguradora XYZ",
    "apoliceCount": 15,
    "produtos": [
      {
        "idProduto": 1,
        "nomeProduto": "Auto Básico",
        "tipoProduto": "AUTO",
        "apoliceCount": 8
      },
      {
        "idProduto": 2,
        "nomeProduto": "Vida Essencial",
        "tipoProduto": "VIDA",
        "apoliceCount": 7
      }
    ]
  }
]
```

---

### POST `/api/seguradoras`
**Acesso:** ADMIN, USUARIO

Cria uma nova seguradora com produtos opcionais.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "nome": "Seguradora ABC",
  "produtos": [
    {
      "nomeProduto": "Residencial Premium",
      "tipoProduto": "RESIDENCIAL"
    }
  ]
}
```

**Response (201):**
```json
{
  "idSeguradora": 2,
  "nomeSeguradora": "Seguradora ABC",
  "apoliceCount": 0,
  "produtos": [
    {
      "idProduto": 3,
      "nomeProduto": "Residencial Premium",
      "tipoProduto": "RESIDENCIAL",
      "apoliceCount": 0
    }
  ]
}
```

**Erros:**
- `403 Forbidden`: Perfil sem permissão
- `409 Conflict`: Seguradora duplicada

---

### PUT `/api/seguradoras/{id}`
**Acesso:** ADMIN, USUARIO

Atualiza uma seguradora existente.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "nome": "Seguradora ABC Atualizada"
}
```

**Response (200):** Objeto SeguradoraResponse atualizado
**Erros:**
- `404 Not Found`: Seguradora não encontrada

---

### DELETE `/api/seguradoras/{id}`
**Acesso:** ADMIN, USUARIO

Exclui uma seguradora.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (204):** Vazio

**Erros:**
- `409 Conflict`: Seguradora possui apólices vinculadas

---

## 📦 Produtos

### POST `/api/seguradoras/{id}/produtos`
**Acesso:** ADMIN, USUARIO

Adiciona um produto a uma seguradora.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "nomeProduto": "Empresarial Completo",
  "tipoProduto": "EMPRESARIAL"
}
```

**Response (201):**
```json
{
  "idProduto": 4,
  "nomeProduto": "Empresarial Completo",
  "tipoProduto": "EMPRESARIAL",
  "apoliceCount": 0
}
```

---

### PUT `/api/seguradoras/{idSeguradora}/produtos/{idProduto}`
**Acesso:** ADMIN, USUARIO

Atualiza um produto.

**Response (200):** ProdutoResponse atualizado

---

### DELETE `/api/seguradoras/{idSeguradora}/produtos/{idProduto}`
**Acesso:** ADMIN, USUARIO

Remove um produto.

**Response (204):** Vazio

**Erros:**
- `409 Conflict`: Produto possui apólices vinculadas

---

## 👥 Clientes

### GET `/api/clientes`
**Acesso:** ADMIN, USUARIO, CORRETOR

Lista todos os clientes. CORRETOR vê apenas seus clientes.

**Response (200):**
```json
[
  {
    "idCliente": 1,
    "nome": "João Silva",
    "cpfCnpj": "12345678900",
    "dataNascimento": "1985-05-15",
    "email": "joao@example.com",
    "telefone": "(11) 98765-4321",
    "ativo": true
  }
]
```

---

### GET `/api/clientes/{id}`
**Acesso:** ADMIN, USUARIO, CORRETOR

Busca cliente por ID.

**Response (200):** ClienteResponse
**Erros:**
- `404 Not Found`: Cliente não encontrado

---

### POST `/api/clientes`
**Acesso:** ADMIN, USUARIO

Cria novo cliente.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "nome": "Maria Santos",
  "cpfCnpj": "98765432100",
  "dataNascimento": "1990-03-20",
  "email": "maria@example.com",
  "telefone": "(21) 91234-5678"
}
```

**Response (201):** ClienteResponse criado

**Validações:**
- `nome`: obrigatório, máx 150 caracteres
- `cpfCnpj`: obrigatório, máx 20 caracteres
- `dataNascimento`: obrigatório
- `email`: obrigatório, formato email válido, máx 150 caracteres
- `telefone`: obrigatório, máx 30 caracteres

---

### PUT `/api/clientes/{id}`
**Acesso:** ADMIN, USUARIO (todos os campos), CORRETOR (apenas email/telefone)

Atualiza cliente. CORRETOR pode atualizar apenas dados de contato.

**Request:**
```json
{
  "nome": "Maria Santos Silva",
  "cpfCnpj": "98765432100",
  "dataNascimento": "1990-03-20",
  "email": "maria.nova@example.com",
  "telefone": "(21) 91234-9999"
}
```

**Response (200):** ClienteResponse atualizado

**Regra:** CORRETOR só pode alterar `email` e `telefone`

---

### POST `/api/clientes/{id}/desativar`
**Acesso:** ADMIN, USUARIO

Desativa um cliente (soft delete).

**Response (204):** Vazio

---

## 🤝 Corretores

### GET `/api/corretores`
**Acesso:** Público

Lista todos os corretores.

**Response (200):**
```json
[
  {
    "idCorretor": 1,
    "idUsuario": 2,
    "nomeCorretor": "Carlos Corretor",
    "corretora": "Corretora ABC Ltda",
    "cpfCnpj": "12345678000199",
    "susepPj": "202300001",
    "susepPf": "123456",
    "email": "carlos@corretora.com",
    "telefone": "(11) 3333-4444",
    "uf": "SP",
    "dataNascimento": "1980-07-10",
    "doc": "RG: 12.345.678-9",
    "dataCriacao": "2024-01-15T10:30:00"
  }
]
```

---

### GET `/api/corretores/{id}`
**Acesso:** Público

Busca corretor por ID.

**Response (200):** CorretorResponse

---

### GET `/api/corretores/me`
**Acesso:** CORRETOR

Retorna o perfil do corretor autenticado.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200):** CorretorResponse do usuário logado

---

### POST `/api/corretores`
**Acesso:** ADMIN, USUARIO

Cria novo corretor.

**Request:**
```json
{
  "idUsuario": 3,
  "nomeCorretor": "Ana Corretora",
  "corretora": "Corretora XYZ",
  "cpfCnpj": "98765432000188",
  "susepPj": "202300002",
  "susepPf": "654321",
  "email": "ana@corretora.com",
  "telefone": "(11) 4444-5555",
  "uf": "RJ",
  "dataNascimento": "1985-09-25",
  "doc": "CNH: 123456789"
}
```

**Response (201):** CorretorResponse criado

**Validações:**
- `nomeCorretor`: obrigatório, máx 150 caracteres
- `email`: formato email válido, máx 150 caracteres
- `uf`: máx 2 caracteres

---

### PUT `/api/corretores/{id}`
**Acesso:** Qualquer autenticado (corretor pode editar próprio perfil)

Atualiza corretor.

**Response (200):** CorretorResponse atualizado

---

### DELETE `/api/corretores/{id}`
**Acesso:** Qualquer autenticado

Remove corretor.

**Response (204):** Vazio
**Erros:**
- `409 Conflict`: Corretor possui clientes vinculados

---

## 📋 Apólices

### GET `/api/apolices`
**Acesso:** ADMIN, USUARIO, CORRETOR

Lista apólices com filtros opcionais.

**Query Parameters:**
- `seguradoraId` (opcional): Filtra por seguradora
- `produtoId` (opcional): Filtra por produto
- `corretorClienteId` (opcional): Filtra por corretor-cliente

**Exemplo:** `GET /api/apolices?seguradoraId=1`

**Response (200):**
```json
[
  {
    "idApolice": 1,
    "numeroApolice": "APL-2024-001",
    "dataEmissao": "2024-01-10",
    "vigenciaInicio": "2024-01-15",
    "vigenciaFim": "2025-01-14",
    "valor": 5000.00,
    "comissaoPercentual": 10.00,
    "tipoContrato": "ANUAL",
    "idCorretorCliente": 1,
    "idProduto": 1,
    "idSeguradora": 1,
    "statusAtual": "ATIVA",
    "parcelas": [
      {
        "idParcela": 1,
        "numeroParcela": 1,
        "dataVencimento": "2024-02-10",
        "valorParcela": 416.67,
        "statusPagamento": "PAGA",
        "dataPagamento": "2024-02-08"
      }
    ],
    "coberturas": [],
    "beneficiarios": []
  }
]
```

---

### GET `/api/apolices/{id}`
**Acesso:** ADMIN, USUARIO, CORRETOR

Busca apólice por ID.

**Response (200):** ApoliceResponse

---

### POST `/api/apolices`
**Acesso:** ADMIN, USUARIO

Cria nova apólice.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "numeroApolice": "APL-2024-002",
  "dataEmissao": "2024-02-01",
  "vigenciaInicio": "2024-02-15",
  "vigenciaFim": "2025-02-14",
  "valor": 8000.00,
  "comissaoPercentual": 12.50,
  "tipoContrato": "ANUAL",
  "idCorretorCliente": 1,
  "idProduto": 2,
  "idSeguradora": 1,
  "coberturas": [],
  "beneficiarios": []
}
```

**Response (201):** ApoliceResponse criada

**Validações:**
- `numeroApolice`: obrigatório, máx 50 caracteres
- `dataEmissao`: obrigatória
- `vigenciaInicio`: obrigatória
- `vigenciaFim`: obrigatória
- `valor`: obrigatório, decimal
- `comissaoPercentual`: obrigatório, decimal
- `tipoContrato`: obrigatório, máx 50 caracteres
- `idCorretorCliente`: obrigatório
- `idProduto`: obrigatório
- `idSeguradora`: obrigatório

---

### PUT `/api/apolices/{id}`
**Acesso:** ADMIN, USUARIO

Atualiza apólice.

**Request:** Mesma estrutura do POST (campos opcionais)

**Response (200):** ApoliceResponse atualizada

---

### POST `/api/apolices/{id}/cancel`
**Acesso:** ADMIN, USUARIO

Cancela uma apólice.

**Query Parameters:**
- `reason` (opcional): Motivo do cancelamento

**Exemplo:** `POST /api/apolices/1/cancel?reason=Cliente solicitou`

**Response (204):** Vazio

**Regra:** Apólice muda status para CANCELADA

---

## 💰 Parcelas

### POST `/api/apolices/{id}/parcelas`
**Acesso:** ADMIN, USUARIO

Adiciona parcela a uma apólice.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "numeroParcela": 2,
  "dataVencimento": "2024-03-10",
  "valorParcela": 416.67
}
```

**Response (201):**
```json
{
  "idParcela": 2,
  "numeroParcela": 2,
  "dataVencimento": "2024-03-10",
  "valorParcela": 416.67,
  "statusPagamento": "PENDENTE",
  "dataPagamento": null
}
```

**Validações:**
- `numeroParcela`: obrigatório
- `dataVencimento`: obrigatória
- `valorParcela`: obrigatório, decimal

---

### POST `/api/apolices/parcelas/{parcelaId}/pay`
**Acesso:** ADMIN, USUARIO

Marca parcela como paga.

**Response (200):** ParcelaResponse com `statusPagamento: "PAGA"` e `dataPagamento` preenchida

---

## ⚠️ Tratamento de Erros

### Códigos de Status HTTP

| Código | Significado | Situações |
|--------|-------------|-----------|
| `200 OK` | Sucesso | Requisição processada com sucesso |
| `201 Created` | Criado | Recurso criado com sucesso |
| `204 No Content` | Sem conteúdo | Operação executada sem retorno |
| `400 Bad Request` | Requisição inválida | Validação falhou, dados inválidos |
| `401 Unauthorized` | Não autenticado | Token inválido/expirado, credenciais incorretas |
| `403 Forbidden` | Sem permissão | Perfil não autorizado para a ação |
| `404 Not Found` | Não encontrado | Recurso não existe |
| `409 Conflict` | Conflito | Duplicação, recurso vinculado, estado inválido |
| `429 Too Many Requests` | Muitas tentativas | Rate limiting ativado |

### Estrutura de Erro Padrão

**Validação (400):**
```json
{
  "timestamp": "2024-02-07T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "email",
      "message": "deve ser um endereço de e-mail bem formado"
    }
  ]
}
```

**Conflito (409):**
```json
{
  "timestamp": "2024-02-07T14:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Seguradora já existe com este nome"
}
```

**Acesso negado (403):**
```json
{
  "timestamp": "2024-02-07T14:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

---

## 🔒 Regras de Autorização

### Matriz de Permissões

| Recurso | ADMIN | USUARIO | CORRETOR |
|---------|-------|---------|----------|
| **Auth** | ✅ | ✅ | ✅ |
| **Seguradoras** (leitura) | ✅ | ✅ | ✅ |
| **Seguradoras** (escrita) | ✅ | ✅ | ❌ |
| **Produtos** (escrita) | ✅ | ✅ | ❌ |
| **Clientes** (leitura) | ✅ | ✅ | ✅ (apenas seus) |
| **Clientes** (criar) | ✅ | ✅ | ❌ |
| **Clientes** (atualizar) | ✅ (tudo) | ✅ (tudo) | ✅ (só contato) |
| **Clientes** (desativar) | ✅ | ✅ | ❌ |
| **Corretores** (leitura) | ✅ | ✅ | ✅ |
| **Corretores** (criar) | ✅ | ✅ | ❌ |
| **Corretores** (editar) | ✅ | ✅ | ✅ (próprio) |
| **Apólices** (leitura) | ✅ | ✅ | ✅ |
| **Apólices** (escrita) | ✅ | ✅ | ❌ |
| **Parcelas** (escrita) | ✅ | ✅ | ❌ |

---

## 📝 Regras de Negócio

### Seguradora
- Nome é obrigatório e único
- Não pode ser excluída se possuir apólices ativas
- Produtos são opcionais na criação

### Produto
- Nome obrigatório (máx 100 caracteres)
- Tipo opcional (máx 50 caracteres)
- Não pode ser excluído se possuir apólices

### Cliente
- CPF/CNPJ único
- Email único
- Desativação é soft delete (flag `ativo`)
- CORRETOR só pode alterar email/telefone

### Corretor
- Vinculado a um usuário (`idUsuario`)
- SUSEP PJ e PF são opcionais
- Não pode ser excluído se possuir clientes

### Apólice
- Número de apólice único
- Status inicial: `ATIVA`
- Vigência fim deve ser posterior à vigência início
- Cancelamento altera status para `CANCELADA`
- Comissão é percentual

### Parcela
- Status inicial: `PENDENTE`
- Pagamento marca como `PAGA` e registra data
- Número da parcela deve ser único por apólice

---

## 🚀 Fluxo de Autenticação Frontend

1. **Login:**
   ```javascript
   const response = await fetch('http://localhost:8080/auth/login', {
     method: 'POST',
     headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({ email: 'user@example.com', senha: 'senha123' })
   });
   const { accessToken, refreshToken, role, userId } = await response.json();
   
   // Armazenar tokens (localStorage/sessionStorage)
   localStorage.setItem('accessToken', accessToken);
   localStorage.setItem('refreshToken', refreshToken);
   localStorage.setItem('userRole', role);
   ```

2. **Requisições autenticadas:**
   ```javascript
   const response = await fetch('http://localhost:8080/api/seguradoras', {
     headers: {
       'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
     }
   });
   ```

3. **Refresh token quando access token expirar (401):**
   ```javascript
   if (response.status === 401) {
     const refreshResponse = await fetch('http://localhost:8080/auth/refresh', {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify({ 
         refreshToken: localStorage.getItem('refreshToken') 
       })
     });
     const { accessToken, refreshToken } = await refreshResponse.json();
     localStorage.setItem('accessToken', accessToken);
     localStorage.setItem('refreshToken', refreshToken);
     
     // Repetir requisição original
   }
   ```

---

## 📊 Tipos TypeScript (Referência)

```typescript
// Auth
interface LoginRequest {
  email: string;
  senha: string;
}

interface AuthResponse {
  userId: number;
  email: string;
  role: 'ADMIN' | 'USUARIO' | 'CORRETOR';
  accessToken: string;
  refreshToken: string;
}

// Seguradora
interface Seguradora {
  idSeguradora: number;
  nomeSeguradora: string;
  apoliceCount: number;
  produtos: Produto[];
}

interface Produto {
  idProduto: number;
  nomeProduto: string;
  tipoProduto: string;
  apoliceCount: number;
}

// Cliente
interface Cliente {
  idCliente: number;
  nome: string;
  cpfCnpj: string;
  dataNascimento: string; // ISO 8601
  email: string;
  telefone: string;
  ativo: boolean;
}

// Corretor
interface Corretor {
  idCorretor: number;
  idUsuario: number;
  nomeCorretor: string;
  corretora?: string;
  cpfCnpj?: string;
  susepPj?: string;
  susepPf?: string;
  email?: string;
  telefone?: string;
  uf?: string;
  dataNascimento?: string;
  doc?: string;
  dataCriacao: string;
}

// Apólice
interface Apolice {
  idApolice: number;
  numeroApolice: string;
  dataEmissao: string;
  vigenciaInicio: string;
  vigenciaFim: string;
  valor: number;
  comissaoPercentual: number;
  tipoContrato: string;
  idCorretorCliente: number;
  idProduto: number;
  idSeguradora: number;
  statusAtual: 'ATIVA' | 'CANCELADA';
  parcelas: Parcela[];
  coberturas: any[];
  beneficiarios: any[];
}

interface Parcela {
  idParcela: number;
  numeroParcela: number;
  dataVencimento: string;
  valorParcela: number;
  statusPagamento: 'PENDENTE' | 'PAGA';
  dataPagamento: string | null;
}

// Error
interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  details?: Array<{ field: string; message: string }>;
}
```

---

## Exceções e validações (Backend)

## Swagger (opcional)
Para habilitar documentação:
1. Adicione a dependência:
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.5.0</version>
</dependency>
```
2. Acesse `http://localhost:8080/swagger-ui.html`

## Auditoria e Logs
- Use cases registram logs com `actor` (username), `role`, IDs e resultado das operações.
- Sem vazamento de dados sensíveis (senhas, tokens, etc).

## Postman
- Coleções disponíveis em `*.postman_collection.json` para Seguradora/Produto, Apólice e Cliente.