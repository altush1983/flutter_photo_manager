package com.fluttercandies.photo_manager.core.entity

import android.annotation.SuppressLint
import android.provider.MediaStore
import com.fluttercandies.photo_manager.constant.AssetType
import com.fluttercandies.photo_manager.core.utils.ConvertUtils

class FilterOption(map: Map<*, *>) {
    val videoOption = ConvertUtils.getOptionFromType(map, AssetType.Video)
    val imageOption = ConvertUtils.getOptionFromType(map, AssetType.Image)
    val audioOption = ConvertUtils.getOptionFromType(map, AssetType.Audio)
    val anyOption = ConvertUtils.getOptionFromType(map, AssetType.Any)
    val createDateCond = ConvertUtils.convertToDateCond(map["createDate"] as Map<*, *>)
    val updateDateCond = ConvertUtils.convertToDateCond(map["updateDate"] as Map<*, *>)
    val containsPathModified = map["containsPathModified"] as Boolean

    private val orderByCond: List<OrderByCond> =
        ConvertUtils.convertOrderByCondList(map["orders"] as List<*>)

    fun orderByCondString(): String? {
        if (orderByCond.isEmpty()) {
            return null
        }
        return orderByCond.joinToString(",") {
            it.getOrder()
        }
    }
}

class FilterCond {
    var isShowTitle = false
    lateinit var sizeConstraint: SizeConstraint
    lateinit var durationConstraint: DurationConstraint
    lateinit var mimeTypesConstraint: MimeTypesConstraint
    lateinit var fileSizeConstraint: FileSizeConstraint

    companion object {
        private const val widthKey = MediaStore.Files.FileColumns.WIDTH
        private const val heightKey = MediaStore.Files.FileColumns.HEIGHT

        @SuppressLint("InlinedApi")
        private const val durationKey = MediaStore.Video.VideoColumns.DURATION

        private const val mimeTypeKey = MediaStore.Files.FileColumns.MIME_TYPE
        private const val fileSizeKey = MediaStore.Files.FileColumns.SIZE
    }

    fun sizeCond(): String =
        "$widthKey >= ? AND $widthKey <= ? AND $heightKey >= ? AND $heightKey <=?"

    fun sizeArgs(): Array<String> {
        return arrayOf(
            sizeConstraint.minWidth,
            sizeConstraint.maxWidth,
            sizeConstraint.minHeight,
            sizeConstraint.maxHeight
        ).toList().map {
            it.toString()
        }.toTypedArray()
    }

    fun mimeTypesCond(): String {
        if (mimeTypesConstraint.ignoreTypes || mimeTypesConstraint.types.isEmpty()) return ""
        return mimeTypesConstraint.types.joinToString(" OR ") { "$mimeTypeKey = ?" }
    }

    fun mimeTypesArgs(): Array<String> {
        if (mimeTypesConstraint.ignoreTypes || mimeTypesConstraint.types.isEmpty()) return arrayOf()
        return mimeTypesConstraint.types.toTypedArray()
    }

    fun durationCond(): String {
        val baseCond = "$durationKey >=? AND $durationKey <=?"
        if (durationConstraint.allowNullable) {
            return "( $durationKey IS NULL OR ( $baseCond ) )"
        }
        return baseCond
    }

    fun durationArgs(): Array<String> {
        return arrayOf(
            durationConstraint.min,
            durationConstraint.max
        ).map { it.toString() }.toTypedArray()
    }

    fun fileSizeCond(): String {
        if (fileSizeConstraint.ignoreSize) return ""
        val baseCond = "$fileSizeKey >=? AND $fileSizeKey <=?"
        return baseCond
    }

    fun fileSizeArgs(): Array<String> {
        return arrayOf(
            fileSizeConstraint.min,
            fileSizeConstraint.max
        ).map { it.toString() }.toTypedArray()
    }

    class SizeConstraint {
        var minWidth = 0
        var maxWidth = 0
        var minHeight = 0
        var maxHeight = 0
        var ignoreSize = false
    }

    class DurationConstraint {
        var min: Long = 0
        var max: Long = 0
        var allowNullable: Boolean = false
    }

    class MimeTypesConstraint {
        var types = listOf<String>()
        var ignoreTypes = false
    }

    class FileSizeConstraint {
        var min: Long = 0
        var max: Long = 0
        var ignoreSize = false
    }
}

data class DateCond(
    val minMs: Long,
    val maxMs: Long,
    val ignore: Boolean
)

data class OrderByCond(
    val key: String,
    val asc: Boolean
) {
    fun getOrder(): String {
        val ascValue = if (asc) {
            "asc"
        } else {
            "desc"
        }
        return "$key $ascValue"
    }
}
