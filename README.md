# Gerador de Imagens de Perfil

Este projeto gera retratos realistas para cada perfil definido em `src/main/resources/profile.json`.
As imagens são produzidas pela API da OpenAI e gravadas em `src/main/resources/static/images`.

## Requisitos

- **Java 17** ou superior
- **Maven 3**
- Chave da API da OpenAI

## Configuração

1. Copie `.env.exemplo` para `.env` e preencha `OPENAI_API_KEY` com sua chave.
2. Opcionalmente, defina `OPENAI_BASE_URL` para alterar a URL da API.

## Execução

Para gerar as imagens e o arquivo `profiles_with_images.json`:

```bash
./mvnw spring-boot:run
```

O comando acima carrega os perfis, solicita as imagens à OpenAI e salva o resultado no diretório `static/images`.

## Testes

Execute todos os testes com:

```bash
./mvnw test
```

## Exemplo de requisição HTTP

O arquivo `openai_requests.http` contém um exemplo de chamada à API de geração de imagens usando as variáveis configuradas.

