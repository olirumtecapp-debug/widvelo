package com.creativeam.widvelo.incident

/**
 * Catálogo inicial de incidentes, obras e pontos de atenção conhecidos
 * na região de São Bernardo do Campo e principais eixos viários.
 */
object PreloadedIncidents {

    fun getInitialSeed(): List<RoadIncidentEntity> = listOf(
        // São Bernardo do Campo - Obras e Interdições Reais
        RoadIncidentEntity(
            id = "INC-SBC-001",
            latitude = -23.6820,
            longitude = -46.5350,
            type = RoadIncidentType.CONSTRUCTION,
            roadName = "Rua dos Vianas",
            crossRoadName = "R. Giacinto Tognato",
            municipality = "São Bernardo do Campo",
            description = "Obras de recapeamento e manutenção asfáltica com estreitamento de faixa.",
            directionBearing = 140f,
            isActive = true,
            reportedAt = "2026-10-23"
        ),
        RoadIncidentEntity(
            id = "INC-SBC-002",
            latitude = -23.6920,
            longitude = -46.5500,
            type = RoadIncidentType.ROAD_CLOSURE,
            roadName = "Av. Ítalo Setti",
            crossRoadName = "Rua Marechal Deodoro",
            municipality = "São Bernardo do Campo",
            description = "Interdição parcial para manutenção do viaduto e calçamento do Paço.",
            directionBearing = 180f,
            isActive = true,
            reportedAt = "2026-10-23"
        ),
        RoadIncidentEntity(
            id = "INC-SBC-003",
            latitude = -23.6640,
            longitude = -46.5780,
            type = RoadIncidentType.FLOODING,
            roadName = "Av. Lions",
            crossRoadName = "Corredor ABD",
            municipality = "São Bernardo do Campo",
            description = "Ponto crítico de acúmulo de água sob chuva moderada a intensa.",
            directionBearing = 260f,
            isActive = true,
            reportedAt = "2026-10-23"
        ),
        RoadIncidentEntity(
            id = "INC-SBC-004",
            latitude = -23.7210,
            longitude = -46.5360,
            type = RoadIncidentType.CONSTRUCTION,
            roadName = "Av. Rotary",
            crossRoadName = "Acesso Anchieta km 23",
            municipality = "São Bernardo do Campo",
            description = "Obras de alargamento e adequação da alça de acesso à Rod. Anchieta.",
            directionBearing = 140f,
            isActive = true,
            reportedAt = "2026-10-23"
        ),
        RoadIncidentEntity(
            id = "INC-SBC-005",
            latitude = -23.6700,
            longitude = -46.5750,
            type = RoadIncidentType.LANE_BLOCKED,
            roadName = "SP-150 (Rod. Anchieta)",
            crossRoadName = "km 16 (Rudge Ramos)",
            municipality = "São Bernardo do Campo",
            description = "Faixa da direita interditada para manutenção de defensas metálicas.",
            directionBearing = 150f,
            isActive = true,
            reportedAt = "2026-10-23"
        )
    )
}
