# Study Genie - Backend (study-api-lesson)

Este monorepo contém a API REST do **Study Genie**, desenvolvida para gerenciar disciplinas, aulas, tags e a geração inteligente de resumos em integração com o serviço **Content Generator** (SeniorLabs).

---

## 🛠️ Tecnologias e Versões

- **Java**: 17
- **Spring Boot**: 4.0.7
- **Banco de Dados**: PostgreSQL 18
- **Migrações**: Flyway 12.6.0
- **Extração de Texto**: Apache PDFBox 3.x e Apache POI para arquivos PDF, DOCX e TXT.
- **Documentação da API**: Springdoc OpenAPI (Swagger) v2.8.9

---

## 📂 Estrutura de Pacotes

A estrutura segue as regras de desenvolvimento definidas para o projeto, organizando os controllers em um pacote global e o restante do domínio encapsulado em subdiretórios específicos:

```text
com.studygenie.lesson
├── controller/             # Todos os REST Controllers (ex: TagController, SummaryController)
├── course/                 # Entidade, Repositório e DTOs de Disciplinas (Courses)
├── lesson/                 # Entidade, Repositório e DTOs de Aulas (Lessons)
├── summary/                # Entidade, Repositório e DTOs de Resumos (Summaries)
├── tag/                    # Entidade, Repositório e DTOs de Tags
├── fileextraction/         # Serviços de extração de texto bruto de PDFs, Word e TXT
├── contentgenerator/       # Cliente HTTP e autenticação (OAuth2 com cache) no Content Generator
└── ai/                     # Geração de resumos usando LLM via Content Generator
```

---

## 🚀 Como Executar Localmente

### Pré-requisitos
1. **Java 17** instalado e configurado no `PATH`.
2. **Docker** para subir o banco de dados.

### Passo 1: Iniciar o Banco de Dados
Na raiz do diretório `apps/study-api-lesson`, execute o Docker Compose para subir o PostgreSQL 18:
```bash
docker compose up -d
```

### Passo 2: Configurar as Variáveis de Ambiente
Crie um arquivo `.env` com base no arquivo `.env.example` presente na raiz deste diretório e insira as credenciais reais:
- `POSTGRES_LESSON_DB`
- `POSTGRES_LESSON_USER`
- `POSTGRES_LESSON_PASSWORD`
- `CONTENT_GENERATOR_ACCESS_KEY`
- `CONTENT_GENERATOR_SECRET`
- `CONTENT_GENERATOR_TENANT_NAME`

### Passo 3: Executar a Aplicação
Para executar a aplicação Java pelo terminal PowerShell, configure as variáveis de ambiente temporárias e execute o jar/classe compilada. Segue o modelo de uso do script passando suas credenciais corretas:

```powershell
${env:POSTGRES_LESSON_DB}='lesson'; ${env:POSTGRES_LESSON_USER}='api-study-leasson'; ${env:POSTGRES_LESSON_PASSWORD}='123456'; ${env:CONTENT_GENERATOR_ACCESS_KEY}='sua-chave'; ${env:CONTENT_GENERATOR_SECRET}='seu-secret'; ${env:CONTENT_GENERATOR_TENANT_NAME}='seu-tenant-name'; & 'C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin\java.exe' '@C:\Users\jesse\AppData\Local\Temp\cp_43ii4q9iwi8zg1ae6yynxt14p.argfile' 'com.studygenie.lesson.LessonApplication'
```
*(Nota: Certifique-se de ajustar o caminho da JDK e do argfile conforme sua máquina e IDE).*

---

## 🔗 Links Úteis

- **Documentação OpenAPI (Swagger)**: http://localhost:8080/swagger-ui/index.html
- **Database Schema**: Para mais detalhes de tabelas e relacionamentos, consulte a documentação técnica em [DATABASE_SCHEMA.md](../../docs/api-lesson/DATABASE_SCHEMA.md).