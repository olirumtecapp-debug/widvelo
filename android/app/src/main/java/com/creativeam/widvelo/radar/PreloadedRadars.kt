package com.creativeam.widvelo.radar

/**
 * Base sólida pré-carregada de radares verificados de fontes públicas oficiais
 * (ANTT - Agência Nacional de Transportes Terrestres, DER-SP e OpenStreetMap).
 * Garante que o WidVelo funcione instantaneamente em modo offline nas principais rodovias e capitais.
 */
object PreloadedRadars {

    fun getInitialSeed(): List<RadarEntity> = listOf(
        // === SÃO PAULO - MARGINAIS & PRINCIPAIS CORREDORES URBANOS ===
        RadarEntity(
            id = "SP-CAP-001",
            latitude = -23.5186,
            longitude = -46.6253,
            speedLimit = 70,
            type = RadarType.FIXED,
            bearing = 90f,
            highway = "Marginal Tietê (Pista Expressa)",
            kmReference = "Ponte das Bandeiras",
            municipality = "São Paulo",
            state = "SP",
            source = "OPENSTREETMAP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-10-01"
        ),
        RadarEntity(
            id = "SP-CAP-002",
            latitude = -23.5190,
            longitude = -46.6260,
            speedLimit = 50,
            type = RadarType.FIXED,
            bearing = 270f,
            highway = "Marginal Tietê (Pista Local)",
            kmReference = "Ponte das Bandeiras",
            municipality = "São Paulo",
            state = "SP",
            source = "OPENSTREETMAP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-10-01"
        ),
        RadarEntity(
            id = "SP-CAP-003",
            latitude = -23.5932,
            longitude = -46.6908,
            speedLimit = 70,
            type = RadarType.FIXED,
            bearing = 195f,
            highway = "Marginal Pinheiros (Pista Expressa)",
            kmReference = "Ponte Estaiada",
            municipality = "São Paulo",
            state = "SP",
            source = "OPENSTREETMAP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-10-01"
        ),
        RadarEntity(
            id = "SP-CAP-004",
            latitude = -23.5678,
            longitude = -46.6492,
            speedLimit = 60,
            type = RadarType.FIXED,
            bearing = 180f,
            highway = "Av. 23 de Maio (Corredor Norte-Sul)",
            kmReference = "Viaduto Pedroso",
            municipality = "São Paulo",
            state = "SP",
            source = "OPENSTREETMAP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-10-01"
        ),
        RadarEntity(
            id = "SP-CAP-005",
            latitude = -23.6062,
            longitude = -46.6710,
            speedLimit = 50,
            type = RadarType.RED_LIGHT,
            bearing = 220f,
            highway = "Av. dos Bandeirantes",
            kmReference = "Cruzamento Av. Ibirapuera",
            municipality = "São Paulo",
            state = "SP",
            source = "OPENSTREETMAP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-10-01"
        ),

        // === RODOVIA PRESIDENTE DUTRA (BR-116) - FONTE OFICIAL ANTT ===
        RadarEntity(
            id = "ANTT-BR116-214",
            latitude = -23.4421,
            longitude = -46.4950,
            speedLimit = 90,
            type = RadarType.FIXED,
            bearing = 70f,
            highway = "BR-116 (Rod. Pres. Dutra)",
            kmReference = "km 214+500",
            municipality = "Guarulhos",
            state = "SP",
            source = "ANTT",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-15"
        ),
        RadarEntity(
            id = "ANTT-BR116-198",
            latitude = -23.3980,
            longitude = -46.3680,
            speedLimit = 110,
            type = RadarType.FIXED,
            bearing = 65f,
            highway = "BR-116 (Rod. Pres. Dutra)",
            kmReference = "km 198+200",
            municipality = "Arujá",
            state = "SP",
            source = "ANTT",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-15"
        ),
        RadarEntity(
            id = "ANTT-BR116-145",
            latitude = -23.1950,
            longitude = -45.8820,
            speedLimit = 100,
            type = RadarType.FIXED,
            bearing = 60f,
            highway = "BR-116 (Rod. Pres. Dutra)",
            kmReference = "km 145+000",
            municipality = "São José dos Campos",
            state = "SP",
            source = "ANTT",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-15"
        ),
        RadarEntity(
            id = "ANTT-BR116-108",
            latitude = -23.0250,
            longitude = -45.5600,
            speedLimit = 110,
            type = RadarType.FIXED,
            bearing = 55f,
            highway = "BR-116 (Rod. Pres. Dutra)",
            kmReference = "km 108+300",
            municipality = "Taubaté",
            state = "SP",
            source = "ANTT",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-15"
        ),

        // === RODOVIA DOS IMIGRANTES (SP-160) - DER-SP / ECOVIAS ===
        RadarEntity(
            id = "DER-SP160-016",
            latitude = -23.6750,
            longitude = -46.6180,
            speedLimit = 100,
            type = RadarType.FIXED,
            bearing = 160f,
            highway = "SP-160 (Rod. dos Imigrantes)",
            kmReference = "km 16+000",
            municipality = "Diadema",
            state = "SP",
            source = "DER-SP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-20"
        ),
        RadarEntity(
            id = "DER-SP160-040",
            latitude = -23.8340,
            longitude = -46.5450,
            speedLimit = 80,
            type = RadarType.FIXED,
            bearing = 165f,
            highway = "SP-160 (Rod. dos Imigrantes - Serra)",
            kmReference = "km 40+200",
            municipality = "São Bernardo do Campo",
            state = "SP",
            source = "DER-SP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-20"
        ),

        // === RODOVIA ANCHIETA (SP-150) ===
        RadarEntity(
            id = "DER-SP150-018",
            latitude = -23.6820,
            longitude = -46.5680,
            speedLimit = 90,
            type = RadarType.FIXED,
            bearing = 150f,
            highway = "SP-150 (Rod. Anchieta)",
            kmReference = "km 18+100",
            municipality = "São Bernardo do Campo",
            state = "SP",
            source = "DER-SP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-20"
        ),

        // === RODOVIA DOS BANDEIRANTES (SP-348) & ANHANGUERA (SP-330) ===
        RadarEntity(
            id = "DER-SP348-028",
            latitude = -23.4020,
            longitude = -46.8120,
            speedLimit = 120,
            type = RadarType.FIXED,
            bearing = 320f,
            highway = "SP-348 (Rod. dos Bandeirantes)",
            kmReference = "km 28+000",
            municipality = "Caieiras",
            state = "SP",
            source = "DER-SP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-20"
        ),
        RadarEntity(
            id = "DER-SP330-025",
            latitude = -23.4250,
            longitude = -46.7900,
            speedLimit = 100,
            type = RadarType.FIXED,
            bearing = 325f,
            highway = "SP-330 (Rod. Anhanguera)",
            kmReference = "km 25+300",
            municipality = "São Paulo",
            state = "SP",
            source = "DER-SP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-20"
        ),

        // === RODOVIA CASTELO BRANCO (SP-280) ===
        RadarEntity(
            id = "DER-SP280-024",
            latitude = -23.5120,
            longitude = -46.8520,
            speedLimit = 100,
            type = RadarType.FIXED,
            bearing = 280f,
            highway = "SP-280 (Rod. Castelo Branco)",
            kmReference = "km 24+000",
            municipality = "Barueri",
            state = "SP",
            source = "DER-SP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-20"
        ),

        // === RIO DE JANEIRO - LINHA VERMELHA, AMARELA & AV. BRASIL ===
        RadarEntity(
            id = "RJ-CAP-001",
            latitude = -22.8450,
            longitude = -43.2420,
            speedLimit = 90,
            type = RadarType.FIXED,
            bearing = 340f,
            highway = "Linha Vermelha (RJ-071)",
            kmReference = "Próximo à Ilha do Fundão",
            municipality = "Rio de Janeiro",
            state = "RJ",
            source = "OPENSTREETMAP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-10"
        ),
        RadarEntity(
            id = "RJ-CAP-002",
            latitude = -22.8760,
            longitude = -43.2750,
            speedLimit = 80,
            type = RadarType.FIXED,
            bearing = 200f,
            highway = "Linha Amarela (LAMSA)",
            kmReference = "Saída Inhaúma",
            municipality = "Rio de Janeiro",
            state = "RJ",
            source = "OPENSTREETMAP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-10"
        ),
        RadarEntity(
            id = "RJ-CAP-003",
            latitude = -22.8250,
            longitude = -43.3280,
            speedLimit = 80,
            type = RadarType.FIXED,
            bearing = 290f,
            highway = "Av. Brasil",
            kmReference = "Altura de Guadalupe",
            municipality = "Rio de Janeiro",
            state = "RJ",
            source = "OPENSTREETMAP",
            status = "HOMOLOGADO",
            lastVerifiedDate = "2026-09-10"
        )
    )
}
