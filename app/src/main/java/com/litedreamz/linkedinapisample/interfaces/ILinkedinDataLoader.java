package com.litedreamz.linkedinapisample.interfaces;

import org.scribe.model.Token;

/*
 * @author Dulan Dissanayake
 * @date 25/02/2015
 * 
 * This interface used to link between LinkedinWrapper and Activities.
 * 
 * */

public interface ILinkedinDataLoader {
    public Token getLinkedinAccessTokenFromDevice();

    public void getCurrentUserProfile(ILinkedinApiCallListner listner);

    public void getCurrentUserConnections(ILinkedinApiCallListner listner);
}
