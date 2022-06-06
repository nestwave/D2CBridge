/******************************************************************************
 * Copyright 2022 - NESTWAVE SAS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *****************************************************************************/
package com.nestwave.device.model;

import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import static java.lang.System.arraycopy;


@Data
public class Assistance {

  @NotNull
  @NotBlank
  private LocalDateTime fileTime;

  @NotNull
  @NotBlank
  private byte[] content;

	@NotNull
	@NotBlank
	private final boolean isBinary;

	public Assistance(){
		this.fileTime = null;
		this.content = null;
		this.isBinary = false;
	}

	public Assistance(boolean isBinary){
		this.fileTime = null;
		this.content = null;
		this.isBinary = isBinary;
	}

	public Assistance(LocalDateTime fileTime, byte[] content){
		this.fileTime = fileTime;
		setContent(content);
		this.isBinary = false;
	}

	public Assistance(LocalDateTime fileTime, byte[] content, boolean isBinary){
		this.fileTime = fileTime;
		setContent(content);
		this.isBinary = isBinary;
	}

	public void setContent(byte[] content) {
		if(isBinary || content == null || content.length <= 0 || content[content.length - 1] == 0){
			this.content = content;
		}else {
			this.content = new byte[content.length + 1];
			arraycopy(content, 0, this.content, 0, content.length);
			this.content[content.length] = 0;
		}
	}
}
