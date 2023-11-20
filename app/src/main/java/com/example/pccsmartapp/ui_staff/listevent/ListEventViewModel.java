package com.example.pccsmartapp.ui_staff.listevent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ListEventViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ListEventViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is profile fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}