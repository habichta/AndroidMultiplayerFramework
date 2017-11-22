package ch.ethz.inf.vs.hostControllerLib.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import ch.ethz.inf.vs.hostControllerLib.ISimplexChannel;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

/**
 * This class describes a simple bluetooth communication channel.
 * @author Romy Profanter
 *
 */
public class BluetoothSimplexChannel<T extends Serializable> implements ISimplexChannel<T, ComException> {

	private final int BUFFER_SIZE;
	/**
	 * The bluetooth socket the messages are sent to and received from
	 */
	private BluetoothSocket bluetoothSocket;
	
	/**
	 * The inputstream of the bluetooth socket
	 */
	private InputStream inputStream;
	
	/**
	 * The outputstream of the bluetooth socket
	 */
	private OutputStream outputStream;
	
	public BluetoothSimplexChannel(BluetoothSocket socket, int receiveBufferSize){
		
		this.BUFFER_SIZE=receiveBufferSize;
		this.bluetoothSocket = socket;
		try {
			this.inputStream = socket.getInputStream();
			this.outputStream = socket.getOutputStream();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	@Override
	public void send(T message) throws ComException {
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream;
			try {
				objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
				objectOutputStream.writeObject(message);
				objectOutputStream.flush();
				
			} catch (IOException e) {
				throw new ComException(ComException.ComExceptionType.INTERNAL_FAILURE);
			}
				
			byte[] messageAsBytes = byteArrayOutputStream.toByteArray();
			
			try {
				outputStream.write(messageAsBytes);
				outputStream.flush();
			} catch (IOException e) {
				throw new ComException(ComException.ComExceptionType.SEND_FAILURE);
			}
	}

	@Override
	public T receive() throws ComException {
		
		T message = null;
		
		byte[] buffer = new byte[BUFFER_SIZE];
		
		ObjectInput objectInput = null;
		Object object = null;
		int retVal;
			try {
				retVal = inputStream.read(buffer);					
			} catch (IOException e) {
				throw new ComException(ComException.ComExceptionType.RECEIVE_FAILURE);
			}
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
			try {
				objectInput = new ObjectInputStream(byteArrayInputStream);
			} catch (StreamCorruptedException e1) {
				Log.d("TAG", ""+retVal);
				throw new ComException(ComException.ComExceptionType.INVALID_MESSAGE);
			} catch (IOException e1) {
				throw new ComException(ComException.ComExceptionType.INTERNAL_FAILURE);
			}

			try {
				object = objectInput.readObject();
			} catch (ClassNotFoundException e) {
				throw new ComException(ComException.ComExceptionType.INVALID_MESSAGE);
			} catch (IOException e) {
				throw new ComException(ComException.ComExceptionType.INTERNAL_FAILURE);
			}
			
			try{
			message = (T)object;
			} catch (Exception e){
				throw new ComException(ComException.ComExceptionType.INVALID_MESSAGE);
			}
		
		return message;
		
	}

	@Override
	public void close() throws ComException{

			try {
				inputStream.close();
				outputStream.close();
				bluetoothSocket.close();
			} catch (IOException e) {
				throw new ComException(ComException.ComExceptionType.CLOSE_FAILURE);
			}
		
	}

}
