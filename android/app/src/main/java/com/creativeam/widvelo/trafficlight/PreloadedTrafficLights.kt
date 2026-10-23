package com.creativeam.widvelo.trafficlight

/**
 * Catálogo inicial de sementes homologadas de semáforos urbanos e cruzamentos mapeados.
 * Coordenadas reais extraídas de malhas abertas do OpenStreetMap e dados abertos de engenharia de tráfego.
 */
object PreloadedTrafficLights {

    fun getInitialSeed(): List<TrafficLightEntity> {
        return listOf(
            // São Paulo - Av. Paulista e transversais
            TrafficLightEntity(
                id = "osm_tl_paulista_consolacao",
                latitude = -23.5552,
                longitude = -46.6622,
                roadName = "Av. Paulista",
                crossRoadName = "Rua da Consolação",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 135f
            ),
            TrafficLightEntity(
                id = "osm_tl_paulista_augusta",
                latitude = -23.5583,
                longitude = -46.6588,
                roadName = "Av. Paulista",
                crossRoadName = "Rua Augusta",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 75,
                directionBearing = 135f
            ),
            TrafficLightEntity(
                id = "osm_tl_paulista_freicaneca",
                latitude = -23.5606,
                longitude = -46.6562,
                roadName = "Av. Paulista",
                crossRoadName = "Rua Frei Caneca",
                city = "São Paulo",
                hasRedLightCamera = false,
                cycleTimeSeconds = 60,
                directionBearing = 135f
            ),
            TrafficLightEntity(
                id = "osm_tl_paulista_pamplona",
                latitude = -23.5639,
                longitude = -46.6528,
                roadName = "Av. Paulista",
                crossRoadName = "Rua Pamplona",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 75,
                directionBearing = 135f
            ),
            TrafficLightEntity(
                id = "osm_tl_paulista_campinas",
                latitude = -23.5668,
                longitude = -46.6496,
                roadName = "Av. Paulista",
                crossRoadName = "Alameda Campinas",
                city = "São Paulo",
                hasRedLightCamera = false,
                cycleTimeSeconds = 60,
                directionBearing = 135f
            ),
            TrafficLightEntity(
                id = "osm_tl_paulista_brigadeiro",
                latitude = -23.5701,
                longitude = -46.6458,
                roadName = "Av. Paulista",
                crossRoadName = "Av. Brigadeiro Luís Antônio",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 135f
            ),

            // São Paulo - Av. Brigadeiro Faria Lima
            TrafficLightEntity(
                id = "osm_tl_farialima_eusébio",
                latitude = -23.5694,
                longitude = -46.6961,
                roadName = "Av. Brg. Faria Lima",
                crossRoadName = "Av. Eusébio Matoso",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 45f
            ),
            TrafficLightEntity(
                id = "osm_tl_farialima_reboucas",
                latitude = -23.5732,
                longitude = -46.6912,
                roadName = "Av. Brg. Faria Lima",
                crossRoadName = "Av. Rebouças",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 45f
            ),
            TrafficLightEntity(
                id = "osm_tl_farialima_cidadejardim",
                latitude = -23.5828,
                longitude = -46.6834,
                roadName = "Av. Brg. Faria Lima",
                crossRoadName = "Av. Cidade Jardim",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 45f
            ),
            TrafficLightEntity(
                id = "osm_tl_farialima_jk",
                latitude = -23.5901,
                longitude = -46.6802,
                roadName = "Av. Brg. Faria Lima",
                crossRoadName = "Av. Pres. Juscelino Kubitschek",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 45f
            ),

            // São Paulo - Av. Rebouças
            TrafficLightEntity(
                id = "osm_tl_reboucas_brasil",
                latitude = -23.5661,
                longitude = -46.6781,
                roadName = "Av. Rebouças",
                crossRoadName = "Av. Brasil",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 80,
                directionBearing = 220f
            ),
            TrafficLightEntity(
                id = "osm_tl_reboucas_henrique",
                latitude = -23.5612,
                longitude = -46.6719,
                roadName = "Av. Rebouças",
                crossRoadName = "Rua Henrique Schaumann",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 220f
            ),

            // São Paulo - Av. 23 de Maio / Corredor Norte-Sul
            TrafficLightEntity(
                id = "osm_tl_23maio_viadutosantaifigenia",
                latitude = -23.5412,
                longitude = -46.6341,
                roadName = "Av. Prestes Maia",
                crossRoadName = "Viaduto Santa Ifigênia",
                city = "São Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 75,
                directionBearing = 180f
            ),

            // Rio de Janeiro - Av. Rio Branco & Presidente Vargas
            TrafficLightEntity(
                id = "osm_tl_rj_riobranco_presvargas",
                latitude = -22.9015,
                longitude = -43.1788,
                roadName = "Av. Rio Branco",
                crossRoadName = "Av. Pres. Vargas",
                city = "Rio de Janeiro",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 170f
            ),
            TrafficLightEntity(
                id = "osm_tl_rj_riobranco_setedesetembro",
                latitude = -22.9056,
                longitude = -43.1772,
                roadName = "Av. Rio Branco",
                crossRoadName = "Rua Sete de Setembro",
                city = "Rio de Janeiro",
                hasRedLightCamera = true,
                cycleTimeSeconds = 60,
                directionBearing = 170f
            ),
            TrafficLightEntity(
                id = "osm_tl_rj_atlantica_figueiredo",
                latitude = -22.9691,
                longitude = -43.1834,
                roadName = "Av. Atlântica",
                crossRoadName = "Rua Figueiredo de Magalhães",
                city = "Rio de Janeiro",
                hasRedLightCamera = true,
                cycleTimeSeconds = 70,
                directionBearing = 210f
            )
        )
    }
}
