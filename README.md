# Gerador de Imagens de Perfil

Este projeto gera retratos realistas para cada perfil definido em `src/main/resources/profile.json`.
As imagens são produzidas pela API da OpenAI e gravadas em `src/main/resources/static/images`.

## Requisitos

- **Java 17** ou superior
- **Maven 3**
- Chave da API da OpenAI

## Configuração

1. Copie `.env.exemplo` para `.env` e preencha `OPENAI_API_KEY` com sua chave.
   - Se você não tiver um arquivo `.env`, o aplicativo criará um automaticamente na primeira execução.
   - Você precisará editar este arquivo para adicionar sua chave de API da OpenAI.
2. Opcionalmente, defina `OPENAI_BASE_URL` para alterar a URL da API.

### Obtendo uma chave de API da OpenAI

1. Acesse [OpenAI API Keys](https://platform.openai.com/api-keys)
2. Faça login ou crie uma conta
3. Crie uma nova chave de API
4. Copie a chave e adicione-a ao seu arquivo `.env`

### Solução de problemas

Se você encontrar um erro "401 Unauthorized" ao executar o aplicativo, verifique:

1. Se o arquivo `.env` existe na raiz do projeto
2. Se a variável `OPENAI_API_KEY` está definida corretamente no arquivo `.env`
3. Se a chave da API é válida e não expirou
4. Se sua conta da OpenAI tem créditos suficientes

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
