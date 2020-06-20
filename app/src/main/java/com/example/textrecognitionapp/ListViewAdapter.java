package com.example.textrecognitionapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import static android.app.Activity.RESULT_OK;

public class ListViewAdapter extends BaseAdapter {

    private Intent dataRet = new Intent();
    private Context context;
    private String[] data;
    private static LayoutInflater inflater = null;
    private Button closePopupBtn, deleteBtn;
    private PopupWindow popupWindow;
    private ImageButton trashBtn;
    private View view;
    private FrameLayout frame;

    ListViewAdapter(Context context, String[] data) {
        this.context = context;
        this.data = data;
        frame = ((Activity)context).findViewById(R.id.linearLayout);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.archive_row, null);
        TextView text = view.findViewById(R.id.titleBox);
        text.setText(data[position]);
        trashBtn = view.findViewById(R.id.btnDelete);
        trashBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                View customView = inflater.inflate(R.layout.popup, null);

                closePopupBtn = customView.findViewById(R.id.btnCancel);
                deleteBtn = customView.findViewById(R.id.btnDelete);

                //instantiate popup window
                popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                frame.getForeground().setAlpha(150);

                //display the popup window
                popupWindow.showAtLocation(trashBtn, Gravity.CENTER, 0, 0);

                //close the popup window on button click
                closePopupBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        frame.getForeground().setAlpha(0);
                    }
                });
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.getFilesDir().listFiles()[position].delete();
                        ((Activity) context).recreate();
                        popupWindow.dismiss();
                    }
                });
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            int pos = position;

            public void onClick(View v) {
                String text = context.getFilesDir().listFiles()[pos].getName();
                dataRet.setData(Uri.parse(text));
                ((Activity) context).setResult(RESULT_OK, dataRet);
                ((Activity) context).finish();
            }
        });

        return view;
    }
}
