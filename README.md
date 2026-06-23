# AstreiaGram 🌌

AstreiaGram é uma aplicação de rede social moderna, fundamentada em uma arquitetura robusta de microsserviços. O projeto é composto por serviços de backend especializados e independentes, uma forte base de infraestrutura DevOps (com deploy automatizado e observabilidade de ponta), além de um aplicativo móvel nativo que atua como a principal interface para os usuários.

## 🏗️ Arquitetura e Microsserviços

O sistema foi desenhado de forma distribuída para suportar alta disponibilidade e permitir que cada serviço escale e evolua de maneira independente. Vale destacar que os serviços principais contam com operações completas de **CRUD** (Create, Read, Update, Delete) para seus domínios, garantindo uma gestão integral dos dados.

### 👤 User Service
- **Tecnologia:** Java (Spring Boot)
- **Banco de Dados:** PostgreSQL
- **Descrição:** Responsável por toda a gestão de usuários, autenticação e autorização via JWT. Ele mantém os perfis de usuários de forma segura e serve como a principal fonte de verdade para as identidades do sistema, sendo consultado por outros microsserviços. Toda a API está amplamente documentada de forma interativa via **Swagger**.
- **Destaque:** Possui **CRUD completo** de usuários, permitindo o registro, consulta de perfis, edição de dados e exclusão de contas.

### 📝 Post Service
- **Tecnologia:** Java (Spring Boot)
- **Banco de Dados:** MongoDB
- **Mensageria:** Apache Kafka
- **Descrição:** Cuida da criação, armazenamento e recuperação das publicações (posts). Ao receber a requisição de um novo post, este serviço se comunica com o `user-service` para validações e, após salvar no MongoDB, publica eventos no Kafka para serem processados de forma assíncrona. A documentação completa dos endpoints é fornecida via **Swagger**.
- **Destaque:** Possui **CRUD completo** de publicações, permitindo aos usuários criar, visualizar listagens, atualizar os detalhes e deletar seus próprios posts de forma eficiente.

### 📰 Feed Service
- **Tecnologia:** Go (Golang)
- **Armazenamento / Cache:** Redis
- **Mensageria:** Apache Kafka
- **Descrição:** Construído em Go visando extrema performance e baixa concorrência de recursos. Ele consome os eventos do Kafka gerados pelo *Post Service* e pré-calcula os feeds dos usuários em tempo real, armazenando essas visões no Redis para que a timeline seja carregada com latência quase nula. Possui também documentação interativa das rotas de acesso disponível via **Swagger**.

---

## 📱 Mobile

O frontend da aplicação encontra-se no diretório `/mobile` e representa a porta de entrada principal para a experiência do usuário final. Ele é um aplicativo completo, robusto e desenvolvido focado em alta usabilidade.
- **Tecnologia:** Kotlin (Android Nativo com Gradle).
- **Descrição:** Projetado para entregar uma experiência fluida e responsiva, o aplicativo integra-se perfeitamente às APIs dos microsserviços de forma escalável. Ele gerencia de ponta a ponta complexos fluxos de autenticação de forma segura, oferece uma navegação dinâmica pelas postagens (consumindo os dados em tempo real do *Feed Service*) e disponibiliza interfaces ricas para a criação, edição e exclusão de publicações (integrando-se aos fluxos de CRUD do backend). O desenvolvimento foca nas melhores práticas do ecossistema Android para garantir alta performance, estabilidade em diferentes dispositivos e uma interface intuitiva.

---

## ⚙️ CI/CD, DevOps e Infraestrutura

A infraestrutura foi pensada com forte inspiração em práticas de engenharia de software modernas (DevOps), resiliência e Event-Driven Architecture (Arquitetura Orientada a Eventos). Tudo orquestrado e conteinerizado via **Docker**.

### 🔄 Automação e Deploy (GitHub Actions)
O projeto adota práticas avançadas de **DevOps** com **deploy automatizado**. Através da utilização nativa do **GitHub Actions**, todo o ciclo de vida (CI/CD) é gerenciado automaticamente, garantindo que os fluxos de testes, builds de imagens Docker e as integrações sejam validadas a cada push e implantadas de maneira contínua e segura.

### 🗄️ Bancos de Dados e Mensageria
- **PostgreSQL:** Banco relacional adotado pelo `user-service` para garantir integridade estrutural nos dados de usuários.
- **MongoDB:** Banco de dados NoSQL focado em documentos flexíveis para as publicações do `post-service`.
- **Redis:** Armazenamento em memória (In-memory) para servir os dados calculados de feed rapidamente.
- **Apache Kafka:** Broker de mensageria e plataforma de streaming de eventos atuando como a espinha dorsal de comunicação assíncrona entre o Post Service e o Feed Service.

### 📊 Observabilidade e Monitoramento
Para a execução local e validação, todo o ecossistema roda sob uma orquestração via **Docker Compose**. O diretório `/infra` conta com uma stack de monitoramento de ponta:
- **Prometheus:** Coleta as métricas de performance e saúde de todos os microsserviços e instâncias.
- **Loki:** Agregador de logs focado em alta eficiência, permitindo a correlação entre logs e métricas.
- **Grafana Alloy:** Atua como um agente unificado (OpenTelemetry Collector) para processar e rotear dados de telemetria.
- **Grafana:** Interface visual centralizada que fornece dashboards interativos e alertas unindo dados do Prometheus (métricas) e Loki (logs).

---

## 🚀 Como Executar Localmente

Para subir todo o ecossistema backend, infraestrutura e observabilidade simultaneamente:

1. Certifique-se de possuir o [Docker](https://www.docker.com/) e o [Docker Compose](https://docs.docker.com/compose/) instalados em sua máquina.
2. Na raiz do projeto, execute o comando:
   ```bash
   docker-compose up -d --build
   ```

### 🌐 Acessos e Portas Úteis

#### Ferramentas de Observabilidade
- **Grafana (Dashboards):** [http://localhost:3000](http://localhost:3000) *(Usuário: `admin` / Senha: `admin`)*
- **Prometheus:** [http://localhost:9090](http://localhost:9090)

#### Serviços e Documentação (Swagger)
- **User Service:** [http://localhost:8080](http://localhost:8080)
  - 📖 **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Post Service:** [http://localhost:8081](http://localhost:8081)
  - 📖 **Swagger UI:** [http://localhost:8081/api/swagger-ui/index.html](http://localhost:8081/api/swagger-ui/index.html)
- **Feed Service:** [http://localhost:8082](http://localhost:8082)
  - 📖 **Swagger UI:** [http://localhost:8082/swagger/index.html](http://localhost:8082/swagger/index.html)
