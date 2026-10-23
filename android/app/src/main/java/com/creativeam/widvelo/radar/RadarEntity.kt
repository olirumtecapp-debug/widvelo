package com.creativeam.widvelo.radar

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/**
 * Entidade de radar persistida no banco Room local do WidVelo.
 */
@Entity(tableName = "radars")
data class RadarEntity(
    @PrimaryKey
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val speedLimit: Int?, // Ex: 50, 60, 80, 100, 110, 120
    val type: RadarType,
    val bearing: Float?, // Direção em graus (0..359) do fluxo de veículos fiscalizado
    val highway: String?, // Ex: "BR-116", "SP-160", "Av. 23 de Maio"
    val kmReference: String?, // Ex: "km 214+500"
    val municipality: String?, // Ex: "São Paulo", "Guarulhos"
    val state: String?, // Ex: "SP", "RJ", "MG"
    val source: String, // "ANTT", "OPENSTREETMAP", "DER-SP", "CONCESSIONARIA"
    val status: String, // "ATIVO", "HOMOLOGADO", "EM_VERIFICACAO"
    val lastVerifiedDate: String // Data ISO ou legível da verificação
)

class RadarTypeConverter {
    @TypeConverter
    fun fromRadarType(type: RadarType): String = type.name

    @TypeConverter
    fun toRadarType(value: String): RadarType = RadarType.fromString(value)
}
