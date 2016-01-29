/*
 * Date: 22/7/2008
 * Author: rcote
 * File: uk.ac.ebi.jmzml.xml.io.MzMLMarshaller
 *
 * jmzml is Copyright 2008 The European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 */

package uk.ac.ebi.jmzml.xml.io;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import uk.ac.ebi.jmzml.model.mzml.MzML;
import uk.ac.ebi.jmzml.model.mzml.MzMLObject;
import uk.ac.ebi.jmzml.model.mzml.utilities.ModelConstants;
import uk.ac.ebi.jmzml.xml.Constants;
import uk.ac.ebi.jmzml.xml.jaxb.marshaller.MarshallerFactory;
import uk.ac.ebi.jmzml.xml.util.EscapingXMLStreamWriter;


public class MzMLMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(MzMLMarshaller.class);

//    jaxb.fragment - value must be a java.lang.Boolean
//        This property determines whether or not document level events will be generated by the Marshaller. If the property is not specified, the default is false. This property has different implications depending on which marshal api you are using - when this property is set to true:
//
//            * marshal(Object,ContentHandler) - the Marshaller won't invoke ContentHandler.startDocument() and ContentHandler.endDocument().
//            * marshal(Object,Node) - the property has no effect on this API.
//            * marshal(Object,OutputStream) - the Marshaller won't generate an xml declaration.
//            * marshal(Object,Writer) - the Marshaller won't generate an xml declaration.
//            * marshal(Object,Result) - depends on the kind of Result object, see semantics for Node, ContentHandler, and Stream APIs
//            * marshal(Object,XMLEventWriter) - the Marshaller will not generate XMLStreamConstants.START_DOCUMENT and XMLStreamConstants.END_DOCUMENT events.
//            * marshal(Object,XMLStreamWriter) - the Marshaller will not generate XMLStreamConstants.START_DOCUMENT and XMLStreamConstants.END_DOCUMENT events.
//

    public <T extends MzMLObject> String marshall(T object) {
        StringWriter sw = new StringWriter();
        this.marshall(object, sw);
        return sw.toString();
    }

    public <T extends MzMLObject> void marshall(T object, OutputStream os) {
        this.marshall(object, new OutputStreamWriter(os));
    }

    public <T extends MzMLObject> void marshall(T object, Writer out) {

        if (object == null) {
            throw new IllegalArgumentException("Cannot marshall a NULL object");
        }

        try {
            Marshaller marshaller = MarshallerFactory.getInstance().initializeMarshaller();

            // Set JAXB_FRAGMENT_PROPERTY to true for all objects that do not have
            // a @XmlRootElement annotation
            // ToDo: add handling of indexedmzML (-> add flag to control treatment as fragment or not)
            if (!(object instanceof MzML)) {
                marshaller.setProperty(Constants.JAXB_FRAGMENT_PROPERTY, true);
                if (logger.isDebugEnabled()) logger.debug("Object '" + object.getClass().getName() +
                                                          "' will be treated as root element.");
            } else {
                if (logger.isDebugEnabled()) logger.debug("Object '" + object.getClass().getName() +
                                                          "' will be treated as fragment.");
            }

            QName aQName = ModelConstants.getQNameForClass(object.getClass());

            // before marshalling out, wrap in a Custom XMLStreamWriter
            // to fix a JAXB bug: http://java.net/jira/browse/JAXB-614
            // also wrapping in IndentingXMLStreamWriter to generate formatted XML
            //XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
            System.setProperty("javax.xml.stream.XMLOutputFactory", "com.sun.xml.internal.stream.XMLOutputFactoryImpl");

//            XMLOutputFactory factory = XMLOutputFactory.newFactory("com.sun.xml.internal.stream.XMLOutputFactoryImpl", null);
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(out);

            IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(new EscapingXMLStreamWriter(xmlStreamWriter));
            marshaller.marshal( new JAXBElement(aQName, object.getClass(), object), writer );

        } catch (JAXBException e) {
            logger.error("MzMLMarshaller.marshall", e);
            throw new IllegalStateException("Error while marshalling object:" + object.toString());
        } catch (XMLStreamException e) {
            logger.error("MzMLMarshaller.marshall", e);
            throw new IllegalStateException("Error while marshalling object:" + object.toString());
        }

    }

    // ToDo: default marshaller can only cope with mzML or sub-elements 
    // ToDo: ?? new marshal method to create indexedmzML (with parameter specifying the elements to index)

    

}
