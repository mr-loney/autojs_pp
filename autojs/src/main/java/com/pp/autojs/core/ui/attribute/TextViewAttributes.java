package com.pp.autojs.core.ui.attribute;

import android.view.View;
import android.widget.TextView;

import com.pp.autojs.core.ui.inflater.ResourceParser;

public class TextViewAttributes extends ViewAttributes {

    public TextViewAttributes(ResourceParser resourceParser, View view) {
        super(resourceParser, view);
    }

    @Override
    protected void onRegisterAttrs() {
        super.onRegisterAttrs();
    }

    @Override
    public TextView getView() {
        return (TextView) super.getView();
    }
}
