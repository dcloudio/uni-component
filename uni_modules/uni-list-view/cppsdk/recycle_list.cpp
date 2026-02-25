#include <chrono>
#include "recycle_list.h"

using namespace uniappx;

namespace recycle_list {
// common start
auto listInstanceCache = std::unordered_map<double, std::weak_ptr<IRecycleList>>();
// common end
// RecycleList start
RecycleList::RecycleList(){};
RecycleList::~RecycleList() {
    this->removeScrollListener();
    this->stopRootObserve();
    this->stopHeaderObserve();
    this->itemInstanceMap.clear();
    listInstanceCache.erase(this->listViewId);
};

void RecycleList::setListViewId(double listViewId) {
    this->listViewId = listViewId;
    listInstanceCache[listViewId] = this->shared_from_this();
}

void RecycleList::setElement(Element *element) {
    // setElement触发在render之后，需要在此时机获取一次高度
    const auto scrollElement = dynamic_cast<ScrollViewElement *>(element);
    this->scrollElement = scrollElement;
    // 调用getBoundingClientRect获取高度略微浪费性能。OffsetHeight返回的又是整形，不符合预期。或许需要暴露NativeView给list-view用
    // auto size = this->scrollElement->getBoundingClientRect().height;
    auto scrollLayoutNode = this->scrollElement->GetLayoutNode();
    auto height = UniLayoutNodeLayoutGetHeight(scrollLayoutNode);
    auto paddingTop = UniLayoutNodeLayoutGetPadding(scrollLayoutNode, CSSDirection::CSSTop);
    auto paddingBottom = UniLayoutNodeLayoutGetPadding(scrollLayoutNode, CSSDirection::CSSBottom);
    if (height && !std::isnan(height)) {
        if (std::isnan(paddingTop)) {
            paddingTop = 0;
        }
        if (std::isnan(paddingBottom)) {
            paddingBottom = 0;
        }
        this->updateContainerSize(height - paddingTop - paddingBottom);
    }
    this->addScrollListener();
    this->startRootObserve();
};

void RecycleList::setPlaceholderElement(Element *element) {
    if (this->placeholderElement == element) {
        return;
    }
    this->placeholderElement = dynamic_cast<UniViewElement *>(element);
}

void RecycleList::setListHeaderElement(Element *element) {
    if (this->listHeaderElement == element) {
        return;
    }
    if (this->listHeaderElement) {
        this->stopHeaderObserve();
    }
    this->listHeaderElement = dynamic_cast<UniViewElement *>(element);
    auto size = UniLayoutNodeLayoutGetHeight(this->listHeaderElement->GetLayoutNode());
    if (size && !std::isnan(size)) {
        this->updateHeaderSize(size);
    }
    this->startHeaderObserve();
};

void RecycleList::startRootObserve() {
    this->resizeObserver = new UniResizeObserver([this](const std::vector<UniResizeObserverEntry> &entries) {
        auto entry = entries[0];
        // 获取内容区域尺寸
        auto size = entry.contentBoxSize[0].blockSize;
        this->size = size;
        this->updateContainerSize(size);
    });
    this->resizeObserver->observe(this->scrollElement);
}

void RecycleList::stopRootObserve() {
    if (this->resizeObserver) {
        this->resizeObserver->unobserve(this->scrollElement);
        delete this->resizeObserver;
        this->resizeObserver = nullptr;
    }
}

void RecycleList::startHeaderObserve() {
    if (this->listHeaderElement) {
        this->headerResizeObserver = new UniResizeObserver([this](const std::vector<UniResizeObserverEntry> &entries) {
            auto entry = entries[0];
            auto size = entry.borderBoxSize[0].blockSize;
            this->updateHeaderSize(size);
        });
        this->headerResizeObserver->observe(this->listHeaderElement);
    }
}

void RecycleList::stopHeaderObserve() {
    if (this->headerResizeObserver) {
        this->headerResizeObserver->unobserve(this->listHeaderElement);
        delete this->headerResizeObserver;
        this->headerResizeObserver = nullptr;
    }
}

void RecycleList::addScrollListener() {
    if (!this->scrollListener) {
        this->scrollListener = this->scrollElement
                                   ->addEventListener("scroll",
                                                      [this](const Event &event) {
                                                          const auto scrollEvent =
                                                              static_cast<const UniScrollEvent &>(event);
                                                          this->onScroll(scrollEvent);
                                                      })
                                   .get();
    }
    if (!this->scrollEndListener) {
        this->scrollEndListener =
            this->scrollElement->addEventListener("scrollend", [this](const Event &event) { this->onScrollEnd(); })
                .get();
    }
};

void RecycleList::removeScrollListener() {
    if (this->scrollElement && this->scrollListener) {
        this->scrollElement->removeEventListener(this->scrollListener->getId());
        this->scrollListener = nullptr;
    }
    if (this->scrollElement && this->scrollEndListener) {
        this->scrollElement->removeEventListener(this->scrollEndListener->getId());
        this->scrollEndListener = nullptr;
    }
};

void RecycleList::onScroll(const UniScrollEvent &event) {
    if (this->ignoreNextScroll) {
        this->ignoreNextScroll = false;
        return;
    }
    if (!this->scrolling) {
        this->onScrollStart();
        this->scrolling = true;
    }
    float scrollTop = this->scrollElement->getScrollTop();
    scrollTop = this->prepareFastScroll(scrollTop);
    this->updateScrollOffset(scrollTop);
};

void RecycleList::onScrollStart() {
    this->anchorItem = this->getRenderStartItem();
    // TODO 不考虑同一页面多个回收列表同时滚动的场景
    this->scrollElement->GetPage()->setRecycling(true);
};

void RecycleList::onScrollEnd() {
    /**
     * 用户手指碰到scroll-view时如果scroll-view正在滚动会立刻触发scrollEnd
     */
    if (!this->scrolling) {
        return;
    }

    this->scrolling = false;
    this->anchorItem = nullptr;
    this->scrollElement->GetPage()->setRecycling(false);
    auto compensation = this->itemSizeChangedCompensation;

    if (this->fastScrolling || compensation != 0) {
        if (this->fastScrolling) {
            this->leaveFastScrollMode();
        }
        if (compensation != 0) {
            this->itemSizeChangedCompensation = 0;
        }
        if (this->fastScrolling) {
            this->updateRenderListOnScroll();
        } else {
            this->preTriggerRenderListUpdate();
        }
        if (compensation != 0) {
            this->ignoreNextScroll = true;
            this->scrollElement->scrollBy(0, compensation);
        }
    }
};

float RecycleList::prepareFastScroll(float offset) {
    auto timestamp = static_cast<uint64_t>(
        std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now().time_since_epoch())
            .count());
    auto lastOffset = this->lastScrollOffsetForFastScroll;
    auto lastTimestamp = this->lastScrollTimestampForFastScroll;
    this->lastScrollOffsetForFastScroll = offset;
    this->lastScrollTimestampForFastScroll = timestamp;
    if (lastTimestamp == 0) {
        this->lastScrollOffsetForFastScrollRender = offset;
        return offset;
    }

