package com.eiviv.fdfs.cmd;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

import com.eiviv.fdfs.model.Result;

public interface Cmd<T extends Serializable> {
	
	Result<T> exec(Socket socket) throws IOException;
	
}
