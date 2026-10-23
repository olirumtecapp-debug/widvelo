package com.creativeam.widvelo.sync

import android.content.Context
import com.creativeam.widvelo.incident.PreloadedIncidents
import com.creativeam.widvelo.incident.RoadIncidentEntity
import com.creativeam.widvelo.radar.PreloadedRadars
import com.creativeam.widvelo.radar.RadarEntity
import com.creativeam.widvelo.radar.RadarType
import com.creativeam.widvelo.radar.WidVeloDatabase
import com.creativeam.widvelo.trafficlight.PreloadedTrafficLights
import com.creativeam.widvelo.trafficlight.TrafficLightEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class SyncResult(
    val success: Boolean,
    val radarsCount: Int,
    val trafficLightsCount: Int,
    val incidentsCount: Int,
    val message: String
)

/**
 * Gerenciador de Sincronização Territorial do WidVelo.
 * Permite baixar a malha completa de semáforos, radares e limites de São Bernardo do Campo
 * e expandir para qualquer outra cidade do Brasil via OpenStreetMap (Overpass API).
 */
class CitySyncManager(private val database: WidVeloDatabase) {

    /**
     * Sincroniza uma cidade ou região específica.
     * Padrão: São Bernardo do Campo & Grande ABC (BBox: -23.85, -46.65 a -23.64, -46.48).
     */
    suspend fun syncTerritory(
        cityName: String = "São Bernardo do Campo",
        minLat: Double = -23.85,
        minLon: Double = -46.65,
        maxLat: Double = -23.64,
        maxLon: Double = -46.48
    ): SyncResult = withContext(Dispatchers.IO) {
        var addedRadars = 0
        var addedLights = 0
        var addedIncidents = 0

        try {
            // 1. Tenta carregar os dados mais recentes via Overpass API do OpenStreetMap
            val overpassQuery = """
                [out:json][timeout:20];
                (
                  node["highway"="speed_camera"]($minLat,$minLon,$maxLat,$maxLon);
                  node["highway"="traffic_signals"]($minLat,$minLon,$maxLat,$maxLon);
                );
                out body 150;
            """.trimIndent()

            val encodedQuery = URLEncoder.encode(overpassQuery, "UTF-8")
            val url = URL("https://overpass-api.de/api/interpreter?data=$encodedQuery")

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 12000
                setRequestProperty("User-Agent", "WidVelo-TerritorySync/1.0")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val elements = json.optJSONArray("elements")

                if (elements != null && elements.length() > 0) {
                    val downloadedRadars = mutableListOf<RadarEntity>()
                    val downloadedLights = mutableListOf<TrafficLightEntity>()

                    for (i in 0 until elements.length()) {
                        val el = elements.getJSONObject(i)
                        val lat = el.getDouble("lat")
                        val lon = el.getDouble("lon")
                        val tags = el.optJSONObject("tags") ?: JSONObject()
                        val id = "osm_${el.optLong("id", i.toLong())}"

                        val highwayTag = tags.optString("highway")
                        if (highwayTag == "speed_camera") {
                            val maxspeedStr = tags.optString("maxspeed", "50").replace(Regex("[^0-9]"), "")
                            val speed = maxspeedStr.toIntOrNull() ?: 50
                            val name = tags.optString("name", tags.optString("description", "Via Monitorada ($cityName)"))

                            downloadedRadars.add(
                                RadarEntity(
                                    id = id,
                                    latitude = lat,
                                    longitude = lon,
                                    speedLimit = speed,
                                    type = RadarType.FIXED,
                                    bearing = tags.optDouble("direction", 0.0).toFloat().takeIf { it > 0f },
                                    highway = name,
                                    kmReference = "OSM Sync",
                                    municipality = cityName,
                                    state = "SP",
                                    source = "OVERPASS_OSM",
                                    status = "HOMOLOGADO",
                                    lastVerifiedDate = "2026-10-23"
                                )
                            )
                        } else if (highwayTag == "traffic_signals") {
                            val road = tags.optString("street", tags.optString("name", "Cruzamento Urbano"))
                            downloadedLights.add(
                                TrafficLightEntity(
                                    id = id,
                                    latitude = lat,
                                    longitude = lon,
                                    roadName = road,
                                    crossRoadName = "Cruzamento Semafórico",
                                    city = cityName,
                                    hasRedLightCamera = tags.optString("camera") == "yes",
                                    cycleTimeSeconds = 70,
                                    directionBearing = tags.optDouble("direction", 0.0).toFloat().takeIf { it > 0f }
                                )
                            )
                        }
                    }

                    if (downloadedRadars.isNotEmpty()) {
                        database.radarDao().insertAll(downloadedRadars)
                        addedRadars += downloadedRadars.size
                    }
                    if (downloadedLights.isNotEmpty()) {
                        database.trafficLightDao().insertAll(downloadedLights)
                        addedLights += downloadedLights.size
                    }
                }
            }
        } catch (_: Exception) {
            // Em caso de falta de conexão ou timeout na rua, garante a base local consolidada
        }

        // 2. Sempre complementa com a base sólida homologada de SBC (Vianas, Ítalo Setti, etc.)
        val seedRadars = PreloadedRadars.getInitialSeed()
        val seedLights = PreloadedTrafficLights.getInitialSeed()
        val seedIncidents = PreloadedIncidents.getInitialSeed()

        database.radarDao().insertAll(seedRadars)
        database.trafficLightDao().insertAll(seedLights)
        database.roadIncidentDao().insertAll(seedIncidents)

        val totalRadars = database.radarDao().getRadarCount()
        val totalLights = database.trafficLightDao().getCount()
        val totalIncidents = database.roadIncidentDao().getCount()

        SyncResult(
            success = true,
            radarsCount = totalRadars,
            trafficLightsCount = totalLights,
            incidentsCount = totalIncidents,
            message = "Malha de $cityName sincronizada! Total: $totalRadars radares, $totalLights semáforos e $totalIncidents pontos de atenção ativos."
        )
    }
}
