package com.example.pccsmartapp.ui_staff.listanggota;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ListAnggotaViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ListAnggotaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is listevent fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}