    auto deltaOffset = offset - lastOffset;
    auto deltaTime = timestamp - lastTimestamp;

    if (deltaTime <= 0) {
        // 不应进入此分支
        return offset;
    }
    
    bool ignoreFastScrollMode = offset < 1 || offset > this->placeholderSize - this->size - 1;
    if (!ignoreFastScrollMode) {
        auto velocity = deltaOffset / deltaTime;
        if (std::abs(velocity) > this->fastScrollVelocity) {
            this->enterFastScrollMode();
            if (velocity > 0) {
                // 快速滚动期间减少渲染内容
                this->cachedSizeStart = 0.0f;
                this->cachedSizeEnd = this->originalCachedSize;
            } else {
                // 快速滚动期间减少渲染内容
                this->cachedSizeStart = this->originalCachedSize;
                this->cachedSizeEnd = 0.0f;
            }
        } else {
            this->leaveFastScrollMode();
        }
    } else if (this->fastScrolling) {
        this->leaveFastScrollMode();
    }

    if (this->fastScrolling) {
        /**
         * 快速滚动过程中降低计算用的滚动速度，达到用户看起来是在滚动的效果
         * 此时fastScrollOffset累计实际滚动距离与显示距离的差值
         */
        auto deltaOffsetAbs = std::abs(deltaOffset);
        if (deltaOffsetAbs > this->fastScrollDeltaLimit) {
            auto deltaOffsetRender = this->fastScrollDeltaLimit * (deltaOffsetAbs / deltaOffset);
            offset = this->lastScrollOffsetForFastScrollRender + deltaOffsetRender;
            this->fastScrollOffset += deltaOffset - deltaOffsetRender;
        }
    }
    this->lastScrollOffsetForFastScrollRender = offset;
    return offset;
}

