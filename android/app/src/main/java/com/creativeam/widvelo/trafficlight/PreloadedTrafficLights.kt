package com.creativeam.widvelo.trafficlight

/**
 * CatÃ¡logo inicial de sementes homologadas de semÃ¡foros urbanos e cruzamentos mapeados.
 * Coordenadas reais extraÃ­das de malhas abertas do OpenStreetMap e dados abertos de engenharia de trÃ¡fego.
 */
object PreloadedTrafficLights {

    fun getInitialSeed(): List<TrafficLightEntity> {
        fun getInitialSeed(): List<TrafficLightEntity> {
        return listOf(
            // === RUA DOS VIANAS & AV. ÍTALO SETTI (SBC) ===
            TrafficLightEntity(
                id = "sbc_tl_vianas_pereira_barreto",
                latitude = -23.6825,
                longitude = -46.5360,
                roadName = "Rua dos Vianas",
                crossRoadName = "Av. Pereira Barreto (Baeta Neves)",
                city = "São Bernardo do Campo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 75,
                directionBearing = 140f
            ),
            TrafficLightEntity(
                id = "sbc_tl_vianas_giacinto_tognato",
                latitude = -23.6800,
                longitude = -46.5320,
                roadName = "Rua dos Vianas",
                crossRoadName = "Rua Giacinto Tognato",
                city = "São Bernardo do Campo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 65,
                directionBearing = 140f
            ),
            TrafficLightEntity(
                id = "sbc_tl_vianas_thales_freire",
                latitude = -23.6765,
                longitude = -46.5295,
                roadName = "Rua dos Vianas",
                crossRoadName = "R. Thales dos Santos Freire",
                city = "São Bernardo do Campo",
                hasRedLightCamera = false,
                cycleTimeSeconds = 60,
                directionBearing = 320f
            ),
            TrafficLightEntity(
                id = "sbc_tl_italo_setti_jurubatuba",
                latitude = -23.6925,
                longitude = -46.5495,
                roadName = "Av. Ítalo Setti",
                crossRoadName = "Av. Jurubatuba",
                city = "São Bernardo do Campo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 80,
                directionBearing = 180f
            ),
            TrafficLightEntity(
                id = "sbc_tl_italo_setti_marechal",
                latitude = -23.6955,
                longitude = -46.5510,
                roadName = "Av. Ítalo Setti",
                crossRoadName = "Rua Marechal Deodoro (Centro)",
                city = "São Bernardo do Campo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 70,
                directionBearing = 180f
            ),
            TrafficLightEntity(
                id = "sbc_tl_italo_setti_faria_lima",
                latitude = -23.6980,
                longitude = -46.5520,
                roadName = "Av. Ítalo Setti",
                crossRoadName = "Av. Brig. Faria Lima (Paço)",
                city = "São Bernardo do Campo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 85,
                directionBearing = 195f
            ),
            // SÃ£o Paulo - Av. Paulista e transversais
            TrafficLightEntity(
                id = "osm_tl_paulista_consolacao",
                latitude = -23.5552,
                longitude = -46.6622,
                roadName = "Av. Paulista",
                crossRoadName = "Rua da ConsolaÃ§Ã£o",
                city = "SÃ£o Paulo",
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
                city = "SÃ£o Paulo",
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
                city = "SÃ£o Paulo",
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
                city = "SÃ£o Paulo",
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
                city = "SÃ£o Paulo",
                hasRedLightCamera = false,
                cycleTimeSeconds = 60,
                directionBearing = 135f
            ),
            TrafficLightEntity(
                id = "osm_tl_paulista_brigadeiro",
                latitude = -23.5701,
                longitude = -46.6458,
                roadName = "Av. Paulista",
                crossRoadName = "Av. Brigadeiro LuÃ­s AntÃ´nio",
                city = "SÃ£o Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 135f
            ),

            // SÃ£o Paulo - Av. Brigadeiro Faria Lima
            TrafficLightEntity(
                id = "osm_tl_farialima_eusÃ©bio",
                latitude = -23.5694,
                longitude = -46.6961,
                roadName = "Av. Brg. Faria Lima",
                crossRoadName = "Av. EusÃ©bio Matoso",
                city = "SÃ£o Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 45f
            ),
            TrafficLightEntity(
                id = "osm_tl_farialima_reboucas",
                latitude = -23.5732,
                longitude = -46.6912,
                roadName = "Av. Brg. Faria Lima",
                crossRoadName = "Av. RebouÃ§as",
                city = "SÃ£o Paulo",
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
                city = "SÃ£o Paulo",
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
                city = "SÃ£o Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 45f
            ),

            // SÃ£o Paulo - Av. RebouÃ§as
            TrafficLightEntity(
                id = "osm_tl_reboucas_brasil",
                latitude = -23.5661,
                longitude = -46.6781,
                roadName = "Av. RebouÃ§as",
                crossRoadName = "Av. Brasil",
                city = "SÃ£o Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 80,
                directionBearing = 220f
            ),
            TrafficLightEntity(
                id = "osm_tl_reboucas_henrique",
                latitude = -23.5612,
                longitude = -46.6719,
                roadName = "Av. RebouÃ§as",
                crossRoadName = "Rua Henrique Schaumann",
                city = "SÃ£o Paulo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 220f
            ),

            // SÃ£o Paulo - Av. 23 de Maio / Corredor Norte-Sul
            TrafficLightEntity(
                id = "osm_tl_23maio_viadutosantaifigenia",
                latitude = -23.5412,
                longitude = -46.6341,
                roadName = "Av. Prestes Maia",
                crossRoadName = "Viaduto Santa IfigÃªnia",
                city = "SÃ£o Paulo",
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
                roadName = "Av. AtlÃ¢ntica",
                crossRoadName = "Rua Figueiredo de MagalhÃ£es",
                city = "Rio de Janeiro",
                hasRedLightCamera = true,
                cycleTimeSeconds = 70,
                directionBearing = 210f
            ),

            // SÃ£o Bernardo do Campo & Grande ABC - Cruzamentos Principais
            TrafficLightEntity(
                id = "sbc_tl_pereira_barreto_metropole",
                latitude = -23.6845,
                longitude = -46.5450,
                roadName = "Av. Pereira Barreto",
                crossRoadName = "R. Kara / Shopping MetrÃ³pole",
                city = "SÃ£o Bernardo do Campo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 80,
                directionBearing = 160f
            ),
            TrafficLightEntity(
                id = "sbc_tl_faria_lima_marechal",
                latitude = -23.7025,
                longitude = -46.5510,
                roadName = "Av. Brig. Faria Lima",
                crossRoadName = "Rua Marechal Deodoro",
                city = "SÃ£o Bernardo do Campo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 75,
                directionBearing = 180f
            ),
            TrafficLightEntity(
                id = "sbc_tl_vergueiro_praca_meninos",
                latitude = -23.6690,
                longitude = -46.5650,
                roadName = "Av. Senador Vergueiro",
                crossRoadName = "Av. Caminho do Mar (PraÃ§a dos Meninos)",
                city = "SÃ£o Bernardo do Campo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 85,
                directionBearing = 145f
            ),
            TrafficLightEntity(
                id = "sbc_tl_kennedy_poliesportivo",
                latitude = -23.6880,
                longitude = -46.5520,
                roadName = "Av. Kennedy",
                crossRoadName = "Rua Continental",
                city = "SÃ£o Bernardo do Campo",
                hasRedLightCamera = false,
                cycleTimeSeconds = 60,
                directionBearing = 30f
            ),
            TrafficLightEntity(
                id = "sbc_tl_piraporinha_robert_kennedy",
                latitude = -23.6950,
                longitude = -46.5920,
                roadName = "Av. Piraporinha",
                crossRoadName = "Av. Robert Kennedy (Largo)",
                city = "SÃ£o Bernardo do Campo",
                hasRedLightCamera = true,
                cycleTimeSeconds = 90,
                directionBearing = 190f
            )
        )
    }
}
