/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.apps.qsp.config;

import org.jpos.util.NameRegistrar;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.FilteredChannel;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
//Add for calculate filter, Zhiyu Tang
import org.jpos.core.NodeConfigurable;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/**
 * Configure filter
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.iso.ISOFilter
 */
public class ConfigFilter implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	LogEvent evt = new LogEvent (qsp, "config-filter");
	Node parent;

	// Find parent Channel 
	if ( (parent  = node.getParentNode()) == null)
	    throw new ConfigurationException ("orphan filter");

	ISOChannel c = ConfigChannel.getChannel (parent);
	if (c == null) 
	    throw new ConfigurationException ("null parent channel");
	if (!(c instanceof FilteredChannel))
	    throw new ConfigurationException ("not a filtered channel");

	FilteredChannel channel = (FilteredChannel) c;

	NamedNodeMap attr = node.getAttributes();
	String className = attr.getNamedItem ("class").getNodeValue();
	String direction = attr.getNamedItem ("direction").getNodeValue();

	ISOFilter filter = (ISOFilter) ConfigUtil.newInstance (className);
	if (filter instanceof Configurable) {
	    try {
		((Configurable)filter).setConfiguration (
		    new SimpleConfiguration (
			ConfigUtil.addProperties (node, null, evt)
			)
		);
	    } catch (ISOException e) {
		throw new ConfigurationException (e);
	    }
	}
	 //Add for calculate filter, Zhiyu Tang
	if( filter instanceof NodeConfigurable ){
		try {
		((NodeConfigurable)filter).setConfiguration (
		   	node
		);
	    } catch (ISOException e) {
		throw new ConfigurationException (e);
	    }
	}
	if (direction.equals ("incoming"))
	    channel.addIncomingFilter (filter);
	else if (direction.equals ("outgoing"))
	    channel.addOutgoingFilter (filter);
	else
	    channel.addFilter (filter);

	evt.addMessage ("parent-channel=" + channel.getName());
	Logger.log (evt);
    }
}