void RecycleList::enterFastScrollMode() {
    if (this->fastScrolling) {
        return;
    }
    this->fastScrolling = true;
    /**
     * 在用户注意不到的时机将补偿值设为0
     */
    this->itemSizeChangedCompensation = 0;
    this->fastScrollOffset = 0.0f;
}

void RecycleList::leaveFastScrollMode() {
    if (!this->fastScrolling) {
        return;
    }
    this->fastScrolling = false;
    /**
     * 在用户注意不到的时机将补偿值设为0
     */
    this->itemSizeChangedCompensation = 0;
    /**
     * 重设lastScrollOffset以便后续能判断真实滚动方向
     */
    this->lastScrollOffset = this->fastScrollOffset + this->scrollOffset;
    this->fastScrollOffset = 0.0f;
    this->lastScrollTimestampForFastScroll = 0;
    this->lastScrollOffsetForFastScrollRender = 0;
    this->scrollOffset = this->scrollElement->getScrollTop();
    this->resetCachedSizeOnNextRender = true;
    this->updateMinMaxRenderOffset();
}

void RecycleList::updateList(const std::vector<std::string> keyList) {
    this->keyList = keyList;

    // rebuild list with preserved items when possible
    std::vector<ItemInfo> tempList;
    tempList.reserve(keyList.size());

    // Use existing keyItemMap to locate prior items (pointers to ItemInfo)
    std::unordered_map<std::string, ItemInfo *> existingItems = this->keyItemMap;

    ItemInfo *lastItem = nullptr;
    for (size_t i = 0; i < keyList.size(); ++i) {
        const auto &key = keyList[i];
        auto it = existingItems.find(key);
        if (it != existingItems.end()) {
            ItemInfo current = *(it->second);
            current.index = static_cast<int>(i);
            current.offset = lastItem ? this->itemEndOffset(lastItem) : 0.0f;
            tempList.push_back(current);
            lastItem = &tempList.back();
        } else {
            ItemInfo current;
            current.index = static_cast<int>(i);
            current.key = key;
            current.size = -1.0f; // DEFAULT_ITEM_SIZE_PLACEHOLDER
            current.offset = lastItem ? this->itemEndOffset(lastItem) : 0.0f;
            current.type = 0;
            tempList.push_back(current);
            lastItem = &tempList.back();
        }
    }

    this->list.swap(tempList);
    // rebuild keyItemMap with pointers to items in `list`
    this->keyItemMap.clear();
    for (size_t i = 0; i < this->list.size(); ++i) {
        this->keyItemMap[this->list[i].key] = &this->list[i];
    }
    this->updateRenderList();
}

bool RecycleList::isAllRenderItemSettled() {
    for (int i = this->renderRangeStart; i < this->renderRangeStart + this->renderRangeLength; ++i) {
        const auto &item = this->list[i];
        if (item.size < 0) {
            return false;
        }
    }
    return true;
}

void RecycleList::updateItemSize(const std::string key, float size) {
    auto it = this->keyItemMap.find(key);
    if (it == this->keyItemMap.end())
        return;

    ItemInfo *itemPtr = it->second;

    if (itemPtr->size == size)
        return;

    auto index = itemPtr->index;
    if (this->anchorItem && index < this->anchorItem->index) {
        this->recordItemOffsetCompensation(size - this->realItemSize(itemPtr->size));
    }
    itemPtr->size = size;
    // 更新当前项之后的所有item的offset
    for (size_t i = index + 1; i < this->list.size(); ++i) {
        const auto prev = &this->list[i - 1];
        this->list[i].offset = this->itemEndOffset(prev);
    }
    if (this->isAllRenderItemSettled()) {
        this->updateRenderListOnItemSizeChange();
    }
}

void RecycleList::recordItemOffsetCompensation(float delta) {
    if (this->fastScrolling) {
        return;
    }
    this->itemSizeChangedCompensation += delta;
}

void RecycleList::updateContainerSize(float size) {
    if (this->size != size) {
        this->size = size;
        this->updateMinMaxRenderOffset();
        this->updateRenderList();
    }
}

