#pragma once
#include "dom/resize_observer.h"
#include "dom/scroll_element.h"
#include "event/event_listener.h"
#include "event/scroll_event.h"
#include "interface/UniCSSProperty.h"
#include "layout/Flex.h"
#include "layout/UniLayout.h"
#include "page_harmony/page.h"
#include "runtime_instance.h"
#include "sdk.h"
#include "vue/uni_vue_component.h"
#include <memory>
#include <node_api.h>
#include <string>
#include <unordered_map>
#include <vector>

using namespace uniappx;
using namespace vue;

namespace recycle_list {
// common start
class IRecycleListItem {
public:
    virtual void updateItemOffset(float offset){};
};
class IRecycleList : public std::enable_shared_from_this<IRecycleList> {
public:
    virtual void updateItemSize(const std::string key, float size){};
    virtual float getItemOffset(const std::string key) { return 0.0; };
    virtual float getItemSize(const std::string key) { return -1.0; };
    std::unordered_map<std::string, IRecycleListItem *> itemInstanceMap;
};
// common end

// RecycleListItem start
class RecycleListItem : public UniVueComponent, public IRecycleListItem {
private:
    float offset = -1.0f;
    UniViewElement *viewElement = nullptr;
    UniResizeObserver *resizeObserver = nullptr;
    void setInstance(std::string key);
    void removeInstance(std::string key);
    IRecycleList *getRecycleList();
    void startObserve();
    void stopObserve();
    bool isCurrentElementAttached();
    void initSize();
    void translate(float offset);
    float getItemSizeFromList();

public:
    RecycleListItem();
    ~RecycleListItem();
    float size = 0.0;
    float getSize();
    void setSize(float size);
    /**
     * listViewId第一个合法值为1.0
     */
    double listViewId = 0.0;
    std::string key = "";
    void setElement(Element *element);
    void updateKey(std::string key);
    void updateItemSize();
    void updateItemOffset(float offset);
    void getAndUpdateItemOffset();
    void setListViewId(double listViewId);
};
// RecycleListItem end

// RecycleList start
class RecycleList : public UniVueComponent, public IRecycleList {
private:
    UniScrollViewElement *scrollElement = nullptr;
    UniViewElement *listHeaderElement = nullptr;
    UniViewElement *placeholderElement = nullptr;
    EventListener *scrollListener = nullptr;
    EventListener *scrollEndListener = nullptr;
    UniResizeObserver *resizeObserver = nullptr;
    UniResizeObserver *headerResizeObserver = nullptr;
    // Keys for items
    std::vector<std::string> keyList;

    struct ItemInfo {
        int index;
        std::string key;
        float size;   // -1 means unknown, use defaultItemSize
        float offset; // top offset
        int type;     // reserved
    };

    std::vector<ItemInfo> list;
    // Fast lookup: key -> pointer to ItemInfo in `list`
    std::unordered_map<std::string, ItemInfo *> keyItemMap;
    ItemInfo *anchorItem = nullptr;

    float size = 600.0f;
    float headerSize = 0.0f;
    float defaultItemSize = 150.0f;
    float scrollOffset = 0.0f;
    float lastScrollOffset = 0.0f;
    float lastScrollOffsetForFastScroll = 0.0f;
    float lastScrollOffsetForFastScrollRender = 0.0f;
    uint64_t lastScrollTimestampForFastScroll = 0;
    float minRenderOffset = 0.0f;
    float maxRenderOffset = 600.0f;
    float placeholderSize = 1200.0f;
    // float cachedSize = 200.0f;
    float cachedSizeStart = 200.0f;
    float cachedSizeEnd = 200.0f;
    float originalCachedSize = 200.0f;
    bool resetCachedSizeOnNextRender = false;
    int renderRangeStart = 0;
    int renderRangeLength = 0;
    int lastRenderRangeStart = 0;
    int lastRenderRangeLength = 0;
    /**
     * 记录由于item size变化引起的offset补偿值，上方元素变小时此值为负值
     * 表示需要scrollTop增加该值，各个item的offset也需要增加该值
     */
    float itemSizeChangedCompensation = 0.0f;
    /**
     * 快速滚动过程中不渲染新的item，旧item通过offset+fastScrollOffset显示在要渲染的区域
     * 向头部滚动时此值为负值
     */
    float fastScrollOffset = 0.0f;
    /**
     * 快速滚动期间的特殊逻辑：
     * - 滚动条快速滚动，但是内容缓慢滚动，通过translate将缓慢滚动的内容持续显示在可视区域
     */
    bool fastScrolling = false;
    /**
     * 判断是否为快速滚动加速度阈值
     */
    float fastScrollVelocity = 20.0f;
    /**
     * 快速滚动时单次滚动（每帧）距离限制
     */
    float fastScrollDeltaLimit = 150.0f;
    bool scrolling = false;
    bool ignoreNextScroll = false;

public:
    RecycleList();
    ~RecycleList();
    /**
     * listViewId第一个合法值为1.0
     */
    double listViewId = 0.0;
    void setElement(Element *element);
    void setListHeaderElement(Element *element);
    void setPlaceholderElement(Element *element);
    void startRootObserve();
    void stopRootObserve();
    void startHeaderObserve();
    void stopHeaderObserve();
    void addScrollListener();
    void removeScrollListener();
    void onScroll(const UniScrollEvent &event);
    void onScrollEnd();
    void onScrollStart();
    void updateList(const std::vector<std::string> keyList);
    void updateContainerSize(float size);
    void updateHeaderSize(float size);
    void updateScrollOffset(float offset);
    void updateItemSize(const std::string key, float size);
    float getItemOffset(const std::string key);
    float getItemSize(const std::string key);
    void setListViewId(double listViewId);

private:
    // helpers
    inline float realItemSize(float size) const { return size < 0 ? defaultItemSize : size; }
    inline float itemEndOffset(const ItemInfo *item) const { return item->offset + realItemSize(item->size); }

    void updateMinMaxRenderOffset();
    void preTriggerRenderListUpdate();
    void triggerRenderListUpdate();
    void updateRenderList();
    void updateRenderListOnItemSizeChange();
    void updateRenderListOnScroll();
    void updateRenderListForward();
    void updateRenderListBackward();
    bool isAllRenderItemSettled();
    void recordItemOffsetCompensation(float delta);
    ItemInfo *getRenderStartItem() {
        if (this->renderRangeStart >= 0 && this->renderRangeStart < static_cast<int>(this->list.size())) {
            return &this->list[this->renderRangeStart];
        }
        return nullptr;
    }
    ItemInfo *getRenderEndItem() {
        int endIndex = this->renderRangeStart + this->renderRangeLength - 1;
        if (endIndex >= 0 && endIndex < static_cast<int>(this->list.size())) {
            return &this->list[endIndex];
        }
        return nullptr;
    }
    void enterFastScrollMode();
    void leaveFastScrollMode();
    float prepareFastScroll(float offset);
};
// RecycleList end

} // namespace recycle_list
