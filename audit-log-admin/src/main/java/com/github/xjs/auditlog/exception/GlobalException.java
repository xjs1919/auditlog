package com.github.xjs.auditlog.exception;


import com.github.xjs.auditlog.vo.common.CodeMsg;

public class GlobalException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	
	private CodeMsg codeMsg;

	public GlobalException(CodeMsg codeMsg) {
		this(codeMsg, null);
	}
	
	public GlobalException(CodeMsg codeMsg, Throwable t) {
		super(codeMsg.getCode()+":"+codeMsg.getMsg(), t);
		this.codeMsg = codeMsg;
	}
	
	public CodeMsg getCodeMsg() {
		return this.codeMsg;
	}

	@Override
	public String toString() {
		return "GlobalException [codeMsg=" + codeMsg + "]";
	}
	
}
