# 4_auto - Автотесты для формы доставки карты

[![Java CI with Gradle](https://github.com/tsarevkostya03-maker/4_auto/actions/workflows/gradle.yml/badge.svg)](https://github.com/tsarevkostya03-maker/4_auto/actions/workflows/gradle.yml)

## Описание
Автотесты для формы заказа доставки карты с использованием Selenide и JUnit 5.

## Запуск тестов
```bash
./gradlew clean test

## 2. Убрать лишнюю паузу (sleep) из gradle.yml

Отредактируйте `.github/workflows/gradle.yml`:

```bash
cat > .github/workflows/gradle.yml << 'EOF'
name: Java CI with Gradle

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Start SUT
      run: |
        java -jar ./artifacts/app-card-delivery.jar &
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Test with Gradle
      run: ./gradlew test --info -Dselenide.headless=true
