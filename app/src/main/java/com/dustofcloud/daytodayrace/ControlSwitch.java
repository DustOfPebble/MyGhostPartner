package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ControlSwitch extends FrameLayout
{
    private ImageView buttonPicture=null;
    private ImageView buttonBackground=null;
    private TextView buttonLabel=null;

    // Storing Icon views
    private Drawable BackgroundInactive =null;
    private Drawable BackgroundActive =null;

    public ControlSwitch(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initObjects(context);

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ButtonState, 0, 0);
        try
        {
            String label =  attributes.getString(R.styleable.ButtonState_label);
            setLabel(label);

            Drawable ButtonIcon = attributes.getDrawable(R.styleable.ButtonState_picture);
            buttonPicture.setBackground(ButtonIcon);

            BackgroundInactive = attributes.getDrawable(R.styleable.ButtonState_inactive);
            BackgroundActive = attributes.getDrawable(R.styleable.ButtonState_active);
            setStateInactive();
        }
        finally { attributes.recycle();}
    }

    private void initObjects(Context context)
    {
        // Inflate the Layout from XML definition
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.button_icon_text, this, true);

        buttonBackground = (ImageView) getChildAt(0);
        buttonPicture = (ImageView) getChildAt(1);

        buttonLabel = (TextView) getChildAt(2);
    }

    public void setLabel(String label)
    {
        if (label == null) return;
        buttonLabel.setText(label);
        invalidate();
        requestLayout();
    }


    public void setStateActive()
    {
        buttonBackground.setBackground(BackgroundActive);
        invalidate();
        requestLayout();
    }

    public void setStateInactive()
    {
        buttonBackground.setBackground(BackgroundInactive);
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int TopPadding = getPaddingTop();
        int BottomPadding = getPaddingBottom();

        int AvailableSpace = getMeasuredHeight() - TopPadding - BottomPadding;
        int IconSpace = AvailableSpace / 2;
        int LabelSpace = AvailableSpace / 7;

        buttonLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) LabelSpace);
        ViewGroup.LayoutParams Container = buttonPicture.getLayoutParams();
        Container.width = IconSpace;
        Container.height = IconSpace;
        buttonPicture.setLayoutParams(Container);
        buttonBackground.setLayoutParams(Container);
    }
}
