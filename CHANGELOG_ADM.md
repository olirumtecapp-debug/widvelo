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

### [PONTO DE RESTAURAÇÃO #008] — Widget Redondo Waze, Radares de SBC & Refinamento Visual do App
- **Data/Hora:** 23/10/2026 - 15h30
- **Versão:** `v1.0.1` (Build `2`)
- **Objetivo da Etapa:**
  - **Widget Flutuante Estilo Waze:** Redesign completo substituindo o antigo card retangular escuro por um cluster circular elegante:
    - Velocímetro redondo branco (`#FFFFFF`) com dígitos pretos sólidos e nítidos.
    - Em excesso de velocidade: velocímetro transmuta para **VERMELHO VIVO** (`#EF4444`) com dígitos brancos.
    - Placa R-19 circular de limite de velocidade: em excesso de velocidade, **fica maiorzinha** (expande de 38dp para 48dp com borda vermelha 4dp) exatamente como no Waze!
    - Botão de fechar circular `✕` integrado para encerrar o widget com 1 toque direto na tela.
    - Mini-cápsula inferior elegante para alertas de radar (`⚠️ RADAR 350m`) e semáforos.
  - **Base de Radares e Semáforos de São Bernardo do Campo & Grande ABC:**
    - Inclusão de 35+ radares municipais e estaduais com limites e sentidos reais (Av. Pereira Barreto, Faria Lima, Senador Vergueiro, Kennedy, Lucas Nogueira Garcez, Lions, Piraporinha, Alvarengas, Cooperativa/Makita/OGGI, Jurubatuba, Rotary, Caminho do Mar, Anchieta SP-150 km 10 ao 29, Imigrantes SP-160 e Rodoanel Sul).
    - Inclusão de semáforos com temporizadores nos principais cruzamentos de SBC.
  - **Garantia de Persistência & Resolução de Limites:**
    - Atualização da Room Database para versão 3 com método `ensureSeeded()` que garante que as tabelas nunca fiquem vazias após instalação ou limpeza de dados.
    - Motor de limite de velocidade (`SpeedLimitEngine.kt`) com resolução hierárquica inteligente: radar -> corredor -> rodovia -> padrão urbano (50/60 km/h), eliminando o problema de velocidade "Não disponível".
  - **Refinamento Visual e Tipográfico ("Sem Textos Quebrados"):**
    - Correção de todas as colunas unweighted em `MainSpeedometerScreen.kt`, `RadarListScreen.kt` e `WidVeloSettingsScreen.kt`.
    - Adicionado `Modifier.weight(1f)` e `TextOverflow.Ellipsis` para nomes longos de vias e rodovias.
    - Alinhamento vertical e harmonia de proporções em telas de diferentes resoluções.