void RecycleList::updateHeaderSize(float size) {
    if (this->headerSize == size) {
        return;
    }
    auto oldHeaderSize = this->headerSize;
    this->headerSize = size;
    this->updateMinMaxRenderOffset();
    if (this->headerSize > oldHeaderSize) {
        this->updateRenderListBackward();
    } else {
        this->updateRenderListForward();
    }
}

void RecycleList::updateScrollOffset(float offset) {
    float delta = offset - this->scrollOffset;
    float deltaAbs = std::abs(delta);
    this->lastScrollOffset = this->scrollOffset;

    float maxScroll = std::max(0.0f, this->placeholderSize - this->size);
    if (offset < 0.0f)
        this->scrollOffset = 0.0f;
    else if (offset > maxScroll)
        this->scrollOffset = maxScroll;
    else
        this->scrollOffset = offset;

//    if (this->lastScrollOffset == this->scrollOffset)
//        return;

    /**
     * 滚动期间持续消耗补偿值，避免一次性消耗过大引起跳动，/3是随便写的一个值，没有什么特别的。希望滚动期间抖动更小，就把分母改大一点。
     * 注意如果分母太大会导致滚动结束时补偿值剩余过多仍然会跳动。
     * 采用此方案而不是在滚动结束后一次消耗，是因为滚动结束后同时调整scrollTop和item的translateY无法确保在同一帧渲染完成，会引发明显跳动。
     */
    if (this->itemSizeChangedCompensation != 0) {
        auto compensationAbs = std::abs(this->itemSizeChangedCompensation);
        auto compensationDecrease = deltaAbs / 3;
        if (compensationAbs > compensationDecrease) {
            auto sign = this->itemSizeChangedCompensation / compensationAbs;
            this->itemSizeChangedCompensation -= compensationDecrease * sign;
        } else {
            this->itemSizeChangedCompensation = 0;
        }
    }

    this->updateMinMaxRenderOffset();
    this->updateRenderListOnScroll();
}

void RecycleList::updateMinMaxRenderOffset() {
    // cachedSize不用于避免频繁更新渲染列表，每次不可更新大量显示item，简单来说滚动期间的更新就是小步快跑
    auto pureOffset = this->scrollOffset - this->headerSize;
    this->minRenderOffset =
        std::max(0.0f, pureOffset - this->cachedSizeStart + this->itemSizeChangedCompensation);
    this->maxRenderOffset = std::min(pureOffset + this->size + this->cachedSizeEnd +
                                         this->itemSizeChangedCompensation,
                                     this->placeholderSize);
}

void RecycleList::preTriggerRenderListUpdate() {
    this->triggerRenderListUpdate();
    // 退出快速滚动的下一帧使用重置的cachedSize
    if(this->resetCachedSizeOnNextRender) {
        this->cachedSizeStart = this->originalCachedSize;
        this->cachedSizeEnd = this->originalCachedSize;
        this->resetCachedSizeOnNextRender = false;
    }
}

void RecycleList::triggerRenderListUpdate() {
    auto renderInfoChanged = false;
    if (this->renderRangeStart != this->lastRenderRangeStart ||
        this->renderRangeLength != this->lastRenderRangeLength) {
        renderInfoChanged = true;
    }

    // list-item在更新key之后主动获取offset进行更新，此时机用于更新已经显示的item的offset
    // 遍历this->itemInstanceMap，更新所有已渲染item的offset
    for (const auto &pair : this->itemInstanceMap) {
        const auto &key = pair.first;
        IRecycleListItem *itemInstance = pair.second;
        auto it = this->keyItemMap.find(key);
        if (it != this->keyItemMap.end()) {
            ItemInfo *itemPtr = it->second;
            auto index = itemPtr->index;
            if (index >= this->renderRangeStart && index < this->renderRangeStart + this->renderRangeLength) {
                itemInstance->updateItemOffset(itemPtr->offset - this->itemSizeChangedCompensation +
                                               this->fastScrollOffset);
            }
        }
    }

    if (!this->list.empty()) {
        const auto lastItem = &this->list.back();
        auto _placeholderSize = this->itemEndOffset(lastItem) - this->itemSizeChangedCompensation;
        if (this->placeholderSize != _placeholderSize) {
            /**
             * 此处需要切线程进行排版，导致界面更新不同步。
             * 例如可删除列表删除倒数第二个item时，倒数第一个的translate先更新，等待排版后才会更新placeholder的尺寸
             * 再然后才会引发scrollTop重新计算，在scrollTop重新计算之前用户会看到底部有空白
             * 此补救措施在scrollTop不满足placeholder尺寸变化时调整一次scrollTop
             */
            auto maxScrollOffset = std::max(0.0f, _placeholderSize - this->size);
            if (this->scrollOffset > maxScrollOffset) {
                this->scrollElement->scrollTo(0, maxScrollOffset);
            }
            this->placeholderSize = _placeholderSize;
            if (this->placeholderElement) {
                Instance::GetTaskExecutor().runOnDomQueue(
                    [placeholderElement = this->placeholderElement, _placeholderSize]() {
                        placeholderElement->UpdateStyle(UniCSSPropertyID::Height,
                                                        "" + std::to_string(_placeholderSize) + "px");
                    });
            }
        }
    }

    if (renderInfoChanged) {
        this->lastRenderRangeStart = this->renderRangeStart;
        this->lastRenderRangeLength = this->renderRangeLength;
        // 通知vue渲染新列表
        napi_value renderRangeStart;
        napi_value renderRangeLength;
        napi_create_double(this->_sharedData->_env, this->renderRangeStart, &renderRangeStart);
        napi_create_double(this->_sharedData->_env, this->renderRangeLength, &renderRangeLength);
        this->callMethod("updateRenderInfo", {renderRangeStart, renderRangeLength});
    }
}

