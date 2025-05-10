package com.lena.android.vm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ShortLifecycleModel extends ViewModel {
    public final MutableLiveData<Lifecycle> liveLifeData;

    public ShortLifecycleModel() {
        this.liveLifeData = new MutableLiveData<>();
        this.liveLifeData.postValue(Lifecycle.UNKNOWN);
    }

    public enum Lifecycle {
        UNKNOWN, CREATE, START, RESUME, PAUSE, STOP, DESTROY
    }
}
