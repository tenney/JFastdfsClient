package com.eiviv.fdfs.cmd;

import java.io.IOException;
import java.net.Socket;

import com.eiviv.fdfs.model.Result;

public interface Cmd<T> {
	
	Result<T> exec(Socket socket) throws IOException;
	
}