void RecycleList::updateRenderList() {
    int start = 0;
    int length = 0;
    bool foundStart = false;
    // TODO 优化此处逻辑，考虑二分遍历
    for (size_t i = 0; i < this->list.size(); ++i) {
        const auto item = &this->list[i];
        if (this->itemEndOffset(item) < this->minRenderOffset) {
            continue;
        } else if (item->offset > this->maxRenderOffset) {
            break;
        } else {
            if (!foundStart) {
                foundStart = true;
                start = static_cast<int>(i);
            }
            length++;
        }
    }
    this->renderRangeStart = start;
    this->renderRangeLength = length;
    this->preTriggerRenderListUpdate();
}

void RecycleList::updateRenderListOnItemSizeChange() {
    int start = 0;
    int length = 0;
    bool foundStart = false;
    // TODO 优化此处逻辑，考虑二分遍历
    for (size_t i = 0; i < this->list.size(); ++i) {
        const auto item = &this->list[i];
        if (this->itemEndOffset(item) < this->minRenderOffset) {
            continue;
        } else if (item->offset > this->maxRenderOffset) {
            break;
        } else {
            if (!foundStart) {
                foundStart = true;
                start = static_cast<int>(i);
            }
            length++;
        }
    }
    
    /**
     * 渲染1-2-3-4时，如果3尺寸比预估大很多，会把4挤出渲染区域，此时无需重新调整渲染区域
     * 某些快速滑动时出现的错乱疑似由此场景引发，在把4挤出渲染区域后，仍快速向尾部滚动，此时会再次渲染4
     */
    auto currentEnd = this->renderRangeStart + this->renderRangeLength - 1;
    auto end = start + length - 1;
    if (start < this->renderRangeStart) {
        this->renderRangeStart = start;
        this->renderRangeLength = currentEnd - start + 1;
    }
    if (end > currentEnd) {
        this->renderRangeLength = end - this->renderRangeStart + 1;
    }
    this->preTriggerRenderListUpdate();
}

void RecycleList::updateRenderListOnScroll() {
    if (this->scrollOffset == this->lastScrollOffset) {
        this->updateRenderList();
        return;
    }
    if (this->scrollOffset > this->lastScrollOffset) {
        // scrolling forward (down)
        // TODO 需要启用下方的调用频率限制，但是目前启用会引发部分item渲染错位，待排查
        // 后续步骤中仍然有限制，启用此频率限制优先级不高
        // const ItemInfo *renderEndItem = this->getRenderEndItem();
        // if (renderEndItem &&
        //     this->itemEndOffset(*renderEndItem) >= this->maxRenderOffset) {
        //     return;
        // }
        this->updateRenderListForward();
    } else {
        // scrolling backward (up)
        // const ItemInfo *renderStartItem = this->getRenderStartItem();
        // if (renderStartItem && renderStartItem->offset <= this->minRenderOffset) {
        //     return;
        // }
        this->updateRenderListBackward();
    }
}

