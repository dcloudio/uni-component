#pragma once

#include "sdk.h"
#include "sdk.cpp.h"

using namespace uniappx;
using namespace vue;

namespace input {
class Input : public UniVueComponent {
private:
    double keyboardHeight = 0;
    bool isFocusing = false;
    bool adjustPosition = true;
    Element *element = nullptr;
    bool resetElementPositionWhenBlur = false;

public:
    Input() = default;
    ~Input() = default;
    void setElement(Element *element);
    void setIsFocusing(bool focus);
    void setAdjustPosition(bool adjust);
    void handleKeyboardHeightChange(double height);
    void handleElementPositionWhenFocus();
    void setBodyTranslateY(double y);
};
} // namespace input
