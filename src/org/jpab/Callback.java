package org.jpab;

import java.nio.ByteBuffer;

/**
 * The Callback interface is used to define custom functionality for a stream.
 * Behind every stream, there is a callback that handles how data from the 
 * stream's input device is handles and where data for the stream's output comes
 * from.
 * 
 * @author Ryan Holdren
 */
public interface Callback {

	/**
	 * The State enumeration is facilitating convenient state changes in the 
	 * stream following a callback.
	 * 
	 * @author Ryan Holdren
	 */
	enum State {
		/**
		 * Indicates that the stream should continue to be processed.
		 */
		RUNNING, 
		
		/**
		 * Indicated that all data has been received/outputted and no further 
		 * callbacks are necessary.
		 */
		COMPLETE,
		
		/**
		 * Indicated that something has gone wrong, and the stream will cease 
		 * operations as soon as possible, possibly abandoning data already in 
		 * the buffer.
		 */
		ABORTED;
	}

	/**
	 * The method that will be called when a stream requires additional output data and/or has input data.
	 * 
	 * @param input A direct ByteBuffer with data from the input device or null if the stream is output-only.
	 * @param output A direct ByteBuffer to be filled with data for the output device or null if the stream is input-only.
	 * @return The new state.
	 */
	State callback(ByteBuffer input, ByteBuffer output);

}