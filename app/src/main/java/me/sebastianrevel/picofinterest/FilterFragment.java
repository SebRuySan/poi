package me.sebastianrevel.picofinterest;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;

import java.io.IOException;
import java.util.Map;


public class FilterFragment extends DialogFragment {

    private Switch mSwitchDistance; // show results for selected location only
    private Switch mSwitchDistance2; // show results within walking distance
    private TextView mRadiusProgress;
    private SeekBar mRadiusBar;
    private boolean mThisAddyOnly;
    private int mRadius;
    private Spinner mSpinnerTime;
    private int mTimeframe;
    private TextView mActionCancel;
    private TextView mActionOk;

    public OnFilterInputListener mOnFilterInputListener;

    static String[] timeframes = new String[]{"Today only",
            "Up to Yesterday",
            "Up to 1 week ago",
            "Up to 1 month ago",
            "Up to 1 year ago",
            "All time"};

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

        mSwitchDistance = view.findViewById(R.id.switchDistance);
        mSwitchDistance2 = view.findViewById(R.id.switchDistance2);
        mRadiusProgress = view.findViewById(R.id.tvRadius);
        mRadiusBar = view.findViewById(R.id.sbRadius);
        mSpinnerTime = view.findViewById(R.id.spinnerTime);
        mActionCancel = view.findViewById(R.id.action_cancel);
        mActionOk = view.findViewById(R.id.action_ok);

        mRadiusBar.setProgress(MainActivity.mRadius);
        mRadiusProgress.setText("Radius: " + MainActivity.mRadius + " mi");

        mSwitchDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!mSwitchDistance.isChecked()) {
                    mThisAddyOnly = false;

                    mSwitchDistance2.setVisibility(View.VISIBLE);
                    mRadiusProgress.setVisibility(View.VISIBLE);
                    mRadiusBar.setVisibility(View.VISIBLE);

                    mSwitchDistance2.setChecked(false);
                } else {
                    mThisAddyOnly = true;
                    mRadius = 0;

                    mSwitchDistance2.setVisibility(View.GONE);
                    mRadiusProgress.setVisibility(View.GONE);
                    mRadiusBar.setVisibility(View.GONE);
                }
            }
        });

        mSwitchDistance2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!mSwitchDistance2.isChecked()) {
                    mRadius = mRadiusBar.getProgress();

                    mRadiusProgress.setVisibility(View.VISIBLE);
                    mRadiusBar.setVisibility(View.VISIBLE);
                } else {
                    mRadius = 1;

                    mRadiusProgress.setVisibility(View.GONE);
                    mRadiusBar.setVisibility(View.GONE);
                }
            }
        });

        mRadius = 15;
        mRadiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mRadiusProgress.setText("Radius: " + i + " mi");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mRadius = seekBar.getProgress();
                //Toast.makeText(getContext(), "Radius input: " + mRadius, Toast.LENGTH_SHORT);
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, timeframes);
        mSpinnerTime.setAdapter(adapter);
        mSpinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mTimeframe = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mTimeframe = 5;
            }
        });

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
                mOnFilterInputListener.sendFilterInput(mThisAddyOnly, mRadius, mTimeframe);
                MainActivity.clear();

                try {
                    MainActivity.loadAll();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                MapFragment.showMap();
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
        void sendFilterInput(boolean thisAddyOnly, int radius, int timeframe);
    }

}
