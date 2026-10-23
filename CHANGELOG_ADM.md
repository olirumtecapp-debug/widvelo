# 🛡️ Livro de Registro & Auditoria Administrativa — WidVelo

> [!IMPORTANT]
> **Diretriz de Engenharia ("Sem Remendos"):**  
> 1. Proibido qualquer tipo de "remendo", gambiarra ou código fictício. Toda funcionalidade deve ser real, testada e modular.  
> 2. Toda e qualquer modificação, adição ou correção DEVE obrigatoriamente ser registrada neste documento e no Painel Administrativo interno do aplicativo (`WidVeloAdminScreen`).  
> 3. Cada ciclo de trabalho possui um Ponto de Restauração identificado para retorno seguro imediato.

---

## 📌 Linha do Tempo de Pontos de Restauração & Mudanças

### [PONTO DE RESTAURAÇÃO #001] — Fundação Arquitetural, Repositório & Infraestrutura Base
- **Data/Hora:** 23/10/2026 - 06h28
- **Versão:** `v1.0.0` (Build `1`)
- **Objetivo da Etapa:**
  - Criação da árvore de pastas seguindo Clean Architecture: `location`, `radar`, `speedlimit`, `overspeed`, `widget`, `data`, `ui`, `ui/theme`.
  - Configuração do Gradle 8.2.2 + Kotlin 1.9.22 + KSP 1.9.22 + Java 17.
  - Inclusão de Jetpack Compose BOM (Material 3), Room Database, FusedLocationProvider e Coroutines.
  - Keystore fixa reprodutível (`widvelo-fixed.keystore`).
  - Pipeline de CI no GitHub Actions (`.github/workflows/build-apk.yml`) para compilação, execução de testes unitários e geração de APK assinado em cada push.
  - Importação dos logotipos oficiais da marca WidVelo em `res/drawable`.
  - Criação do Livro de Auditoria e Changelog Administrativo (`CHANGELOG_ADM.md`).
- **Arquivos Criados/Afetados:**
  - `android/build.gradle`
  - `android/settings.gradle`
  - `android/gradle.properties`
  - `android/app/build.gradle`
  - `android/app/widvelo-fixed.keystore`
  - `.github/workflows/build-apk.yml`
  - `CHANGELOG_ADM.md`
- **Status da Etapa:** ✅ Concluída com sucesso.

---

### [PONTO DE RESTAURAÇÃO #002] — Motor de Telemetria GPS & Filtro Anti-Oscilação (Zero-Lock)
- **Data/Hora:** 23/10/2026 - 06h35
- **Versão:** `v1.0.0` (Build `1`)
- **Objetivo da Etapa:**
  - Desenvolver o motor de filtragem de velocidade GPS (`GpsSpeedFilter.kt`) com Média Móvel Exponencial adaptativa.
  - Implementar Trava de Imobilidade (`Zero-Lock`) para zerar a velocidade rigorosamente quando abaixo de 1.5 km/h, eliminando ruído quando parado no semáforo.
  - Criar avaliador de qualidade de satélites (`GpsSignalQuality.kt`) com 4 níveis (Excelente, Boa, Moderada, Ruim) baseado em `accuracy` em metros.
  - Criar `LocationTracker.kt` usando `FusedLocationProviderClient` de alta precisão com fallback automático para `LocationManager` nativo do Android (100% offline).
  - Criar `WidVeloLocationService.kt` como Foreground Service do Android com notificação contínua e ações de início/parada.
- **Arquivos Criados/Afetados:**
  - `android/app/src/main/java/com/creativeam/widvelo/location/GpsSpeedFilter.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/location/GpsSignalQuality.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/location/VehicleTelemetry.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/location/SpeedUnit.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/location/LocationTracker.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/location/WidVeloLocationService.kt`
- **Status da Etapa:** ✅ Concluída com sucesso.

---

