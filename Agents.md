# AGENTS.md

## 1 — Contexto do projeto

* **Stack:** Java 21 +, Spring Boot 3, Maven.
* **Estrutura Padrão:**

```text
matchapp/
├─ pom.xml
└─ src/
   ├─ main/
   │  ├─ java/
   │  │  └─ com/example/matchapp/
   │  │     ├─ model/ Profile.java
   │  │     ├─ service/ ImageGenerationService.java
   │  │     └─ config/ ImageGenProperties.java
   │  └─ resources/
   │     ├─ profile.json          ← arquivo de entrada
   │     └─ static/images/           ← gravar aqui as imagens geradas
   └─ test/ …                     ← testes JUnit 5 + Mockito
```

* **Boas práticas:** SOLID, Clean Code, Effective Java, logs estruturados (SLF4J + Logback), try‑with‑resources, DTOs imutáveis.

---

## 2 — Objetivo do agente

Para **cada** objeto em `profile.json`, gerar **uma foto digital realista** coerente com os atributos do perfil e gravá‑la em:

```
src/main/resources/static/images/{imageUrl}
```

Depois, devolver um novo JSON na mesma estrutura, acrescentando:

```json
"imageGenerated": true
```

Este agente deve utilizar **um modelo de geração de imagem da OpenAI**, como `DALL·E`, através da API oficial.

A **chave de API deve estar configurada via variável de ambiente** e lida a partir do arquivo `.env`, usando uma propriedade como:

```env
OPENAI_API_KEY=your_openai_key_here
OPENAI_BASE_URL=https://api.openai.com/v1/images/generations
```

Um exemplo desse arquivo está disponível em `.env.exemplo`. O projeto carrega automaticamente o arquivo `.env` usando `EnvFileLoader` se ele existir.

---

## 3 — Estrutura de entrada (`profile.json`)

```json
[
  {
    "id": "97da7cbd-cd8b-4629-a95c-5b157a3464b5",
    "firstName": "Valentina",
    "lastName": "Rodriguez",
    "age": 26,
    "ethnicity": "Hispanic",
    "gender": "FEMALE",
    "bio": "Journalist and storyteller. Passionate about discovering new cultures and sharing their stories.",
    "imageUrl": "97da7cbd-cd8b-4629-a95c-5b157a3464b5.jpg",
    "myersBriggsPersonalityType": "ENFP"
  }
]
```

> A lista real contém 20 perfis com o mesmo formato.

---

## 4 — Regras de geração de imagem

1. **Aparência** – combine `ethnicity` e `age` para traços faciais e tom de pele plausíveis.
2. **Expressão & postura** – reflita indícios de personalidade (campo `bio` + MBTI).
3. **Cenário** – ambiente sutilmente relacionado ao hobby/profissão; fundo limpo.
4. **Iluminação** – luz natural suave ou estúdio soft‑box, conforme adequado.
5. **Roupa** – casual elegante coerente com a descrição; evitar logotipos/marcas.
6. **Técnica** – 1024 × 1024 px, proporção 1:1, JPEG, cores naturais, sem marcas‑d’água.
7. **Nome do arquivo** – usar exatamente o valor de `imageUrl`.

---

## 5 — Saída esperada

1. Arquivo de imagem para cada perfil salvo no caminho indicado.
2. Relatório JSON com a lista original **+** `"imageGenerated": true`.

---

## 6 — Requisitos de integração & código

* **Serviço:** `ImageGenerationService` (interface) → implementação `SpringAIImageGenerationService` (bean primário) ou `OpenAIImageGenerationService` para permitir *dependency inversion*.
* **Geração de prompt:** use `PromptBuilderService` para criar prompts ricos a partir dos atributos do perfil. A implementação padrão é `DefaultPromptBuilderService`.
* `ProfileService` carrega `profile.json` (Jackson) e delega ao `ImageGenerationService`.
* `ImageGenProperties` usa `@ConfigurationProperties` para chave de API, timeout etc.; não hardcodar.
* `.env` deve ser carregado automaticamente (ex: usando biblioteca `dotenv-java`).
* **Logging:** incluir `profileId` via MDC em cada chamada ao serviço externo.
* **Testes:**

  * `ProfileServiceTest` usando `@TempDir` para verificar gravação de arquivos.
  * Mock do cliente HTTP simulando resposta binária da imagem.
* **CI:** estágio **verify** (`mvn test` ou `./mvnw test`) e **package**; falhas de teste quebram o build.
* **API REST:** a aplicação expõe endpoints em `/api` para CRUD de perfis e geração de imagens. Consulte `README.md` para exemplos de uso via `curl` ou `api_tests.http`.
* **Execução local:** utilize `mvn spring-boot:run` (ou `./mvnw spring-boot:run`) para iniciar o serviço na porta `8080`.

---

## 7 — Exemplo de prompt interno (um perfil)

```text
Crie um retrato fotográfico realista (1024×1024) de mulher hispânica, 26 anos.
Nome: Valentina Rodriguez.
Traços: pele oliva clara, cabelo castanho escuro liso até o ombro, sorriso caloroso.
Personalidade (ENFP): extrovertida, curiosa, expressiva.
Profissão/hobby: jornalista e fotógrafa de viagem (câmera sutilmente visível).
Cenário: parede terracota em fundo desfocado.
Iluminação: luz natural suave (golden hour).
Roupa: suéter mostarda simples.
Estilo: fotografia digital, nitidez moderada, cores naturais, sem marca d’água.
Arquivo de saída: 97da7cbd-cd8b-4629-a95c-5b157a3464b5.jpg
```

