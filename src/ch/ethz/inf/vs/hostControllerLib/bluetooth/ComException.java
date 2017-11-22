package ch.ethz.inf.vs.hostControllerLib.bluetooth;

public class ComException extends Exception {

	private static final long serialVersionUID = 6134274232583515505L;

	public enum ComExceptionType{
		INVALID_MESSAGE, RECEIVE_FAILURE, SEND_FAILURE, CLOSE_FAILURE, INTERNAL_FAILURE, INVALID_IDENTIFIER;
	}
	
	public ComExceptionType type;
	
	public ComException(ComExceptionType type, String detailMessage){
		super(detailMessage);
		this.type = type;
	}
	
	public ComException(ComExceptionType type){
		this.type = type;
	}
	
	public ComException(String detailMessage){
		super(detailMessage);
	}
}
