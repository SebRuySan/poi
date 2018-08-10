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

    private boolean mThisAddyOnly;
    private boolean mSortByLikes;
    private boolean mSortByScores;
    private boolean mSortByFollowers;

    private int mRadius;
    private int mTimeframe;

    private TextView mRadiusProgress;
    private TextView mActionCancel;
    private TextView mActionOk;

    private Switch mSwitchDistance; // show results for selected location only
    private Switch mSwitchDistance2; // show results within walking distance
    private Switch mSwitchLikes;
    private Switch mSwitchScores;
    private Switch mSwitchFollowers;

    private SeekBar mRadiusBar;
    private Spinner mSpinnerTime;

    public OnFilterInputListener mOnFilterInputListener;

    static String[] timeframes = new String[]{"Today only",
            "Up to Yesterday",
            "Up to 1 week ago",
            "Up to 1 month ago",
            "Up to 1 year ago",
            "All Time"};

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
       //getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);

        mRadiusProgress = view.findViewById(R.id.tvRadius);
        mActionCancel = view.findViewById(R.id.action_cancel);
        mActionOk = view.findViewById(R.id.action_ok);

        mSwitchDistance = view.findViewById(R.id.switchDistance);
        mSwitchDistance2 = view.findViewById(R.id.switchDistance2);
        mSwitchLikes = view.findViewById(R.id.switchLikes);
        mSwitchScores = view.findViewById(R.id.switchScores);
        mSwitchFollowers = view.findViewById(R.id.switchFollowers);

        mRadiusBar = view.findViewById(R.id.sbRadius);
        mSpinnerTime = view.findViewById(R.id.spinnerTime);

        // set global variables to last updated values
        mThisAddyOnly = MainActivity.mThisAddyOnly;
        mSortByLikes = MainActivity.mSortByLikes;
        mSortByScores = MainActivity.mSortByScores;
        mSortByFollowers = MainActivity.mSortByFollowers;

        mRadius = MainActivity.mRadius;
        mTimeframe = MainActivity.mTimeframe;

        // set items on screen to represent these values
        if (mRadius == 0) {
            mSwitchDistance.setChecked(true);
            mSwitchDistance2.setChecked(false);

            mSwitchDistance2.setVisibility(View.GONE);
            mRadiusProgress.setVisibility(View.GONE);
            mRadiusBar.setVisibility(View.GONE);

        } else if (mRadius == 1) {
            mSwitchDistance.setChecked(false);
            mSwitchDistance2.setChecked(true);

            mSwitchDistance2.setVisibility(View.VISIBLE);
            mRadiusProgress.setVisibility(View.GONE);
            mRadiusBar.setVisibility(View.GONE);

        } else {
            mRadiusBar.setProgress(mRadius);
            mRadiusProgress.setText("Radius: " + mRadius + " mi");
        }

        if (mSortByLikes) {
            mSwitchLikes.setChecked(true);
        } else {
            mSwitchLikes.setChecked(false);
        }

        if (mSortByScores) {
            mSwitchScores.setChecked(true);
        } else {
            mSwitchScores.setChecked(false);
        }

        if (mSortByFollowers) {
            mSwitchFollowers.setChecked(true);
        } else {
            mSwitchFollowers.setChecked(false);
        }

        // add listeners to necessary items on screen
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

        mSwitchLikes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!mSwitchLikes.isChecked()) {
                    mSortByLikes = false;
                } else {
                    mSortByLikes = true;
                }
            }
        });

        mSwitchScores.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!mSwitchScores.isChecked()) {
                    mSortByScores = false;
                } else {
                    mSortByScores = true;
                }
            }
        });

        mSwitchFollowers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!mSwitchFollowers.isChecked()) {
                    mSortByFollowers = false;
                } else {
                    mSortByFollowers = true;
                }
            }
        });

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
        mSpinnerTime.setSelection(mTimeframe);
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

                mOnFilterInputListener.sendFilterInput(mThisAddyOnly, mSortByLikes, mSortByScores, mSortByFollowers, mRadius, mTimeframe);

                try {
                    MainActivity.clear();
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

        // Make user unable to click outside of the dialog fragment to dismiss
        getDialog().setCanceledOnTouchOutside(false);

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
        void sendFilterInput(boolean thisAddyOnly, boolean sortByLikes, boolean sortByScores, boolean sortByFollowers, int radius, int timeframe);
    }

}
