package com.rapidminer.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SingleInstanceLock {
	
	private static SingleInstanceLock instance = new SingleInstanceLock();
	
	private Lock lock = new ReentrantLock();
	
	private SingleInstanceLock() {
		
	}
	
	public static SingleInstanceLock getInstance () {
		return instance;
	}
	
	public Lock getLock () {
		return this.lock;
	}

}