### [PONTO DE RESTAURAÇÃO #003] — Banco Room de Radares & Filtro Angular de Sentido (ANTT / DER / OSM)
- **Data/Hora:** 23/10/2026 - 06h42
- **Versão:** `v1.0.0` (Build `1`)
- **Objetivo da Etapa:**
  - Desenvolver a camada de persistência Room com a tabela `radars` indexada espacialmente por latitude/longitude.
  - Implementar `PreloadedRadars.kt` com sementes homologadas da ANTT (BR-116 Dutra, BR-101, BR-381 Fernão Dias, BR-040, etc.), DER-SP (Marginais Pinheiros/Tietê, Castello Branco, Ayrton Senna, Bandeirantes) e OpenStreetMap.
  - Criar o motor espacial `RadarDetector.kt` que calcula distância geodésica Haversine e aplica cone angular de ±35° baseado no azimute do veículo e sentido de fiscalização do radar, eliminando falsos alertas em pistas contrárias e vias paralelas.
- **Arquivos Criados/Afetados:**
  - `android/app/src/main/java/com/creativeam/widvelo/radar/RadarType.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/RadarEntity.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/RadarDao.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/RadarAlert.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/RadarDetector.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/PreloadedRadars.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/WidVeloDatabase.kt`
- **Status da Etapa:** ✅ Concluída com sucesso.

---

### [PONTO DE RESTAURAÇÃO #004] — Reconhecimento de Limites de Via & Alertas de Excesso de Velocidade
- **Data/Hora:** 23/10/2026 - 06h48
- **Versão:** `v1.0.0` (Build `1`)
- **Objetivo da Etapa:**
  - Implementar o motor `SpeedLimitEngine.kt` para determinar o limite real do trecho via corredores de radares homologados com regra mandatória de integridade: nunca inventar limites; se não houver certeza, exibir "Limite não disponível".
  - Implementar `OverspeedEvaluator.kt` comparando a velocidade com o limite oficial da via e margem de tolerância do condutor (+0, +3, +5, +10 km/h).
  - Implementar `AudioAlerter.kt` com gerador sonoro nativo (`ToneGenerator`) com latência zero, cooldown anti-repetição e vibração háptica.
- **Arquivos Criados/Afetados:**
  - `android/app/src/main/java/com/creativeam/widvelo/speedlimit/RoadSpeedLimit.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/speedlimit/SpeedLimitEngine.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/overspeed/OverspeedEvaluator.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/overspeed/AudioAlerter.kt`
- **Status da Etapa:** ✅ Concluída com sucesso.

---

### [PONTO DE RESTAURAÇÃO #005] — Widget Flutuante Arrastável sobre Navegadores & Telas da Aplicação
- **Data/Hora:** 23/10/2026 - 06h54
- **Versão:** `v1.0.0` (Build `1`)
- **Objetivo da Etapa:**
  - Construir o widget flutuante real via `WindowManager` (`TYPE_APPLICATION_OVERLAY`) para exibição ininterrupta sobre Waze e Google Maps.
  - Implementar física de arrasto livre (TouchListener) e persistência automática de coordenadas em SharedPreferences.
  - Exibir visualmente: Velocidade em destaque, Limite oficial da via, Distância do radar e borda dinâmica em caso de excesso.
  - Construir a interface completa em Jetpack Compose:
    - `MainSpeedometerScreen.kt`: Mostrador gigante digital, barra de satélites, badge de radar e limite.
    - `WidVeloAdminScreen.kt`: Tela interna de auditoria, diagnóstico de sensores e changelog oficial.
    - `WidVeloSettingsScreen.kt`: Ajustes de tolerância, unidade de medida, alertas sonoros e tema.
    - `RadarListScreen.kt`: Lista e busca de radares georreferenciados cadastrados na base.
    - `MainActivity.kt`: Orquestração de permissões de GPS e Overlay.
