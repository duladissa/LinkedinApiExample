package com.litedreamz.linkedinapisample.interfaces;

import com.litedreamz.linkedinapisample.model.LinkedinPeopleProfile;

import java.util.ArrayList;

/*
 * @author Dulan Dissanayake
 * @date 25/02/2015
 * 
 * This interface used to link between LinkedinWrapper and Activities.
 * 
 * */

public interface ILinkedinApiCallListner {
    public void onLinkedinApiCallSuccess(ArrayList<LinkedinPeopleProfile> people);

    public void onLinkedinApiCallError(String message);
}