void RecycleList::updateRenderListForward() {
    const int lastRenderIndex =
        this->renderRangeLength == 0 ? -1 : (this->renderRangeStart + this->renderRangeLength - 1);
    // remove invisible from top
    while (this->renderRangeLength > 0) {
        auto currentItem = this->list[this->renderRangeStart];
        if (this->itemEndOffset(&currentItem) < this->minRenderOffset) {
            this->renderRangeStart++;
            this->renderRangeLength--;
        } else {
            break;
        }
    }
    // append visible at bottom
    bool foundStart = this->renderRangeLength > 0;
    auto listSize = static_cast<int>(this->list.size());
    for (int i = lastRenderIndex + 1; i < listSize; ++i) {
        const auto item = &this->list[i];
        if (this->itemEndOffset(item) < this->minRenderOffset) {
            continue;
        } else if (item->offset > this->maxRenderOffset) {
            break;
        } else {
            this->renderRangeLength++;
            if (!foundStart) {
                foundStart = true;
                this->renderRangeStart = i;
            }
        }
    }
    this->preTriggerRenderListUpdate();
}

void RecycleList::updateRenderListBackward() {
    const int firstRenderIndex =
        this->renderRangeLength == 0 ? static_cast<int>(this->list.size()) : this->renderRangeStart;
    // remove invisible from bottom
    while (this->renderRangeLength > 0) {
        auto currentItem = &this->list[this->renderRangeStart + this->renderRangeLength - 1];
        if (currentItem->offset > this->maxRenderOffset) {
            this->renderRangeLength--;
        } else {
            break;
        }
    }
    // prepend visible at top
    for (int i = firstRenderIndex - 1; i >= 0; --i) {
        const auto item = &this->list[i];
        if (item->offset > this->maxRenderOffset) {
            continue;
        } else if (this->itemEndOffset(item) < this->minRenderOffset) {
            break;
        } else {
            this->renderRangeStart = i;
            this->renderRangeLength++;
        }
    }
    this->preTriggerRenderListUpdate();
}

float RecycleList::getItemOffset(const std::string key) {
    auto it = this->keyItemMap.find(key);
    if (it == this->keyItemMap.end())
        return -1.0f;

    ItemInfo *itemPtr = it->second;
    return itemPtr->offset - this->itemSizeChangedCompensation;
}

float RecycleList::getItemSize(const std::string key) {
    auto it = this->keyItemMap.find(key);
    if (it == this->keyItemMap.end())
        return -1.0f;

    ItemInfo *itemPtr = it->second;
    return itemPtr->size;
}
// RecycleList end

// RecycleListItem start

RecycleListItem::RecycleListItem(){};
RecycleListItem::~RecycleListItem() { this->stopObserve(); };

/**
 * setListViewId会在updateKey之前调用。
 * TODO 不要依赖setListViewId、updateKey调用时机特性
 */
void RecycleListItem::setListViewId(double listViewId) { this->listViewId = listViewId; }

/**
 * updateKey和setElement先后顺序无法确定
 * setElement每个list-item组件只会调用一次
 */
void RecycleListItem::setElement(Element *element) {
    if (this->viewElement) {
        return;
    }
    const auto viewElement = dynamic_cast<ViewElement *>(element);
    this->viewElement = viewElement;
    this->initSize();
    this->startObserve();
    this->getAndUpdateItemOffset();
};

void RecycleListItem::initSize() {
    if (!this->viewElement) {
        return;
    }

    auto size = UniLayoutNodeLayoutGetHeight(this->viewElement->GetLayoutNode());
    if (std::isnan(size)) {
        auto size = this->viewElement->getBoundingClientRect().height;
        this->setSize(size);
    } else {
        this->setSize(size);
    }
}

void RecycleListItem::startObserve() {
    this->resizeObserver = new UniResizeObserver([this](const std::vector<UniResizeObserverEntry> &entries) {
        auto entry = entries[0];
        auto size = entry.borderBoxSize[0].blockSize;
        this->setSize(size);
    });
    this->resizeObserver->observe(this->viewElement);
};

void RecycleListItem::stopObserve() {
    if (this->resizeObserver && this->viewElement) {
        this->resizeObserver->unobserve(this->viewElement);
        delete this->resizeObserver;
    }
}

float RecycleListItem::getSize() { return this->size; };

