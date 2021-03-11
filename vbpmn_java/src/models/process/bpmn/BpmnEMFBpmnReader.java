/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * <p>
 * vbpmn
 * Copyright (C) 2014 Pascal Poizat (@pascalpoizat)
 * emails: pascal.poizat@lip6.fr
 */

package models.process.bpmn;

// emf

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl; // keep
// bpmn2
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
// fmt
import models.base.AbstractModel;
import models.base.AbstractModelReader;
import models.base.IllegalModelException;
import models.base.IllegalResourceException;

import java.io.IOException;

public class BpmnEMFBpmnReader extends AbstractModelReader {

    @Override
    public String getSuffix() {
        return "bpmn";
    }

    @Override
    public void modelFromFile(AbstractModel model) throws IOException, IllegalResourceException, IllegalModelException {
        checkModel(model, BpmnModel.class);
        BpmnModel bpmnModel = (BpmnModel) model;
        // load the model using the BPMN EMF model
        URI uri = URI.createURI(bpmnModel.getResource().getPath());
        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        reg.getExtensionToFactoryMap().put(getSuffix(), new Bpmn2ResourceFactoryImpl());
        ResourceSet rs = new ResourceSetImpl();
        Resource res = rs.getResource(uri, true);
        res.load(null);
        EObject root = res.getContents().get(0);
        // search for the process instance
        Definitions definitions;
        if (root instanceof DocumentRoot) {
            definitions = ((DocumentRoot) root).getDefinitions();
        } else {
            definitions = (Definitions) root;
        }
        bpmnModel.setModel(definitions);
    }
}
