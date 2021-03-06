package de.tum.in.tumcampusapp.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampusapp.adapters.StudyRoomsPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.models.StudyRoom;
import de.tum.in.tumcampusapp.models.StudyRoomGroup;
import de.tum.in.tumcampusapp.models.managers.StudyRoomGroupManager;

/**
 * Shows information about reservable study rooms.
 */
public class StudyRoomsActivity extends ActivityForDownloadingExternal implements AdapterView
        .OnItemSelectedListener {

    private List<StudyRoomGroup> mStudyRoomGroupList;
    private int mSelectedStudyRoomGroupId = -1;
    private ViewPager mViewPager;
    private StudyRoomsPagerAdapter mSectionsPagerAdapter;

    public StudyRoomsActivity() {
        super(Const.STUDY_ROOMS, R.layout.activity_study_rooms);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        requestDownload(false);
    }

    @Override
    /**
     * Setup for switching study room locations via action bar
     */
    protected void onStart() {
        super.onStart();

        StudyRoomGroupManager studyRoomGroupManager = new StudyRoomGroupManager(this);
        mStudyRoomGroupList = studyRoomGroupManager.getStudyRoomGroupsFromCursor
                (studyRoomGroupManager.getAllFromDb());
        for (StudyRoomGroup group : mStudyRoomGroupList) {
            sortStudyRoomsByOccupation(group.rooms);
        }

        if (hasGotStudyRoomGroups()) {
            Spinner spinner = getStudyRoomGroupsSpinner();
            selectCurrentSpinnerItem(spinner);
        } else
            showCorrectErrorLayout();

    }

    private void selectCurrentSpinnerItem(Spinner spinner) {
        for (int i = 0; i < mStudyRoomGroupList.size(); i++) {
            StudyRoomGroup s = mStudyRoomGroupList.get(i);
            if (mSelectedStudyRoomGroupId == -1 || mSelectedStudyRoomGroupId == s.id) {
                mSelectedStudyRoomGroupId = s.id;
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void sortStudyRoomsByOccupation(List<StudyRoom> studyRooms) {
        Collections.sort(studyRooms, new Comparator<StudyRoom>() {
            @Override
            public int compare(StudyRoom lhs, StudyRoom rhs) {
                return lhs.occupiedTill.compareTo(rhs.occupiedTill);
            }
        });
    }

    private Spinner getStudyRoomGroupsSpinner() {
        // Adapter for drop-down navigation
        ArrayAdapter<StudyRoomGroup> adapterCafeterias =
                new ArrayAdapter<StudyRoomGroup>(this, R.layout.simple_spinner_item_actionbar,
                        android.R.id.text1, mStudyRoomGroupList) {
                    final LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(LAYOUT_INFLATER_SERVICE);

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View v = inflater.inflate(R.layout.simple_spinner_dropdown_item_actionbar,
                                parent, false);
                        StudyRoomGroup studyRoomGroup = getItem(position);

                        // Set name
                        TextView name = (TextView) v.findViewById(android.R.id.text1);
                        name.setText(studyRoomGroup.name);

                        // Set detail
                        TextView details = (TextView) v.findViewById(android.R.id.text2);
                        details.setText(studyRoomGroup.details);

                        return v;
                    }
                };

        Spinner spinner = (Spinner) findViewById(R.id.spinnerToolbar);
        spinner.setAdapter(adapterCafeterias);
        spinner.setOnItemSelectedListener(this);
        return spinner;
    }

    private boolean hasGotStudyRoomGroups() {
        return mStudyRoomGroupList != null && mStudyRoomGroupList.size() != 0;
    }

    private void showCorrectErrorLayout() {
        if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
        } else {
            showErrorLayout();
        }
    }

    /**
     * A new study room group has been selected -> Switch.
     *
     * @param parent the parent view
     * @param pos    index of the new selection
     * @param id     id of the selected item
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mSelectedStudyRoomGroupId = mStudyRoomGroupList.get(pos).id;

        if (mSectionsPagerAdapter == null)
            setupViewPagerAdapter(mSelectedStudyRoomGroupId);
        else
            changeViewPagerAdapter(mSelectedStudyRoomGroupId);
    }

    private void changeViewPagerAdapter(int mSelectedStudyRoomGroupId) {
        mViewPager.setAdapter(null); //unset the adapter for updating
        mSectionsPagerAdapter.setStudyRoomGroupId(this, mSelectedStudyRoomGroupId);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    private void setupViewPagerAdapter(int mSelectedStudyRoomGroupId) {
        mSectionsPagerAdapter = new StudyRoomsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.setStudyRoomGroupId(this, mSelectedStudyRoomGroupId);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //
    }

    public void goToRoomFinder(View view) {
        String link = ((TextView) view).getText().toString();
        String roomCode = link.substring(link.indexOf(' ') + 1, link.length());

        Intent findStudyRoomIntent = new Intent();
        findStudyRoomIntent.putExtra(SearchManager.QUERY, roomCode);
        findStudyRoomIntent.setClass(this, RoomFinderActivity.class);
        startActivity(findStudyRoomIntent);
    }
}
