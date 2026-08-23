# Guia de Setup, Validação Local e Qualidade (CI)

## 1. Ambiente e Toolchain

- **JDK**: Java 21 (recomendado para sincronização com Gradle e Android Gradle Plugin do projeto).
- **Gradle**: Utilizar exclusivamente o Gradle Wrapper incluído no repositório (`./gradlew`).

## 2. Matriz Mínima de Testes e Validação

Antes de enviar qualquer alteração, os seguintes comandos devem ser executados e aprovados localmente:

### 2.1 Testes Unitários (Lite e Full)

```bash
./gradlew testLiteDebugUnitTest testFullDebugUnitTest
```

### 2.2 Análise Estática / Lint

```bash
./gradlew lintLiteDebug
```

### 2.3 Compilação de Debug

```bash
./gradlew assembleLiteDebug assembleFullDebug
```

## 3. Checklist Mínimo para Pull Requests (PRs)

- [ ] **Testes unitários**: `testLiteDebugUnitTest` e `testFullDebugUnitTest` executados sem falhas.
- [ ] **Análise de Lint**: `lintLiteDebug` executado sem erros ou advertências impeditivas.
- [ ] **Compatibilidade Lite/Full**: Verificado que novos conteúdos e skins funcionam corretamente tanto no flavor `lite` quanto no `full`.
- [ ] **Acessibilidade básica**: Componentes de UI novos incluem rótulos (`contentDescription`), áreas de toque adequadas e contraste.
- [ ] **Artefatos e código fonte**: Nenhuma edição direta em diretórios de build/artefatos gerados.
