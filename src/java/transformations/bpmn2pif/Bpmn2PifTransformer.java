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
package transformations.bpmn2pif;

import models.base.IllegalModelException;
import models.base.IllegalResourceException;
import models.choreography.bpmn.BpmnModel;
import models.process.pif.PifModel;
import org.eclipse.bpmn2.*;
import transformations.base.AbstractTransformer;

public class Bpmn2PifTransformer extends AbstractTransformer {

    public static final String NAME = "bpmn2pif";
    public static final String VERSION = "1.0";

    public Bpmn2PifTransformer() {
        super();
    }

    @Override
    public void transform() throws IllegalResourceException, IllegalModelException {
        checkModel(inputModel, BpmnModel.class);
        checkModel(outputModel, PifModel.class);
        PifModel mout = ((PifModel) outputModel);
        BpmnModel min = ((BpmnModel) inputModel);
        // TODO
        //
        message("** Transformation achieved");
    }

    @Override
    public void about() {
        System.out.println(NAME + " " + VERSION);
    }
}
