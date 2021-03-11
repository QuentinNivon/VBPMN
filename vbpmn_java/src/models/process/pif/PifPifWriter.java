/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * fmt
 * Copyright (C) 2014 Pascal Poizat (@pascalpoizat)
 * emails: pascal.poizat@lip6.fr
 */

package models.process.pif;

import models.base.AbstractModel;
import models.base.AbstractModelWriter;
import models.base.IllegalModelException;
import models.base.IllegalResourceException;
import models.process.pif.generated.Process;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PifPifWriter extends AbstractModelWriter {
    public static final String SCHEMA_PATH = "model/pif.xsd";
    public static final boolean VALIDATE = true;

    @Override
    public String getSuffix() {
        return "pif";
    }

    @Override
    public void modelToFile(AbstractModel model) throws IOException, IllegalResourceException, IllegalModelException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            checkModel(model, PifModel.class);
            PifModel pifModel = (PifModel) model;
            FileOutputStream fos = new FileOutputStream(pifModel.getResource());
            final JAXBContext ctx = JAXBContext.newInstance(Process.class);
            final Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            if(VALIDATE) {
                Schema schema = factory.newSchema(new StreamSource(new File(SCHEMA_PATH)));
                marshaller.setSchema(schema);
            }
            marshaller.marshal(pifModel.getModel(), fos);
        } catch (JAXBException e) {
            throw new IllegalModelException(e.toString());
        } catch (SAXException e) {
            throw new IllegalResourceException(e.getMessage());
        }
    }
}