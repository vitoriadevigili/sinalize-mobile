# Sinalize Mobile

Aplicativo **Android** para aprender **Libras** (Língua Brasileira de Sinais) de forma interativa, desenvolvido com **Kotlin** e **Jetpack Compose** e integrado a uma API em **Java Spring Boot**.

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-IDE-3DDC84?logo=androidstudio&logoColor=white)
![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

---

## Sobre o projeto

O **Sinalize Mobile** leva o quiz **Sinalize** para o celular. Um avatar 3D do **VLibras** executa o sinal de uma palavra, e o jogador escolhe, entre quatro alternativas, qual palavra foi sinalizada.

O projeto nasceu na disciplina de Informática na Educação (React e NestJS) e foi adaptado para dispositivos móveis em **Programação para Dispositivos Móveis** (API Java). O app reproduz as telas e a identidade visual da versão web e se adapta a celulares e tablets, na vertical e na horizontal.

O repositório tem o **app Android** (`android/`) e a **API REST** com o banco de dados (`backend/`).

**Vídeo de apresentação:** [adicionar link]

---

## Funcionalidades

| Tela           | O que o usuário faz                                                                                                                                                                                             |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Página inicial | Acessa o quiz, o dicionário e as informações do projeto                                                                                                                                                         |
| Quiz           | Escolhe categorias, assiste ao sinal do avatar e responde. A resposta fica verde ou vermelha, o progresso aparece no topo e o resultado é mostrado no final. O botão **Repetir** faz o avatar sinalizar de novo |
| Dicionário     | Consulta, cadastra, edita e exclui palavras e categorias, com busca, filtro por letra e ordenação                                                                                                               |

Recursos de dispositivos móveis: layout responsivo (de 1 a 4 colunas, conforme a tela), suporte a retrato e paisagem, arquitetura MVVM, comunicação com a API por Retrofit e avatar do VLibras em uma `WebView`.

---

## Tecnologias utilizadas

| Camada         | Tecnologia                                                  |
| -------------- | ----------------------------------------------------------- |
| App            | Kotlin, Jetpack Compose e Material 3                        |
| Arquitetura    | MVVM (ViewModel e Navigation Compose)                       |
| Rede           | Retrofit, OkHttp e kotlinx.serialization                    |
| Sinalização    | VLibras, em uma `WebView`                                   |
| API            | Java 21 e Spring Boot 3.5 (Web, Data JPA e Bean Validation) |
| Banco de dados | PostgreSQL 16 e Flyway                                      |
| Ambiente       | Docker Compose                                              |

> Não é necessário instalar o Maven nem o Gradle: o projeto já inclui os wrappers (`mvnw` e `gradlew`).

---

## Pré-requisitos

- Android Studio, com o Android SDK e um emulador de Android 8.0 ou superior;
- Java JDK 21 (confira com `java -version`);
- Docker e Docker Compose;
- Git.

Se o `JAVA_HOME` apontar para o Java do Android Studio, ajuste para o JDK 21 antes de rodar a API (PowerShell):

```powershell
$env:JAVA_HOME = "C:\caminho\do\jdk-21"
```

---

## Passo a passo

Os comandos valem para o PowerShell, na raiz do repositório. No Linux ou no Mac, use `./mvnw` e `./gradlew`.

### 1. Clonar e subir o banco

```bash
git clone https://github.com/vitoriadevigili/sinalize-mobile.git
cd sinalize-mobile
docker compose up -d
```

Aguarde o status `healthy` (`docker compose ps`). O PostgreSQL fica em `localhost:5433`, com banco, usuário e senha `sinalize`. O Flyway cria as tabelas e carrega os dados iniciais na primeira execução da API.

### 2. Iniciar a API

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`. Para conferir, acesse `/actuator/health`: a resposta deve ser `{"status":"UP"}`. Mantenha o terminal aberto enquanto usar o app.

### 3. Rodar o app

1. Abra a pasta **`android/`** no Android Studio (não a raiz do repositório) e aguarde o Gradle sincronizar.
2. No _Device Manager_, ligue um emulador com Android 8.0 ou superior.
3. Selecione a configuração `app` e clique em **Run**.
4. Na primeira vez, toque em **Permitir** quando o Android pedir acesso a dispositivos próximos. Essa permissão é necessária para o app alcançar a API na rede local.

### Endereço da API

Por padrão, o app acessa a API em `http://10.0.2.2:8080/`, endereço que o emulador usa para chegar ao computador. Em celular físico, passe o IP do computador na rede Wi-Fi com `-PsinalizeApiUrl` no terminal ou no arquivo `android/gradle.properties`, por exemplo `-PsinalizeApiUrl=http://192.168.0.10:8080/`.

### Problemas comuns

- **"Não foi possível conectar ao servidor":** confira se a API está no ar e se a permissão de dispositivos próximos foi concedida.
- **O avatar não aparece:** o VLibras precisa de internet e, no emulador, pode levar de 20 a 60 segundos para carregar.

---

## Dados iniciais

6 categorias (Saudações, Família, Cores, Animais, Alimentos e Lugares), 44 palavras e um usuário padrão (`admin@sinalize.com.br` / `Sinalize123`). Ainda não há tela de login: as operações são feitas em nome desse usuário.

---

## API REST

O arquivo `backend/requests.http` traz requisições prontas de todas as rotas (`/categorias`, `/palavras`, `/quizzes` e `/usuarios`), para usar com a extensão REST Client do VS Code ou com o HTTP Client do IntelliJ.

---

## Testes automatizados

```powershell
cd backend
.\mvnw.cmd test       # unidade (API)
.\mvnw.cmd verify     # inclui integração (precisa do Docker no ar)

cd ..\android
.\gradlew.bat testDebugUnitTest
```

---

## Uso de IA

O Claude (Anthropic) foi usado como apoio na estruturação do projeto e na revisão do código e da documentação.

---

## Equipe

| Integrante                        |
| --------------------------------- |
| Pedro Paoli Neto                  |
| Luís Felipe dos Anjos de Carvalho |
| Isadora Pimenta de Oliveira       |
| Larissa Colzani                   |
| Vitória Carolina Devigili         |

**Professor:** Allan Rodrigo Leite

**Disciplina:** Programação para Dispositivos Móveis

**Curso/Turma:** TADS 2026/2
