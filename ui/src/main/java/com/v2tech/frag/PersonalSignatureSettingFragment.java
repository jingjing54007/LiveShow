package com.v2tech.frag;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.v2tech.R;
import com.v2tech.service.PersonalSetting;


public class PersonalSignatureSettingFragment extends Fragment {

    PersonalSetting settingService;

    private EditText et;

    public PersonalSignatureSettingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = LayoutInflater.from(getActivity()).inflate(R.layout.personal_signature_setting_frag_layout, null);
        et = (EditText)root.findViewById(R.id.personal_signature_setting_et);

        return root;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public PersonalSetting getSettingService() {
        return settingService;
    }

    public void setSettingService(PersonalSetting settingService) {

        this.settingService = settingService;
    }
}
