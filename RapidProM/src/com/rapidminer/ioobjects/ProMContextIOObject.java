/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.ioobjects;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.operator.ResultObjectAdapter;

/** 
 * 
 * @author Ronny Mans
 */
public class ProMContextIOObject extends ResultObjectAdapter implements ProMIOObject {

	private static final long serialVersionUID = 1725159059797569345L;

	private PluginContext pluginContext;	
	
	public ProMContextIOObject(PluginContext pc) {
		this.pluginContext = pc;
	}

	public PluginContext getPluginContext() {
		return pluginContext;
	}
	
	
	@Override
	public String toResultString() {
		return "ProMContextIOObject:" + this.pluginContext.getLabel() + "," + this.pluginContext.getID();
	}

	@Override
	public void clear() {
		PluginContext parentContext = this.pluginContext.getParentContext();
		this.pluginContext.clear();
		parentContext.clear();
		this.pluginContext = null;
	}
}
