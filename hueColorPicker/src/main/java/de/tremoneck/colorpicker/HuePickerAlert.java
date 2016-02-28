package de.tremoneck.colorpicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class HuePickerAlert extends AlertDialog implements AlertDialog.OnClickListener, AlertDialog.OnCancelListener {

    private HueColorPicker picker;
    private ColorPickerListener listener;
    private int color;

    public HuePickerAlert(Context context, int color) {
        super(context);
        picker = new HueColorPicker(context, null);
        picker.setColor(color);
        setView(picker);
        this.color = color;
        setCancelable(true);
        setButton(BUTTON_POSITIVE, context.getString(R.string.alert_choose), this);
        setButton(BUTTON_NEGATIVE, context.getString(R.string.alert_cancel), this);
        setOnCancelListener(this);
    }

    public int getColor() {
        return picker.getColor();
    }

    public void setListener(ColorPickerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        listener.canceled();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == BUTTON_POSITIVE)
            listener.onColorSelected(picker.getColor());
        else
            listener.canceled();
    }

    public interface ColorPickerListener {
        void onColorSelected(int color);

        void canceled();
    }
}
