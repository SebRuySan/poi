package me.sebastianrevel.picofinterest;

import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;


public class FilterFragment extends DialogFragment {


    private TextView mActionOk;
    private TextView mActionCancel;

    public OnFilterInputListener mOnFilterInputListener;

    public FilterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_filter, container, false);

        mActionOk = view.findViewById(R.id.action_ok);
        mActionCancel = view.findViewById(R.id.action_cancel);

        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FilterFragment", "onClick: closing dialog");
                getDialog().dismiss();
            }
        });

        mActionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FilterFragment", "onClick: capturing input");

                // TODO: get and update filters
                mOnFilterInputListener.sendFilterInput("Input sent.");

                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
        mOnFilterInputListener = (OnFilterInputListener) getActivity();
        } catch (ClassCastException e) {
            Log.e("FilterFragment", "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    public interface OnFilterInputListener {
        void sendFilterInput(String input);
    }

}