void RecycleListItem::updateItemSize() {
    if (this->key == "" || this->size <= 0.0) {
        return;
    }
    auto recycleList = this->getRecycleList();
    if (recycleList) {
        recycleList->updateItemSize(this->key, this->size);
    }
}

void RecycleListItem::setSize(float size) {
    // TODO 排查为什么有时候会获取到NaN
    if (size && !std::isnan(size) && size > 0.0) {
        this->size = size;
        this->updateItemSize();
    }
};

void RecycleListItem::updateKey(std::string key) {
    auto prevKey = this->key;
    if (prevKey == key) {
        return;
    }
    if (prevKey != "") {
        // key更新逻辑
        this->removeInstance(prevKey);
    }
    this->key = key;
    this->setInstance(key);
    // key变更后需要重置offset
    this->offset = -1.0f;
    this->getAndUpdateItemOffset();
    /**
     * key更新时优先从list获取缓存的size，否则可能获取到复用的item的size
     * 后续步骤仍会将尺寸更新为正确值，但是期间出现的错误值会导致抖动
     */
    auto sizeFromList = this->getItemSizeFromList();
    if(sizeFromList > 0) {
        /**
         * 从list获取的size无需再更新到list
         */
        this->size = sizeFromList;
    } else {
        this->initSize();
    }
};

float RecycleListItem::getItemSizeFromList() {
    auto recycleList = this->getRecycleList();
    if (recycleList) {
        return recycleList->getItemSize(this->key);
    }
    return -1.0;
}

IRecycleList *RecycleListItem::getRecycleList() {
    if (this->listViewId < 1.0) {
        return nullptr;
    }
    auto it = listInstanceCache.find(this->listViewId);
    if (it != listInstanceCache.end()) {
        return it->second.lock().get();
    }
    return nullptr;
}

void RecycleListItem::setInstance(std::string key) {
    auto recycleList = this->getRecycleList();
    if (recycleList) {
        recycleList->itemInstanceMap[key] = this;
    }
}

void RecycleListItem::removeInstance(std::string key) {
    auto recycleList = this->getRecycleList();
    if (recycleList) {
        auto it = recycleList->itemInstanceMap.find(key);
        if (it != recycleList->itemInstanceMap.end() && it->second == this) {
            recycleList->itemInstanceMap.erase(it);
        }
    }
}

bool RecycleListItem::isCurrentElementAttached() {
    if (!this->viewElement) {
        return false;
    }
    if (!this->viewElement->isConnected()) {
        return false;
    }
    if (this->key == "") {
        return false;
    }
    return true;
}

/**
 * 供RecycleList调用
 */
void RecycleListItem::updateItemOffset(float offset) {
    if (!this->isCurrentElementAttached()) {
        return;
    }
    if (offset >= 0 && this->offset != offset) {
        this->translate(offset);
        this->offset = offset;
    }
}

/**
 * 供RecycleListItem内部调用
 */
void RecycleListItem::getAndUpdateItemOffset() {
    if (!this->isCurrentElementAttached()) {
        return;
    }
    auto recycleList = this->getRecycleList();
    if (recycleList) {
        auto offset = recycleList->getItemOffset(this->key);
        if (offset >= 0 && this->offset != offset) {
            this->translate(offset);
            this->offset = offset;
        }
    }
}

void RecycleListItem::translate(float offset) {
    auto nativeView = this->viewElement->GetNativeView();
    // TODO setElement是在waitNativeRender之后执行的，但是仍然会出现无nativeView的情况，待排查
    bool translated = false;
    if (nativeView) {
        nativeView->transformInternal(
            UniCSSTransform{UniCSSTransformTranslate::Translatey(offset, UniCSSUnitType::PX)});
        translated = true;
    } else {
        auto id = this->viewElement->GetId();
        auto page = this->viewElement->GetPage();
        auto nativeView = page->GetNativeViewById(id);
        if (nativeView) {
            nativeView->transformInternal(
                UniCSSTransform{UniCSSTransformTranslate::Translatey(offset, UniCSSUnitType::PX)});
            translated = true;
        }
    }

    if (!translated) {
        Instance::GetTaskExecutor().runOnDomQueue([viewElement = this->viewElement, offset]() {
            viewElement->UpdateStyle(UniCSSPropertyID::TransformInternal,
                                     "translateY(" + std::to_string(offset) + "px)");
        });
    }
}

// RecycleListItem end
} // namespace recycle_list