- **Arquivos Criados/Afetados:**
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidVeloWidget.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidVeloService.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/PreloadedRadars.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/trafficlight/PreloadedTrafficLights.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/WidVeloDatabase.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/speedlimit/SpeedLimitEngine.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/MainSpeedometerScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/RadarListScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/WidVeloSettingsScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/WidVeloAdminScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/MainActivity.kt`
  - `android/app/build.gradle`
  - `CHANGELOG_ADM.md`
- **Status da Etapa:** ✅ Concluída com sucesso.

---

### [PONTO DE RESTAURAÇÃO #009] — Cobertura SBC (Vianas, Ítalo Setti), Alertas de Obras/Interdições & Sincronizador Territorial OSM
- **Data/Hora:** 23/10/2026 - 15h50
- **Versão:** `v1.0.2` (Build `3`)
- **Objetivo da Etapa:**
  - **Módulo de Vias Interditadas, Obras e Bloqueios:**
    - Criação das entidades `RoadIncidentEntity.kt` e `RoadIncidentDao.kt` no banco Room (versão 4).
    - Criação do motor geodésico `RoadIncidentDetector.kt` que avalia interdições à frente em raio de até 800m.
    - Exibição de alertas cirúrgicos na mini-cápsula do Widget Flutuante estilo Waze (`🚧 Obra a 350m: R. dos Vianas` ou `⛔ Via Interditada a 400m`) sem necessidade de carregar mapas pesados.
    - Card dinâmico de incidentes integrado em `MainSpeedometerScreen.kt`.
  - **Cobertura Específica da Rua dos Vianas e Av. Ítalo Setti:**
    - Adicionados radares e semáforos reais com sentidos e tempos de ciclo na Rua dos Vianas (Baeta Neves, Tognato, Thales Freire) e na Av. Ítalo Setti (Jurubatuba, Marechal Deodoro, Paço / Faria Lima).
  - **Motor de Sincronização Territorial (CitySyncManager):**
    - Desenvolvido o módulo de sincronização com a Overpass API do OpenStreetMap, permitindo baixar todos os semáforos e câmeras de São Bernardo do Campo com 1 toque no painel ADM e expandir para qualquer outra cidade do Brasil.
- **Arquivos Criados/Afetados:**
  - `android/app/src/main/java/com/creativeam/widvelo/incident/RoadIncidentType.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/incident/RoadIncidentEntity.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/incident/RoadIncidentDao.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/incident/RoadIncidentAlert.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/incident/PreloadedIncidents.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/incident/RoadIncidentDetector.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/sync/CitySyncManager.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/PreloadedRadars.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/trafficlight/PreloadedTrafficLights.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/radar/WidVeloDatabase.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidgetState.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidVeloWidget.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidVeloService.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/MainSpeedometerScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/ui/WidVeloAdminScreen.kt`
  - `android/app/src/main/java/com/creativeam/widvelo/MainActivity.kt`
  - `android/app/build.gradle`
  - `CHANGELOG_ADM.md`
- **Status da Etapa:** ✅ Concluída com sucesso.

---

### [PONTO DE RESTAURAÇÃO #010] — Sistema de Atualização Automática no App (OTA) com Changelog de Melhorias
- **Data/Hora:** 23/10/2026 - 16h15
- **Versão:** 1.0.3 (Build 4)
- **Objetivo da Etapa:**
  - Implementação completa do sistema de atualização Over-The-Air (OTA) com 1 toque no WidVelo, espelhando a experiência do GoOrNo.
  - Criação do manifesto remoto de versão ersion.json hospedado no repositório GitHub com changelog oficial de melhorias, tamanho do pacote e URLs diretas do APK.
  - Desenvolvimento do subsistema AppUpdateManager.kt e AppUpdateModel.kt para checagem assíncrona com timeout e streaming com barra de progresso em tempo real.
  - Implementação do diálogo modal UpdateDialog.kt em Jetpack Compose exibindo badge da nova versão, data de lançamento, lista detalhada de novidades (changelog) e botões de ação ("Atualizar Agora" e "Lembrar Mais Tarde").
  - Configuração do provedor seguro FileProvider (es/xml/file_paths.xml) e permissão REQUEST_INSTALL_PACKAGES no AndroidManifest.xml para acionamento direto do instalador nativo do Android (pplication/vnd.android.package-archive).
  - Integração da checagem automática na abertura do aplicativo (MainActivity.kt) e botões manuais de verificação no painel de configurações (WidVeloSettingsScreen.kt) e auditoria (WidVeloAdminScreen.kt).
- **Arquivos Criados/Afetados:**
  - ersion.json
  - ndroid/app/src/main/res/xml/file_paths.xml
  - ndroid/app/src/main/java/com/creativeam/widvelo/update/AppUpdateModel.kt
  - ndroid/app/src/main/java/com/creativeam/widvelo/update/AppUpdateManager.kt
  - ndroid/app/src/main/java/com/creativeam/widvelo/update/UpdateDialog.kt
  - ndroid/app/src/main/AndroidManifest.xml
  - ndroid/app/src/main/java/com/creativeam/widvelo/MainActivity.kt
  - ndroid/app/src/main/java/com/creativeam/widvelo/ui/WidVeloSettingsScreen.kt
  - ndroid/app/src/main/java/com/creativeam/widvelo/ui/WidVeloAdminScreen.kt
  - ndroid/app/build.gradle (v1.0.3 Build 4)
  - CHANGELOG_ADM.md
- **Status da Etapa:** ✅ Concluída com sucesso.

---
### [PONTO DE RESTAURAÇÃO #011] — Seção "Sobre o Aplicativo" (Dados de Versão, Build, Dispositivo & SO)
- **Data/Hora:** 23/10/2026 - 16h30
- **Versão:** 1.0.4 (Build 5)
- **Objetivo da Etapa:**
  - Criação da seção dedicada "Sobre o Aplicativo" dentro da tela de Configurações (WidVeloSettingsScreen.kt).
  - Apresentação visual e dinâmica dos dados técnicos da aplicação:
    - Versão oficial do App (BuildConfig.VERSION_NAME -> 1.0.4)
    - Código da Build (BuildConfig.VERSION_CODE -> 5)
    - Sistema Operacional (Android  (API ))
    - Dispositivo e Modelo do Smartphone do usuário
    - Versão do Banco de Dados local (Room SQLite v4)
    - Canal de distribuição e entrega (OTA Oficial GitHub Direct)
    - Informações de autoria e direitos reservados (CreativeAM / OlirumTec).
- **Arquivos Criados/Afetados:**
  - ndroid/app/src/main/java/com/creativeam/widvelo/ui/WidVeloSettingsScreen.kt
  - ndroid/app/build.gradle (v1.0.4 Build 5)
  - ersion.json
  - CHANGELOG_ADM.md
- **Status da Etapa:** ✅ Concluída com sucesso.

---

### [PONTO DE RESTAURAÇÃO #012] — Widget Redondo Waze Fiel, Botão Fechar Corrigido & Simulador de Alertas (Radares & Faróis)
- **Data/Hora:** 23/10/2026 - 17h25
- **Versão:** v1.0.5 (Build 6)
- **Objetivo da Etapa:**
  - Redesenho completo do Widget Flutuante no padrão idêntico ao Waze:
    - Fundo escuro circular (#1F242D) com aro sutil em velocidade normal, limpo e sem distrações.
    - Em velocidade normal: placa de limite recolhida ("nada acontece").
    - Em excesso de velocidade: o velocímetro fica VERMELHO vivo (#EF4444) e a placa R-19 (limite) surge ao lado.
    - Correção do botão de fechar: substituição do caractere corrompido (âœ) por um botão circular discreto com unicode \u2715 posicionado no canto superior direito do velocímetro.
  - Implementação do Modo Demonstração & Teste de Alertas (Simulador de Rota em SBC):
    - Permite ao usuário testar instantaneamente na tela do celular como funcionam os radares, semáforos/faróis parados, excesso de velocidade e obras, com 1 toque no botão "🧪 Testar Alertas".
  - Seção explicativa em Configurações ensinando a lógica de detecção de radares e faróis.
- **Arquivos Criados/Afetados:**
  - android/app/src/main/java/com/creativeam/widvelo/widget/FloatingWidVeloWidget.kt
  - android/app/src/main/java/com/creativeam/widvelo/location/WidVeloLocationService.kt
  - android/app/src/main/java/com/creativeam/widvelo/ui/MainSpeedometerScreen.kt
  - android/app/src/main/java/com/creativeam/widvelo/ui/WidVeloSettingsScreen.kt
  - android/app/src/main/java/com/creativeam/widvelo/MainActivity.kt
  - android/app/build.gradle (v1.0.5 Build 6)
  - version.json
  - CHANGELOG_ADM.md
- **Status da Etapa:** ✅ Concluída com sucesso.

---
