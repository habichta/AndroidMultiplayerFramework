package ch.ethz.inf.vs.hostControllerLib;

import android.os.Handler;

public interface ICommunicatorCallback<T> {

	/**
	 * The method which will be called once a host is discovered.
	 * @param host The discovered host.
	 */
	public void onHostDiscovered(T host);
	
	/**
	 * Returns a handler the called methods of this object shall be posted to
	 * @return A handler the called methods of this object shall be posted to
	 */
	public Handler getCallbackHandler();
}
