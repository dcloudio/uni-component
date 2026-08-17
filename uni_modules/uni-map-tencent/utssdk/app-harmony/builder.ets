import { MapComponent, InfoWindowInfo } from '@tencentmap/map'

export interface ExistMarkerInfo {
	id : string
	text : string
	infoWindowInfo : InfoWindowInfo
}

@Builder
export function buildTencentMap(params: ESObject) {
	Column() {
		MapComponent({
		  mapOptions: params.mapOptions,
		  onReady: params.onReady
		})
		/* TODO 下面的注释请勿删除，后面会用到 */
		/* ForEach(params.existMarkerInfoList, (item: ExistMarkerInfo) => {
			Text(item.text)
				.id(item.id)
				.fontSize(14)
				.backgroundColor("#ffffff")
				.padding(4)
				.visibility(item.infoWindowInfo.visible ? Visibility.Visible : Visibility.Hidden)
				.fontWeight(FontWeight.Bold)
				.position({ x: item.infoWindowInfo.x, y: item.infoWindowInfo.y  })
				.opacity(1)
		}, (item: ExistMarkerInfo) => item.id)
		
		Text("length: " + params.existMarkerInfoList.length + " - " + params.existMarkerInfoList[0]?.id)
			.id("info_window_0")
			.fontSize(14)
			.backgroundColor("#ffffff")
			.padding(4)
			.visibility(Visibility.Visible)
			.fontWeight(FontWeight.Bold)
			.position({ x: 0, y: 0 })
			.opacity(1) */
					
	}
  
}