- **Arquivos Criados/Afetados:**
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidgetState.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidVeloWidget.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidVeloService.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/MainSpeedometerScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/WidVeloAdminScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/WidVeloSettingsScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/RadarListScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/theme/WidVeloTheme.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/MainActivity.kt`
- **Status da Etapa:** ✅ Concluída com sucesso.

---

### [PONTO DE RESTAURAÇÃO #006] — Android Manifest, Application Class, Recursos XML & Suíte de Testes
- **Data/Hora:** 23/10/2026 - 07h00
- **Versão:** `v1.0.0` (Build `1`)
- **Objetivo da Etapa:**
  - Criação da classe `WidVeloApplication.kt` com inicialização assíncrona do banco Room.
  - Configuração completa do `AndroidManifest.xml` com serviços de localização e permissão de sobreposição de janela.
  - Criação dos recursos XML (`strings.xml`, `colors.xml`, `themes.xml`) e `gradle-wrapper.properties`.
  - Construção da suíte de testes unitários automatizados:
    - `GpsSpeedFilterTest.kt`: Validação de conversão m/s para km/h, trava de repouso (Zero-Lock) e amortecimento EMA.
    - `RadarDetectorTest.kt`: Validação de distância geodésica, rejeição de radares em sentido oposto e detecção na Dutra.
    - `OverspeedEvaluatorTest.kt`: Validação de limites normais, margem de tolerância e supressão de falsos alertas quando o limite não está disponível.
- **Arquivos Criados/Afetados:**
  - `android/app/src/main/java/com/creativeam/widvelo/WidVeloApplication.kt`
  - `android/app/src/main/AndroidManifest.xml`
  - `android/app/src/main/res/values/strings.xml`
  - `android/app/src/main/res/values/colors.xml`
  - `android/app/src/main/res/values/themes.xml`
  - `android/gradle/wrapper/gradle-wrapper.properties`
  - `android/app/src/test/java/com/creativeam/widvelo/GpsSpeedFilterTest.kt`
  - `android/app/src/test/java/com/creativeam/widvelo/RadarDetectorTest.kt`
  - `android/app/src/test/java/com/creativeam/widvelo/OverspeedEvaluatorTest.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/WidVeloAdminScreen.kt`
  - `CHANGELOG_ADM.md`
- **Status da Etapa:** ✅ Concluída com sucesso.

---

### [PONTO DE RESTAURAÇÃO #007] — Detector de Semáforos Urbanos & Temporizador de Ciclo de Parada
- **Data/Hora:** 23/10/2026 - 07h15
- **Versão:** `v1.0.0` (Build `1`)
- **Objetivo da Etapa:**
  - Criação da entidade `TrafficLightEntity.kt` e DAO `TrafficLightDao.kt` no banco Room (versão 2).
  - Pré-carregamento de sementes homologadas de cruzamentos semafóricos de capitais brasileiras (`PreloadedTrafficLights.kt`) a partir de dados abertos do OpenStreetMap (`highway=traffic_signals`) e CET.
  - Implementação do motor espacial `TrafficLightDetector.kt` com distância Haversine (< 250m) e filtro angular de sentido de fluxo (±45°) para descartar vias perpendiculares.
  - Temporizador de cruzamento automático: ao detectar imobilização do veículo (Zero-Lock) a menos de 65m do semáforo, aciona contagem regressiva baseada no ciclo regular da via.
  - Atualização do Widget Flutuante (`FloatingWidVeloWidget.kt` e `FloatingWidVeloService.kt`) exibindo badge de semáforo e contagem regressiva em tempo real sobre o Waze/Maps.
  - Atualização do painel principal (`MainSpeedometerScreen.kt` e `MainActivity.kt`) com Card dinâmico de semáforo e barra de progresso linear do ciclo.
  - Criação da suíte de testes unitários `TrafficLightDetectorTest.kt` validando distância, rejeição perpendicular e engajamento do temporizador.
- **Arquivos Criados/Afetados:**
  - `android/app/src/main/java/com/creativeam/widvelo/trafficlight/TrafficLightEntity.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/trafficlight/TrafficLightDao.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/trafficlight/PreloadedTrafficLights.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/trafficlight/TrafficLightAlert.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/trafficlight/TrafficLightDetector.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/WidVeloDatabase.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidgetState.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidVeloWidget.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidVeloService.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/MainSpeedometerScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/MainActivity.kt`
  - `android/app/src/test/java/com/creativeam/widvelo/TrafficLightDetectorTest.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/WidVeloAdminScreen.kt`
  - `CHANGELOG_ADM.md`
- **Status da Etapa:** ✅ Concluída com sucesso.

---
