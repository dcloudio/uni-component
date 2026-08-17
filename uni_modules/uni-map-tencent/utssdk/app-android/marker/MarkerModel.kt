package uts.sdk.modules.uniMapTencent.marker

/**
 * 基础数据类型格式
 * @property rotate Double?
 * @property alpha Float
 * @property width Double?
 * @property height Double?
 * @property clusterId Int?
 * @property ariaLabel String?
 * @property joinCluster Boolean
 * @property callout MarkerCallout?
 * @property label MarkerLabel?
 * @property anchor Any?
 * @property customCallout MarkerCustomCallout?
 * @constructor
 */
class MarkerModel(val id: Int, val latitude: Double, val longitude: Double, val iconPath: String) {
    var title: String? = null
    var rotate: Double = 0.0
    var alpha = 1.0f
    var width: Double? = null
    var height: Double? = null
    var clusterId: Int? = null
    var ariaLabel: String? = null
    var anchor = FloatArray(2)

    var joinCluster = false
    var callout: CalloutModel? = null
    var label: MarkerLabel? = null
    var customCallout: CalloutModel? = null

    init {
        anchor[0] = 0.5f
        anchor[1] = 1.0f
    }
}