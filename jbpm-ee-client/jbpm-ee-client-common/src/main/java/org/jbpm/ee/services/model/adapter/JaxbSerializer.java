package org.jbpm.ee.services.model.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It would be better to drop the Externalizable interface from the base model types, 
 * so that the EJB layer can leverage the default Java serialization instead.
 * 
 * @author bradsdavis
 *
 */
public class JaxbSerializer {
	private static Logger LOG = LoggerFactory.getLogger(JaxbSerializer.class);
	
	public static void writeExternal(Object object, ObjectOutput out) throws IOException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
			jaxbContext.createMarshaller().marshal(object, baos);
			
			String outbound = baos.toString();
			if(LOG.isDebugEnabled()) {
				LOG.debug("Writing to stream:"+outbound);
			}
			out.writeUTF(outbound);
		} catch (Exception e) {
			throw new IOException("Exception writing object.", e);
		}
	}
	
	public static <T> T unmarshall(Class<T> clazz, String inbound) throws IOException {
		if(LOG.isDebugEnabled()) {
			LOG.info("Reading from stream: "+inbound);
		}
	    try {
	    	ByteArrayInputStream bais = new ByteArrayInputStream(inbound.getBytes());
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			T hydrated = (T)jaxbContext.createUnmarshaller().unmarshal(bais);
			return hydrated;
		} catch (Exception e) {
			throw new IOException("Exception reading object.", e);
		}
	}
	
	public static <T> void readExternal(Initializable<T> obj, ObjectInput in) throws IOException {
	    String inbound = in.readUTF();
	    if(LOG.isDebugEnabled()) {
	    	LOG.debug("Reading from stream: "+inbound);
	    }
	    try {
	    	ByteArrayInputStream bais = new ByteArrayInputStream(inbound.getBytes());
			JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
			T hydrated = (T)jaxbContext.createUnmarshaller().unmarshal(bais);
			obj.initialize(hydrated);
		} catch (Exception e) {
			throw new IOException("Exception reading object.", e);
		}
	}
	
	
}
