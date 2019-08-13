package com.example.vpunay.qrsample;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;


public class UI extends BaseObservable {
    private String label;
    private String buttonLabel;
    private boolean isButtonVisible;
    private boolean isLoadingScreenVisible;
    private boolean isShowLogo;
    private boolean isShowOverlay = true;
    private int logo;

    @Bindable
    public int getLogo() {
        return logo;
    }

    public void setLogo(int logo) {
        this.logo = logo;
        notifyPropertyChanged(BR.logo);
    }

    @Bindable
    public boolean isShowOverlay() {
        return isShowOverlay;
    }

    public void setShowOverlay(boolean showOverlay) {
        isShowOverlay = showOverlay;
        notifyPropertyChanged(BR.showOverlay);
    }


    @Bindable
    public boolean isShowLogo() {
        return isShowLogo;
    }

    public void setShowLogo(boolean showLogo) {
        isShowLogo = showLogo;
        notifyPropertyChanged(BR.showLogo);
    }

    @Bindable
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        notifyPropertyChanged(BR.label);
    }

    @Bindable
    public String getButtonLabel() {
        return buttonLabel;
    }

    public void setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
        notifyPropertyChanged(BR.buttonLabel);
    }

    @Bindable
    public boolean isButtonVisible() {
        return isButtonVisible;
    }

    public void setButtonVisible(boolean buttonVisible) {
        isButtonVisible = buttonVisible;
        notifyPropertyChanged(BR.buttonVisible);
    }

    @Bindable
    public boolean isLoadingScreenVisible() {
        return isLoadingScreenVisible;
    }

    public void setLoadingScreenVisible(boolean loadingScreenVisible) {
        isLoadingScreenVisible = loadingScreenVisible;
        notifyPropertyChanged(BR.loadingScreenVisible);
    }
}
